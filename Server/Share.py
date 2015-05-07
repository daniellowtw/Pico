class Share:
    """
    Models a Pico server share
    """
    
    
    _server_share = None
    _id = None
    _auth = None
    _log = None
    _disabling_key = None
    _enabling_key = None
    _greeting = None
    _count = None
    _disabled_flag = False

    def __init__(self, id, server_share, auth):
        self._server_share = server_share
        self._auth = auth
        self._count = 0
        self._id = id

    def __str__(self):
        # Used for debugging. Disable for production.
        return ("id: " + str(self._id) + "\n"
        + "Server share: " + str(self._server_share) + "\n"
        + "Disabling key: " + str(self._disabling_key) + "\n"
        + "Enabling key: " + str(self._enabling_key) + "\n"
#        + "greeting: " + str(self._greeting) + "\n"
        + "count: " + str(self._count) + "\n")

    def get_server_share(self):
        self.increment_count()
        return self._server_share
    
    def get_count(self):
        return self._count

    def increment_count(self):
        self._count += 1
        
    def get_disabling_key(self):
        return self._disabling_key
        
    def set_disabling_key(self, key):
        self._disabling_key = key
        
    def get_enabling_key(self):
        return self._enabling_key
        
    def set_enabling_key(self, key):
        self._enabling_key = key

    def disable(self):
        self._disabling_key = None
        self._disabled_flag = True
        
    def enable(self):
        self._enabling_key = None
        self._disabled_flag = False