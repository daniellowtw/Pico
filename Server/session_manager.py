from collections import defaultdict
import os
import time


class SessionManager:

    def __init__(self):
        self._sessions = defaultdict(int)
        self.ENTROPY_IN_BYTES = 2
        self.SESSION_LIMIT = 2 ** (self.ENTROPY_IN_BYTES * 8)
        self.TIME_TO_LIVE = 60

    def create_new_session(self):
        candidate = int(os.urandom(2).encode('hex'), 16)
        while (self._sessions.has_key(candidate)):
            self.clean_up()
            if (len(self._sessions) == self.SESSION_LIMIT):
                return None
            candidate = int(os.urandom(2).encode('hex'), 16)
        self._sessions[candidate] = {'time_created': time.time()}
        return candidate

    def has_session(self, session_id):
        return self._sessions.has_key(int(session_id))

    def is_session_valid(self, session_id):
        if (self.has_session(session_id)):
            return (time.time() - self._sessions[session_id]['time_created']
                    < self.TIME_TO_LIVE)
        else:
            return False

    def add_otp_response_and_key(self, challenge, response, key):
        """Used by the share server to create response code and store the key.
        This checks if the session token has expired and return whether the 
        operation succeeded or not.
        """
        if (self.has_session(challenge)):
            self._sessions[challenge]['response'] = response
            self._sessions[challenge]['key'] = key
            return True
        else:
            return False

    def verify_otp_response(self, challenge, response):
        """Verify the challenge and response. If it matches, the temporary key
        is returned and the session is deleted otherwise return None
        """
        if (self._sessions[challenge] and
                self._sessions[challenge].has_key('response') and
                self._sessions[challenge]['response'] == response):
            return self._sessions[challenge]['key']
        return None

    def clean_up(self):
        for session_id in self._sessions:
            if (not self.is_session_valid(session_id)):
                del self._sessions[session_id]
