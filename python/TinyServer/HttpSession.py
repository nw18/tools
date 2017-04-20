import SocketServer
from HttpResponse import *

class HttpSession(SocketServer.StreamRequestHandler):
    def setup(self):
        SocketServer.StreamRequestHandler.setup(self)

    def handle(self):
        http_procedure(self)
        print ("session close:", self.client_address)

    def send(self, value):
        self.wfile.write(value)

    def close(self):
        self.finish()

    def recv(self):
        data = []
        while 1:
            line = self.rfile.readline()
            if line == "" or line == "\r\n":
                break
            data.append(line)
        return data
