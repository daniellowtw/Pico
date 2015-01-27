# -*- coding: utf-8 -*-
"""
@author: Daniel
"""

from twisted.web.resource import Resource, NoResource
from twisted.web.static import File

root = File('front/')

# TODO: Use sharemanager class as db
#sharedValue = {'demo':['demosecret',0]}
_share_manager_global = None

# For dev only, currently unused.
# Read value from db
class SharedValueRes(Resource):
    def __init__(self, index):
        Resource.__init__(self)
        self.index = index
        
    def render_GET(self, request):
        return str(_share_manager_global.get(self.index))
        
# For dev only
class GetAllKeys(Resource):
    def render_GET(self, request):
        global _share_manager_global
        return str(_share_manager_global)
        
class DeleteKey(Resource):
    def render_POST(self, request):
        x = request.args["revKey"][0]
        if _share_manager_global.get_share(x) == None:
            return "Error, key is not valid."
        else:
            _share_manager_global.delete_share(x)
            return "Key removed successfully."

class APIPage(Resource):
    def getChild(self, name, request):
        # TODO : change to putchild
        # For dev only
        if name == 'all':
            return GetAllKeys()
        if name == 'delete':
            return DeleteKey()
        else:
            return NoResource()
            
    def render_GET(self, request):
        # Don't want people to visit this page
        return ""

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
        # TODO: make this default instead of ui path
        self.putChild("ui", root)
        self.putChild("api", APIPage())
        
    def getChild(self, id, request):
        # for dev only
        if self._share_manager.get_share(id):
            return SharedValueRes(id)
        else:
            return NoResource()