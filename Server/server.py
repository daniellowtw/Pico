from twisted.internet import reactor, ssl
from twisted.python import log
from twisted.python.modules import getModule
from twisted.web.server import Site

import conf
from web_server import ServicesPage
from share_server import ShareServerFactory
from share_manager import ShareManager

def main(db = None):
    log.startLogging(open(conf.LOG_FILE, 'w'))
    _share_manager = ShareManager()

    certData = getModule(__name__).filePath.sibling(
            conf.SERVER_PEM_FILE).getContent()
    certificate = ssl.PrivateCertificate.loadPEM(certData)
    reactor.listenSSL(
            conf.CLIENT_PORT_SSL, ShareServerFactory(_share_manager), certificate.options())
#    Listen to non ssl connection for backwards compatibility
    reactor.listenTCP(conf.CLIENT_PORT_NON_SSL, ShareServerFactory(_share_manager))
#     UI and api server
    reactor.listenTCP(conf.WEB_PORT,Site(ServicesPage(_share_manager)))
    print ("server is running")
    reactor.run()


if __name__ == '__main__':
    main()