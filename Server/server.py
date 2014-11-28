from twisted.internet import protocol, reactor, endpoints
from twisted.python import log


class Echo(protocol.Protocol):

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
        if data in ["key\r\n", "key", "key\n"]:
            # to be replaced in the future by another object that retrieve the
            # key
            self.factory.number_of_request_for_share += 1
            self.transport.write(
                "secret key has been asked " +
                str(self.factory.number_of_request_for_share) + " times.\r\n")
        else:
            self.transport.write("Unrecognised message:" + data)
        self.transport.loseConnection()

    def connectionLost(self, reason):
        log.msg('connectionLost', reason)


class EchoFactory(protocol.Factory):
    number_of_request_for_share = 0

    def buildProtocol(self, addr):
        return Echo(self)

log.startLogging(open('log', 'w'))
endpoints.serverFromString(reactor, "tcp:1234").listen(EchoFactory())
reactor.run()
