# -*- coding: utf-8 -*-
"""
Created on Tue Jan 06 20:48:06 2015

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
    CLIENT_PORT_SSL = 8001
    CLIENT_PORT_NON_SSL = 1234
    SERVER_PEM_FILE = 'server.pem'
    SERVER_DB_FILE = 'pico_share.db'

        