import os
import sys
import socket
import thread
from SocketServer import *

class FtpConfig:
    RootPath = "."
    AuthConfig = "./auth.config"
    @staticmethod
    def loadConfig():
        

class FtpSesstion(StreamRequestHandler):

    def __init__(self):
        self.user_name = ""
        self.root_path = ""
        pass
    def handle(self):

        pass


def main():
    print FtpConfig.RootPath , FtpConfig.AuthConfig

if __name__ == "__main__":
    main()