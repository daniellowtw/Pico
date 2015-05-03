import pickle
from share import Share
import logging
import base64
import os

logging.basicConfig(filename='example.log', level=logging.DEBUG)

class ShareManager:
    _loaded_database = None
    _pico_lookup = None

    # Create sample data
    def create_new_db_file(self):
        """Create the necessary database files and preload a default share"""
        logging.info("create new db called")
        temp_db_object = {}
        sample = Share('demo', "The demo secret")
        temp_db_object["demo"] = sample
        self._loaded_database = temp_db_object
        self._pico_lookup = {}
        self.save_db()

    def add_share(self, id, server_share):
        """Create a new share with the given Pico ID and server share."""
        temp_share = Share(id, server_share)
        self._loaded_database[id] = temp_share
        self.save_db()


#    def delete_share(self, id):
#        """Deprecated. Use revoke_share instead. Keeping for debugging purposes
#        """
#        if (self._loaded_database.has_key(id)):
#            del self._loaded_database[id]
#            self.save_db()
#        else:
#            logging.error("trying to delete a share that does not exist")

    def revoke_share(self, rev_key):
        """If the rev_key is valid, delete the associated share. Returns
        whether the operation succeeds or not.
        """
        if self._pico_lookup.has_key(rev_key):
            id = self._pico_lookup[rev_key]
            del self._loaded_database[id]
            del self._pico_lookup[rev_key]
            self.save_db()
            return True
        else:
            return False

    def get_database(self):
        """Return the dictionary of shares."""
        return self._loaded_database
        
    def get_revocation_key(self, id):
        """Retrieve the revocation key of the given Pico ID.
        If there is no such share, return None.
        """
        share = self.get_share(id)
        if share:
            return share.get_rev()
        else:
            return None
        
        
    def create_revocation_key(self, id):
        """If the user authenticates the request from the web UI and a
        revocation key is not yet associated with this id, create one and
        return the OTP challenge response. If there is no such share, return
        None.
        """
        share = self.get_share(id)
        if share:
            if not share.get_rev():
                rev_key = base64.b64encode(os.urandom(32))
                share.set_rev(rev_key)
                self._pico_lookup[rev_key] = id
                self.save_db()
                return rev_key
            else:
                return None
        else:
            return None
                
    def get_share(self, id):
        """Return the share with the given id or return None if not found."""
        if (self._loaded_database.has_key(id)):
            return self._loaded_database.get(id)
        else:
            return None

    def save_db(self):
        """Saves the dictionaries to a file."""
        combined_database = {'loaded_db':self._loaded_database, 'pico_lookup':self._pico_lookup}
        pickle.dump(combined_database, open(self._filename, 'wb'))
        
    def load_db(self, filename):
        """Tries to load the share and lookup dictionaries. Throws an 
        exception if file does not exist.
        """
        combined_database = pickle.load(open(filename, 'rb'))
        self._loaded_database = combined_database['loaded_db']
        self._pico_lookup = combined_database['pico_lookup']

    def __str__(self):
        res = ""
        for index, share in self._loaded_database.iteritems():
            res += (str(index) + "\n\n" + str(share) + "\n\n\n")
        return res

    def __init__(self, filename):
        self._filename = filename
        try:
            logging.info("trying to load" + filename)
            self.load_db(filename)
            print "loaded db"
        # no file was loaded, so create new
        except Exception:
            logging.error(Exception.message)
            logging.warning("cannot load exiting db, creating one instead")
            self.create_new_db_file()
            self.save_db()

