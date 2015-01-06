from twisted.internet import reactor, ssl
from twisted.python import log
from twisted.python.modules import getModule
from twisted.web.server import Site

import conf
from web_server import ServicesPage
from share_server import ShareServerFactory

def main():
    log.startLogging(open(conf.LOG_FILE, 'w'))
    
#    TODO: Load persistent share data
    db = {'demo': ['demosecret', 0]}    
    
    certData = getModule(__name__).filePath.sibling(
            conf.SERVER_PEM_FILE).getContent()
    certificate = ssl.PrivateCertificate.loadPEM(certData)
    reactor.listenSSL(
            conf.CLIENT_PORT_SSL, ShareServerFactory(db), certificate.options())
#    Listen to non ssl connection for backwards compatibility
    reactor.listenTCP(conf.CLIENT_PORT_NON_SSL, ShareServerFactory())
#     UI and api server
    reactor.listenTCP(conf.WEB_PORT,Site(ServicesPage(db)))
    print "server is running"
    reactor.run()


if __name__ == '__main__':
    main()