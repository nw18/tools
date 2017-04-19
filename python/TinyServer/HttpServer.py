import SocketServer
import HttpSession
import threading
from HttpConfig import  *

load_sys_argv()

class HttpServer:
    def setupServer(self):
        self.server_instance = SocketServer.ThreadingTCPServer((config["ip"],config["port"]),HttpSession.HttpSession)
        self.server_instance.serve_forever(0.5)
        self.server_instance.server_close()

    def stopServer(self):
        def closeSelf(self):
            self.server_instance.shutdown()
        #close may block the signal.
        threading._start_new_thread(closeSelf,(self,))
