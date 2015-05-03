# -*- coding: utf-8 -*-
"""
@author: Daniel
"""
from twisted.internet.protocol import Protocol, Factory
from twisted.internet import error
from twisted.python import log

import os


class ShareServer(Protocol):

    """This method is the logic behind what happens when we have a transport 
    channel between the Pico application and the server and when a message 
    is received.
    """

    # Pass in factory so we can use factory for storage
    def __init__(self, factory, share_manager, active_sessions):
        self._factory = factory
        self._share_manager = share_manager
        self._active_sessions = active_sessions

    # Welcome message. To check that we have indeed connected to the server.
    # To remove in future
    def connectionMade(self):
        self._factory.count += 1
#        log.msg('connectionMade')
        self.transport.write("Welcome to pico. " + str(self._factory.count) 
                            + "\r\n")

    def dataReceived(self, data):
        """Decode the message and marshall the arguments"""
        log.msg('data received', data)
        data = data.strip().split(']')
        if data[0] == "key":
            # key is in data[1]
#            log.msg('key asked', data[1])
            share = self._share_manager.get_share(data[1])
            if (share):
                # key is in db
                self.transport.write("secret key has been asked " +
                                     str(share.get_count()) + " times.\r\n")
            else:
                # key not in db
                self.transport.write("Revoked/Non-existent key")

        elif data[0] == "get":
            # data is of form add]index
            share = self._share_manager.get_share(data[1])
            if (share):
                # key is in db
                self.transport.write(share.get_secret())
            else:
                # key not in db
                self.transport.write("Revoked/Non-existent key")
        elif data[0] == "add":
            # add key to db
            # data is of form add]index]key
            self._share_manager.add_share(data[1], data[2])
            self.transport.write("secret key stored")
        elif data[0] == "request" and data[2].isdigit():
            session_id = int(data[2])
            # data is of the form request]id]otp_challenge
            if self._active_sessions.is_session_valid(session_id):
                key = self._share_manager.create_revocation_key(data[1])
                if (key):
                    otp_response = int(os.urandom(2).encode('hex'), 16)
                    if (self._active_sessions.add_otp_response_and_key(
                            session_id,
                            otp_response, key)):
                        self.transport.write("%05d" % otp_response)
                    else:
                        self.transport.write("Error")
                else:
                    self.transport.write("Too many requests. Try again later.")
            else:
                self.transport.write("Invalid/Expired OTP challenge.")
        else:
            self.transport.write("Unrecognised message:" + str(data))
        self.transport.loseConnection()

    def connectionLost(self, reason):
        if not reason.check(error.ConnectionClosed):
            print("bad")
        self._factory.count -= 1
#        log.msg('connectionLost', reason)
        pass


class ShareServerFactory(Factory):
    count = 0

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
        return ShareServer(self, self._share_manager, self._active_sessions)
