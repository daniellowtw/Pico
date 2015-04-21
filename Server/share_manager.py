import pickle
import Share
import pprint as pp
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
        sample = Share.Share("The demo secret")
        temp_db_object["demo"] = sample
        self._loaded_database = temp_db_object
        self._pico_lookup = {}
        self.save_db()

    def add_share(self, id, key):
        temp_share = Share.Share(key)
        self._loaded_database[id] = temp_share
        self.save_db()

    def delete_share(self, id):
        if (self._loaded_database.has_key(id)):
            del self._loaded_database[id]
            self.save_db()
        else:
            logging.error("trying to delete a share that does not exist")

    def revoke_share(self, rev_key):
        if self._pico_lookup.has_key(rev_key):
            id = self._pico_lookup[rev_key]
            del self._loaded_database[id]
            del self._pico_lookup[rev_key]
            return True
        else:
            return False

    def get_database(self):
        return self._loaded_database
        
    def get_revocation_key(self, id):
        """Retrieve the revocation key of the given Pico ID.
        """
        share = self.get_share(id)
        if share:
            return share.get_rev()
        else:
            return None
        
        
    def create_revocation_key(self, id):
        """If the user authenticates the request from the web UI and a
        revocation key is not yet associated with this id, create one and
        return the OTP challenge response.
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
        if (self._loaded_database.has_key(id)):
            return self._loaded_database.get(id)
        else:
            return None

    def save_db(self):
        combined_database = {'loaded_db':self._loaded_database, 'pico_lookup':self._pico_lookup}
        pickle.dump(combined_database, open(self._filename, 'wb'))
        
    def load_db(self, filename):
        """Tries to load db from file. Might throw exception"""
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

