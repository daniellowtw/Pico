from twisted.internet import protocol, reactor, endpoints

class Echo(protocol.Protocol):
    
    # Pass in factory so we can use factory for storage
    def __init__(self, factory):
        self.factory = factory
        
    # Welcome message. To check that we have indeed connected to the server. To remove in future
    def connectionMade(self):
        self.transport.write("Welcome to pico\r\n")
        
    def dataReceived(self, data):
        print data
        if data in ["key\r\n","key","key\n"]:
            # to be replaced in the future by another object that retrieve the key
            self.factory.number_of_request_for_share += 1
            self.transport.write("secret key has been asked " + str(self.factory.number_of_request_for_share) + " times.\r\n")
        else:
            self.transport.write("Unrecognised message:" + data);

class EchoFactory(protocol.Factory):
    number_of_request_for_share = 0
    def buildProtocol(self, addr):
        return Echo(self)

endpoints.serverFromString(reactor, "tcp:1234").listen(EchoFactory())
reactor.run()
