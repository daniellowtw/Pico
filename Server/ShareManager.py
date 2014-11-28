import pickle
import Share
import pprint as pp
import logging

logging.basicConfig(filename='example.log', level=logging.DEBUG)


class ShareManager:
    loaded_database = None

    # Create sample data
    def create_new_db_file(self):
        logging.info("create new db called")
        temp_db_object = {}
        sample1 = Share.Share("asdf")
        temp_db_object["demo"] = sample1
        return temp_db_object

    def save_db(self, db):
        pickle.dump(db, open('pico_share.db', 'wb'))

    def __init__(self, filename='pico_share.db'):
        try:
            logging.info("trying to load" + filename)
            loaded_database = pickle.load(open(filename, 'rb'))
            pp.pprint(loaded_database['demo'].__key)
        # no file was loaded, so create new
        except Exception:
            logging.error(Exception.message)
            logging.warning("cannot load exiting db, creating one instead")
            loaded_database = self.create_new_db_file()
            self.save_db(loaded_database)


a = ShareManager()
