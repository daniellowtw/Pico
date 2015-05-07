from twisted.internet import reactor, ssl
from twisted.web.server import Site

from web_server import ServicesPage
from share_server import ShareServerFactory
from share_manager import ShareManager
from conf import Conf
from session_manager import SessionManager

import getpass

class MySite(Site):
    def getResourceFor(self, request):
        request.setHeader('X-Frame-Options', 'DENY')
        request.setHeader('X-XSS-Protection', '1')
        request.setHeader('X-Content-Type-Options','nosniff')
        request.setHeader('Server', 'None')
        return Site.getResourceFor(self, request)

class PicoServer:

    """This class contains initialisation code for starting the servers. When
    initialising the servers classes, the reference to the ShareManager is
    passed along to the two server objects so that they can both update the
    global state.
    """

    def __init__(self, conf, db_passphrase):
        self.conf = Conf()
        self.db_passphrase = db_passphrase

    def start(self):
        # log.startLogging(open(self.conf.LOG_FILE, 'w'))
        _share_manager = ShareManager(self.conf.SERVER_DB_FILE, self.db_passphrase)
        _active_sessions = SessionManager()
        cert_data = open(self.conf.SERVER_PEM_FILE).read()
        certificate = ssl.PrivateCertificate.loadPEM(cert_data)
        # Create endpoint for Share server
        reactor.listenSSL(
            self.conf.CLIENT_PORT_SSL,
            ShareServerFactory(_share_manager, _active_sessions),
            certificate.options())
        # Listen to non ssl connection for backwards compatibility
        reactor.listenTCP(
            self.conf.CLIENT_PORT_NON_SSL,
            ShareServerFactory(_share_manager, _active_sessions))
        # Create endpoint for UI web server
        reactor.listenTCP(
            self.conf.WEB_PORT,
            MySite(ServicesPage(_share_manager, _active_sessions)))
        # Create TLS endpoing for UI web server
        reactor.listenSSL(
            self.conf.WEB_PORT_SSL,
            MySite(ServicesPage(_share_manager, _active_sessions)),
            certificate.options())
        reactor.run()


def main():
    passphrase = getpass.getpass("Enter database password: ")
    conf = Conf()
    pico_server = PicoServer(conf, passphrase)
    pico_server.start()

if __name__ == '__main__':
    main()
