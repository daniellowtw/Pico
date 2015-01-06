# -*- coding: utf-8 -*-
"""
@author: Daniel
"""

from twisted.web.resource import Resource, NoResource
from twisted.web.static import File

root = File('front/')

# TODO: Use sharemanager class as db
sharedValue = {'demo':['demosecret',0]}

# For dev only, currently unused.
# Read value from db
class SharedValueRes(Resource):
    def __init__(self, index):
        Resource.__init__(self)
        self.index = index
        
    def render_GET(self, request):
        return str(sharedValue[self.index])
        
# For dev only
class GetAllKeys(Resource):
    def render_GET(self, request):
        return str(sharedValue)
        
class DeleteKey(Resource):
    def render_POST(self, request):
        x = request.args["revKey"][0]
        if x not in sharedValue:
            return "Error, key is not valid."
        del sharedValue[x]
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
    def __init__(self, shared_value = None):
        if (shared_value!=None):
            global sharedValue
            sharedValue = shared_value
            
        Resource.__init__(self)
        # TODO: make this default instead of ui path
        self.putChild("ui", root)
        self.putChild("api", APIPage())
        
    def getChild(self, path, request):
        # for dev only
        if path in sharedValue:
            return SharedValueRes(path)
        else:
            return NoResource()