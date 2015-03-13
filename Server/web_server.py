# -*- coding: utf-8 -*-
"""
@author: Daniel
"""

from twisted.web.resource import Resource, NoResource
from twisted.web.static import File
import logging


root = File('front/')

_share_manager_global = None

# For dev only, currently unused.
# Read value from db
class SharedValueRes(Resource):
    def __init__(self, index):
        Resource.__init__(self)
        self.index = index
        
    def render_GET(self, request):
        return str(_share_manager_global.get_share(self.index))
        
# For dev only
class GetAllKeys(Resource):
    def render_GET(self, request):
        global _share_manager_global
        return str(_share_manager_global)
        
class DeleteKey(Resource):
    def __init__(self):            
        Resource.__init__(self)   
        
    def render_POST(self, request):
        x = request.args["revKey"][0]
        if _share_manager_global.get_share(x) == None:
            logging.warning("Someone tried to delete " + x)
            return "Error, key is not valid."
        else:
            _share_manager_global.delete_share(x)
            return "Key removed successfully."
            
    def render_GET(self, request):
        logging.warning("Someone tried to view the delete page")
        # Don't want people to visit this page
        return NoResource()

class APIPage(Resource):
    def __init__(self):            
        Resource.__init__(self)
        self.putChild("all", GetAllKeys())
        self.putChild("delete", DeleteKey())
            
    def render_GET(self, request):
        # Don't want people to visit this page
        return NoResource()

class ServicesPage(Resource):
    _share_manager = None
    
    def __init__(self, share_manager = None):
        global _share_manager_global
        if (share_manager!=None):
            _share_manager_global = share_manager
            self._share_manager = share_manager
            print("share manager loaded")
        else:
            print("no share manager")
            _share_manager_global = {}
            
        Resource.__init__(self)
        self.putChild("", root)
        self.putChild("api", APIPage())
        
    def getChild(self, id, request):
        logging.info(id + " Requested")
        # for dev only
        if self._share_manager.get_share(id):
            return SharedValueRes(id)
        else:
            return root