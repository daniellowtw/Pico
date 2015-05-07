# -*- coding: utf-8 -*-
"""
@author: Daniel
"""
from twisted.internet.protocol import Protocol, Factory
from twisted.python import log

import os


class ShareServer(Protocol):

    """This method is the logic behind what happens when we have a transport
    channel between the Pico application and the server and when a message
    is received.
    """

    # Pass in factory so we can use factory for storage
    def __init__(self, share_manager, active_sessions):
        self._share_manager = share_manager
        self._active_sessions = active_sessions

    # Welcome message. To check that we have indeed connected to the server.
    def connectionMade(self):
        log.msg('connectionMade')
        self.transport.write("Welcome to pico\r\n")

    def dataReceived(self, data):
        """Decode the message and marshall the arguments"""
        log.msg('data received', data)
        data = data.strip().split(']')

        if data[0] == "get" and len(data) == 3:
            # data is of form add]id]auth
            if self._share_manager.authenticate(data[1], data[2]):
                share = self._share_manager.get_share(data[1])
                if (share):
                    # key is in db
                    self.transport.write(share.get_server_share())
                else:
                    # key not in db
                    self.transport.write("Revoked/Non-existent key")
            else:
                self.transport.write("Rejected")

        elif data[0] == "add" and len(data) == 3:
            # add key to db
            # data is of form add]id]server_share
            auth_key = self._share_manager.add_share(data[1], data[2])
            self.transport.write(auth_key)

        elif data[0] == "request" and len(data) == 4 and data[3].isdigit():
            session_id = int(data[3])
            # data is of the form request]id]auth]otp_challenge
            if self._active_sessions.is_session_valid(session_id):
                if self._share_manager.authenticate(data[1], data[2]):
                    keys = self._share_manager.create_revocation_keys(data[1])
                    if (keys):
                        otp_response = int(os.urandom(2).encode('hex'), 16)
                        if (self._active_sessions.add_otp_response_and_keys(
                                session_id,
                                otp_response,
                                keys)):
                            self.transport.write("%05d" % otp_response)
                        else:
                            self.transport.write("Error")
                    else:
                        self.transport.write(
                            "Key has already been given out.")
                else:
                    # An invalid authentication code!
                    self.transport.write("Rejected")
            else:
                self.transport.write("Invalid/Expired OTP challenge.")
        else:
            self.transport.write("Unrecognised message:" + str(data))
        self.transport.loseConnection()

    def connectionLost(self, reason):
        log.msg('connectionLost', reason)


class ShareServerFactory(Factory):

    """A factory that creates instances of the ShareServerProtocol
    This also keeps track of shared references of ShareManager and Sessions
    among the instances.
    """

    _share_manager = None
    _active_sessions = None

    def __init__(self, share_manager, active_sessions):
        self._share_manager = share_manager
        self._active_sessions = active_sessions

    def buildProtocol(self, addr):
        return ShareServer(self._share_manager, self._active_sessions)
