# -*- coding: utf-8 -*-
import sys
from twisted.internet import ssl, protocol, task, defer, reactor, endpoints
from twisted.python import log

class Echo(protocol.Protocol):
    # Pass in factory so we can use factory for storage
    def __init__(self, factory):
        self.factory = factory

    def connectionMade(self):
        log.msg('connectionMade')
        self.transport.write("Is this going through?")

    def dataReceived(self, data):
        log.msg('Server said', data)
        self.transport.loseConnection()

    def connectionLost(self, reason):
        log.msg('connectionLost', reason)


class EchoFactory(protocol.Factory):
    def buildProtocol(self, addr):
        return Echo(self)
        
    def clientConnectionFailed(self, connector, reason):
        print "Connection failed - goodbye!"
        reactor.stop()

    def clientConnectionLost(self, connector, reason):
        print "Connection lost - goodbye!"
        reactor.stop()
        
    def startedConnecting(self, connector):
        print "Started connecting"

def main(reactor):
    log.startLogging(sys.stdout)
#    factory = protocol.Factory.forProtocol(Echo)
    reactor.connectSSL("dlow.me", 8001, EchoFactory(), ssl.ClientContextFactory())

    return defer.Deferred()

if __name__ == '__main__':
    import client
    task.react(client.main)