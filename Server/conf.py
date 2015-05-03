# -*- coding: utf-8 -*-
"""
@author: Daniel
Just a file with parameters for the server to use.
"""


class Conf:
    # dev config
    """contains the parameters such as the ports to listen to, and the names of
    the files.
    """
    LOG_FILE = 'python_server.log'
    CURRENT_STATE = 'dev'
    WEB_PORT = 1235
    WEB_PORT_SSL = 8002
    CLIENT_PORT_SSL = 8001
    CLIENT_PORT_NON_SSL = 1234
    SERVER_PEM_FILE = 'keys/server.pem' # releative to cwd
    SERVER_DB_FILE = 'pico_share.db' # releative to cwd

        
