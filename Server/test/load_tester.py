
import multiprocessing
import Queue
import threading
import time
import socket
import ssl


URL = 'localhost'
PORT = 8001
PROCESSES = 4
PROCESS_THREADS = 100
INTERVAL = 0.1  # secs
RUN_TIME = 100  # secs
RAMPUP = 0  # secs


def main():
    q = multiprocessing.Queue()
    rw = ResultWriter(q)
    rw.setDaemon(True)
    rw.start()

    start_time = time.time()

    for i in range(PROCESSES):
        manager = LoadManager(
            q, start_time, i, PROCESS_THREADS, INTERVAL, RUN_TIME, RAMPUP)
        manager.start()


class LoadManager(multiprocessing.Process):

    def __init__(self, queue, start_time, process_num, num_threads=1,
                 interval=0, run_time=10, rampup=0):
        multiprocessing.Process.__init__(self)
        self.q = queue
        self.start_time = start_time
        self.process_num = process_num
        self.num_threads = num_threads
        self.interval = interval
        self.run_time = run_time
        self.rampup = rampup

    def run(self):
        thread_refs = []
        for i in range(self.num_threads):
            spacing = float(self.rampup) / float(self.num_threads)
            if i > 0:
                time.sleep(spacing)
            agent_thread = LoadAgent(
                self.q, self.interval, self.start_time,
                self.run_time)
            agent_thread.setDaemon(True)
            thread_refs.append(agent_thread)
            print('starting process %i, thread %i' % (self.process_num + 1,
                                                      i + 1))
            agent_thread.start()
        for agent_thread in thread_refs:
            agent_thread.join()


class LoadAgent(threading.Thread):

    def __init__(self, queue, interval, start_time, run_time):
        threading.Thread.__init__(self)
        self.q = queue
        self.interval = interval
        self.start_time = start_time
        self.run_time = run_time
        self.default_timer = time.time

    def run(self):
        while True:
            start = self.default_timer()
            try:
                result = self.send()
            except Exception, e:
                result = "error"
                print e
            finish = self.default_timer()
            latency = finish - start
            elapsed = time.time() - self.start_time
            self.q.put((elapsed, latency, result))
            if elapsed >= self.run_time:
                break
            expire_time = self.interval - latency
            if expire_time > 0:
                time.sleep(expire_time)

    def send(self):
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        ssl_s = ssl.wrap_socket(s)
        ssl_s.connect((URL, PORT))
        try:
            # This should tell you the number of connection made so far
            result = ssl_s.read().split()[-1]
            ssl_s.write('get]demo')
            ssl_s.read()
        except Exception, e:
            raise Exception('Connection Error: %s' % e)
        finally:
            ssl_s.close()
        return result


class ResultWriter(threading.Thread):

    def __init__(self, queue):
        threading.Thread.__init__(self)
        self.q = queue

    def run(self):
        with open('results.csv', 'w') as f:
            while True:
                try:
                    elapsed, latency, num_users = self.q.get(False)
                    f.write('%.3f,%.3f,%s\n' % (elapsed, latency, num_users))
                    f.flush()
                    print '%.3f %s' % (latency, num_users)
                except Queue.Empty:
                    time.sleep(1)


if __name__ == '__main__':
    main()
