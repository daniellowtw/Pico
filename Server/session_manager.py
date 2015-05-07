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
        """Create a new session with only ID and time_created."""
        # TODO: Create a session class instead
        candidate = int(os.urandom(2).encode('hex'), 16)
        while (self._sessions.has_key(candidate)):
            self.clean_up()
            if (len(self._sessions) == self.SESSION_LIMIT):
                return None
            candidate = int(os.urandom(2).encode('hex'), 16)
        self._sessions[candidate] = {'time_created': time.time()}
        return candidate

    def has_session(self, session_id):
        """Checks if a session with the given session_id exists."""
        return self._sessions.has_key(int(session_id))

    def is_session_valid(self, session_id):
        """Checks if a session with the given session_id is still valid
        The duration of the session is defined in self.TIME_TO_LIVE"""
        if (self.has_session(session_id)):
            return (time.time() - self._sessions[session_id]['time_created']
                    < self.TIME_TO_LIVE)
        else:
            return False

    def add_otp_response_and_keys(self, challenge, response, keys):
        """Used by the share server to create response code and store the keys.
        Check if the session token has expired and return whether the operation 
        succeeded or not.
        """
        if (self.has_session(challenge)):
            self._sessions[challenge]['response'] = response
            self._sessions[challenge]['key'] = keys
            return True
        else:
            return False

    def verify_otp_response(self, challenge, response):
        """Verify the challenge and response. If it matches, the temporary key
        is returned and the session is deleted otherwise return None.
        """
        if (self._sessions[challenge] and
                self._sessions[challenge].has_key('response') and
                self._sessions[challenge]['response'] == response):
            key = self._sessions[challenge]['key']
            del self._sessions[challenge]
            return key
        return None

    def clean_up(self):
        """Go through all available sessions and delete all sessions that are
        no longer valid."""
        keys = self._sessions.keys()
        for session_id in keys:
            if (not self.is_session_valid(session_id)):
                del self._sessions[session_id]
