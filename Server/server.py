from twisted.internet import protocol, reactor
from twisted.python import log
from twisted.web.resource import Resource, NoResource
from twisted.web.server import Site
from twisted.web.static import File

root = File('front/')

# TODO: Use sharemanager class as db
sharedValue = {'demo':['demosecret',0]}

# For dev only, currently unused.
# Read value from db
class SharedValueRes(Resource):
    def __init__(self, index):
        Resource.__init__(self)
        self.index = index
        
    def render_GET(self, request):
        return str(sharedValue[self.index])
        
# For dev only
class GetAllKeys(Resource):
    def render_GET(self, request):
        return str(sharedValue)
        
class DeleteKey(Resource):
    def render_POST(self, request):
        x = request.args["revKey"][0]
        if x not in sharedValue:
            return "Error, key is not valid."
        del sharedValue[x]
        return "Key removed successfully."

class APIPage(Resource):
    def getChild(self, name, request):
        # TODO : change to putchild
        # For dev only
        if name == 'all':
            return GetAllKeys()
        if name == 'delete':
            return DeleteKey()
        else:
            return NoResource()
            
    def render_GET(self, request):
        # Don't want people to visit this page
        return ""

class ServicesPage(Resource):
    def __init__(self):
        Resource.__init__(self)
        # TODO: make this default instead of ui path
        self.putChild("ui", root)
        self.putChild("api", APIPage())
        
    def getChild(self, path, request):
        # for dev only
        if path in sharedValue:
            return SharedValueRes(path)
        else:
            return NoResource()

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
        if data.startswith("key"):
            temp = data.strip().split(']')
            # key is in temp[1]
            log.msg('key asked', temp[1])
            if (temp[1] in sharedValue):
                # key is in db
                sharedValue[temp[1]][1]+=1
                self.transport.write(
                    "secret key has been asked " +
                    str(sharedValue[temp[1]][1]) + " times.\r\n")
            else:
                # key not in db
                self.transport.write("Revoked/Non-existent key")
        elif data.startswith("add"):
            # add key to db
            # data is of form add]index]key
            temp = data.strip().split(']')
            # store key and statistics
            sharedValue[temp[1]]=[temp[2],0]
            self.transport.write("secret key stored")
        else:
            self.transport.write("Unrecognised message:" + data)
        self.transport.loseConnection()

    def connectionLost(self, reason):
        log.msg('connectionLost', reason)


class EchoFactory(protocol.Factory):
    # deprecated
    # number_of_request_for_share = 0

    def buildProtocol(self, addr):
        return Echo(self)


log.startLogging(open('log', 'w'))
# android ping server
reactor.listenTCP(1234,EchoFactory())
# UI and api server
reactor.listenTCP(1235,Site(ServicesPage()))
print "server is running"
reactor.run()
