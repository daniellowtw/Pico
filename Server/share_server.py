# -*- coding: utf-8 -*-
"""
@author: Daniel
"""
from twisted.internet.protocol import Protocol, Factory
from twisted.python import log


class ShareServer(Protocol):

    # Pass in factory so we can use factory for storage
    def __init__(self, factory):
        self.factory = factory

    # Welcome message. To check that we have indeed connected to the server.
    # To remove in future
    def connectionMade(self):
        log.msg('connectionMade')
        self.transport.write("Welcome to pico\r\n")

    def dataReceived(self, data):
        log.msg('data received', data)
        if data.startswith("key"):
            temp = data.strip().split(']')
            # key is in temp[1]
            log.msg('key asked', temp[1])
            if (temp[1] in self.factory.shared_value):
                # key is in db
                self.transport.write(
                    "secret key has been asked " +
                    str(self.factory.shared_value[temp[1]][1]) + " times.\r\n")
            else:
                # key not in db
                self.transport.write("Revoked/Non-existent key")
        elif data.startswith("get"):
            # add key to db
            # data is of form add]index]key
            temp = data.strip().split(']')
            # store key and statistics
            self.factory.shared_value[temp[1]][1] += 1
            self.transport.write(self.factory.shared_value[temp[1]][0])
        elif data.startswith("add"):
            # add key to db
            # data is of form add]index]key
            temp = data.strip().split(']')
            # store key and statistics
            self.factory.shared_value[temp[1]] = [temp[2], 0]
            self.transport.write("secret key stored")
        else:
            self.transport.write("Unrecognised message:" + data)
        self.transport.loseConnection()

    def connectionLost(self, reason):
        log.msg('connectionLost', reason)


class ShareServerFactory(Factory):

    def __init__(self, shared_value=None):
        if (shared_value):
            self.shared_value = shared_value
        else:
            log.err("No shared database passed in, using default instead")
            self.shared_value = {'demo': ['demosecret', 0]}

    def buildProtocol(self, addr):
        return ShareServer(self)
