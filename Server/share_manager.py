import pickle
import Share
import pprint as pp
import logging

logging.basicConfig(filename='example.log', level=logging.DEBUG)


class ShareManager:
    _loaded_database = None

    # Create sample data
    def create_new_db_file(self):
        logging.info("create new db called")
        temp_db_object = {}
        sample1 = Share.Share("asdf")
        temp_db_object["demo"] = sample1
        self._loaded_database = temp_db_object
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

    def get_database(self):
        return self._loaded_database

    def get_share(self, id):
        if (self._loaded_database.has_key(id)):
            return self._loaded_database.get(id)
        else:
            return None

    def save_db(self):
        pickle.dump(self._loaded_database, open('pico_share.db', 'wb'))

    def __str__(self):
        res = ""
        for index, share in self._loaded_database.iteritems():
            res += (str(index) + "\n\n" + str(share) + "\n\n\n")
        return res

    def __init__(self, filename='pico_share.db'):
        try:
            logging.info("trying to load" + filename)
            self._loaded_database = pickle.load(open(filename, 'rb'))
            print "loaded db"
        # no file was loaded, so create new
        except Exception:
            logging.error(Exception.message)
            logging.warning("cannot load exiting db, creating one instead")
            self.create_new_db_file()
            self.save_db()


a = ShareManager()
