import os
import sys
import socket
import thread,time,signal
from SocketServer import *

class FtpConfig:
    RootPath = "."
    AuthConfig = "./auth.config"
    @staticmethod
    def loadConfig():
        pass
        

class FtpSesstion(StreamRequestHandler):

    def __init__(self):
        self.user_name = ""
        self.root_path = ""
        pass
    def handle(self):

        pass
class FtpApp:
    
    def start(self):
        pass
    def stop(self,force = False):
        pass
    def run():
        pass
    @staticmethod
    def debug_quit(signum, frame):
        print "server to be close by ",signum,frame,"."
        exit(-1)

def main():
    signal.signal(signal.SIGINT,FtpApp.debug_quit)
    signal.signal(signal.SIGTERM,FtpApp.debug_quit)
    while True:
        print "run"
        time.sleep(1)

if __name__ == "__main__":
    main()