import pickle;
import Share;
import pprint as pp;

class ShareManager:
    loaded_database = None;
    
    # Create sample data
    def create_new_db_file(self):
        print("create new db called")
        temp_db_object = {}
        sample1 = Share.Share("asdf")
        temp_db_object["demo"] = sample1
        return temp_db_object
        
    def save_db(self, db):
        pickle.dump(db, open('pico_share.db', 'wb'))
    
    def __init__(self,filename='pico_share.db'):    
        try :
            print(filename)
            loaded_database = pickle.load(open(filename,'rb'))
            pp.pprint(loaded_database['demo'].__key)
        # no file was loaded, so create new
        except Exception:
            pp.pprint(Exception.message)            
            loaded_database = self.create_new_db_file()
            self.save_db(loaded_database)
            
a = ShareManager()