# -*- coding: utf-8 -*-

import sys
from twisted.internet import ssl, protocol, task, defer, endpoints
from twisted.python import log
from twisted.python.modules import getModule

class Echo(protocol.Protocol):

    # Pass in factory so we can use factory for storage
    def __init__(self, factory):
        self.factory = factory

    def connectionMade(self):
        log.msg('connectionMade')
        self.transport.write("Welcome to pico\r\n")

    def dataReceived(self, data):
        log.msg('data received', data)
        self.transport.write("this is what you wrote:" + data)

    def connectionLost(self, reason):
        log.msg('connectionLost', reason)


class EchoFactory(protocol.Factory):
    def buildProtocol(self, addr):
        return Echo(self)

def main(reactor, choice):
    log.startLogging(sys.stdout)
    log.msg("choice is " + str(choice))
    if (choice== 1):
        log.msg("TLS option selected")
        certData = getModule(__name__).filePath.sibling('server.pem').getContent()
        certificate = ssl.PrivateCertificate.loadPEM(certData)
        reactor.listenSSL(8001, EchoFactory(), certificate.options())
    else :
        if (choice == 2):
            log.msg("non-tls option selected")
        else:
            log.msg("Unknown option, default to non-tls")        
        reactor.listenTCP(8001,EchoFactory())
        
#    If the above doesn't work try the following
#    reactor.listenSSL(8000, EchoFactory(), ssl.DefaultOpenSSLContextFactory('server_decoded.key', 'server.crt'))    
    
    return defer.Deferred()
    
    
if __name__ == '__main__':
    x = input("(1) for TLS \n(2) for non-TLS")
    import server
    task.react(server.main, (x,))