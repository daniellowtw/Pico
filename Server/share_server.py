# -*- coding: utf-8 -*-
"""
@author: Daniel
"""
from twisted.internet.protocol import Protocol, Factory
from twisted.python import log


class ShareServer(Protocol):

    # Pass in factory so we can use factory for storage
    def __init__(self, share_manager):
        self._share_manager = share_manager

    # Welcome message. To check that we have indeed connected to the server.
    # To remove in future
    def connectionMade(self):
#        log.msg('connectionMade')
        self.transport.write("Welcome to pico\r\n")

    def dataReceived(self, data):
#        log.msg('data received', data)
        data = data.strip().split(']')
        if data[0] == "key":
            # key is in data[1]
#            log.msg('key asked', data[1])
            share = self._share_manager.get_share(data[1])
            if (share != None):
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
        else:
            self.transport.write("Unrecognised message:" + data)
        self.transport.loseConnection()

    def connectionLost(self, reason):
#        log.msg('connectionLost', reason)
        pass


class ShareServerFactory(Factory):

    _share_manager = None

    def __init__(self, share_manager=None):
        if (share_manager):
            self._share_manager = share_manager
        else:
            log.err("No shared database passed in, using default instead")
            exit()

    def buildProtocol(self, addr):
        return ShareServer(self._share_manager)
