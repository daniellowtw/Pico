class Share:
    _secret = None
    _id = None
    _log = None
    _rev = None
    _greeting = None
    _count = None

    def __init__(self, id, secret):
        self._secret = secret
        self._count = 0
        self._id = id

    def __str__(self):
        return ("id: " + str(self._id) + "\n"
        + "secret: " + str(self._secret) + "\n"
        + "rev: " + str(self._rev) + "\n"
#        + "greeting: " + str(self._greeting) + "\n"
        + "count: " + str(self._count) + "\n")

    def get_secret(self):
        self.increment_count()
        return self._secret
    
    def get_count(self):
        return self._count

    def increment_count(self):
        self._count += 1
        
    def get_rev(self):
        return self._rev
        
    def set_rev(self, key):
        self._rev = key