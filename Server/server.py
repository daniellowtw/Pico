from twisted.internet import reactor, ssl
from twisted.python import log
from twisted.python.modules import getModule
from twisted.web.server import Site

from web_server import ServicesPage
from share_server import ShareServerFactory
from share_manager import ShareManager


class Conf:

    """contains the parameters such as the ports to listen to, and the names of
    the files.
    """
    LOG_FILE = 'python_server.log'
    CURRENT_STATE = 'dev'
    WEB_PORT = 1235
    CLIENT_PORT_SSL = 8001
    CLIENT_PORT_NON_SSL = 1234
    SERVER_PEM_FILE = 'server.pem'
    SERVER_DB_FILE = 'pico_share.db'


class PicoServer:

    """This class contains initialisation code for starting the servers. When
    initialising the servers classes, the reference to the ShareManager is
    passed along to the two server objects so that they can both update the
    global state.
    """

    def __init__(self, conf):
        self.conf = Conf()

    def start(self):
        log.startLogging(open(self.conf.LOG_FILE, 'w'))
        _share_manager = ShareManager(self.conf.SERVER_DB_FILE)
        cert_data = getModule(__name__).filePath.sibling(
            self.conf.SERVER_PEM_FILE).getContent()
        certificate = ssl.PrivateCertificate.loadPEM(cert_data)
        # Create endpoint for Share server
        reactor.listenSSL(
            self.conf.CLIENT_PORT_SSL, ShareServerFactory(_share_manager),
            certificate.options())
        # Listen to non ssl connection for backwards compatibility
        reactor.listenTCP(
            self.conf.CLIENT_PORT_NON_SSL, ShareServerFactory(_share_manager))
        # Create endpoint for UI web server
        reactor.listenTCP(
            self.conf.WEB_PORT, Site(ServicesPage(_share_manager)))
        reactor.run()


def main():
    conf = Conf()
    pico_server = PicoServer(conf)
    pico_server.start()

if __name__ == '__main__':
    main()
