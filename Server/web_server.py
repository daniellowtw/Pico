# -*- coding: utf-8 -*-
"""
@author: Daniel
"""

from twisted.web.resource import Resource, NoResource
from twisted.web.static import File
import logging



class MyResourceWrapper(Resource):

    """This is a wrapper class around Resource so that any intialisation will 
    require both a share_manager and active_sessions
    """

    def __init__(self, share_manager, active_sessions):
        Resource.__init__(self)
        self._share_manager = share_manager
        self._active_sessions = active_sessions


class SharedValueRes(Resource):

    """A class used for checking if a key exist"""

    def __init__(self, index):
        Resource.__init__(self)
        self.index = index

    def render_GET(self, request):
        return str(self._share_manager.get(self.index))


class GetAllKeys(MyResourceWrapper):

    """A class representing the page that displays all the Pico paired with the server"""

    def render_GET(self, request):
        return str(self._share_manager)


class DeleteKey(MyResourceWrapper):

    """A class representing the page that handles the deletion of Pico data"""

    def render_POST(self, request):
        try:
            revocation_key = request.args["revKey"][0]
            if self._share_manager.revoke_share(revocation_key):
                return "Key removed successfully."
            else:
                return "Error, key is not valid."
        except KeyError:
            return ""

class RequestRevKey(MyResourceWrapper):

    def render_GET(self, request):
        challenge = self._active_sessions.create_new_session()
        if (challenge):
            f = open('front/update.html')
            return f.read().replace("[[OTP_CHALLENGE]]", str(challenge))
        else:
            return "Server is too busy. Try again later"

    def render_POST(self, request):
        try:
            if (request.args["otp_challenge"][0].isdigit() and
            request.args["otp_response"][0].isdigit()):
                rev_key = self._active_sessions.verify_otp_response(
                        int(request.args["otp_challenge"][0]),
                        int(request.args["otp_response"][0]))
                if (rev_key):
                    return open('front/rev_key.html').read().replace("[[REVOCATION KEY]]", "This is your key:" + rev_key)
                else:
                    return self.render_GET(request)
            else:
                return self.render_GET(request)
        except KeyError:
            return self.render_GET(request)


class APIPage(MyResourceWrapper):

    def getChild(self, name, request):
        # For dev only
        if name == 'all':
            return GetAllKeys(self._share_manager, self._active_sessions)
        if name == 'delete':
            return DeleteKey(self._share_manager, self._active_sessions)
        else:
            return NoResource()

    def render_GET(self, request):
        # Don't want people to visit this page
        return NoResource()




class ServicesPage(Resource):

    def __init__(self, share_manager, active_sessions):
        root = File('front')
        js = File('front/js/')
        css = File('front/css/')                
        self._share_manager = share_manager
        self._active_sessions = active_sessions
        Resource.__init__(self)
        self.putChild("", root)
        self.putChild(
            "api", APIPage(self._share_manager, self._active_sessions))
        self.putChild(
            "request", RequestRevKey(self._share_manager, self._active_sessions))
        self.putChild("js", js)
        self.putChild("css", css)

    def getChild(self, id, request):
        logging.info(id + " Requested")
        # for dev only
        if self._share_manager.get_share(id):
            return SharedValueRes(id)
        else:
            return NoResource()
