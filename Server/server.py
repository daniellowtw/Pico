from twisted.internet import reactor, ssl
from twisted.python import log
from twisted.python.modules import getModule
from twisted.web.server import Site

from web_server import ServicesPage
from share_server import ShareServerFactory
from share_manager import ShareManager
from conf import Conf
from session_manager import SessionManager

class PicoServer:

    """This class contains initialisation code for starting the servers. When
    initialising the servers classes, the reference to the ShareManager is
    passed along to the two server objects so that they can both update the
    global state.
    """

    def __init__(self, conf):
        self.conf = Conf()

    def start(self):
        # log.startLogging(open(self.conf.LOG_FILE, 'w'))
        _share_manager = ShareManager(self.conf.SERVER_DB_FILE)
        _active_sessions = SessionManager()
        cert_data = getModule(__name__).filePath.sibling(
            self.conf.SERVER_PEM_FILE).getContent()
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
            Site(ServicesPage(_share_manager, _active_sessions)))
        reactor.run()


def main():
    conf = Conf()
    pico_server = PicoServer(conf)
    pico_server.start()

if __name__ == '__main__':
    main()
