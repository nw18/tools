import os
import sys
import socket
import threading,time,signal
import ConfigParser
from SocketServer import *

class FtpApp(threading.Thread):
    TheFtpApp = None
    def __init__(self):
        threading.Thread.__init__(self)
        TheFtpApp = self
    def start(self):
        if os.path.isfile(AuthConfig):
            print "config file not exists " , AuthConfig
            return False
        try:
            cf = ConfigParser.ConfigParser()
            cf.read(AuthConfig)
            self.ip = cf.get("ftp","ip")
            self.port = cf.getint("ftp","port")
            self.root = cf.get("ftp","root")
            self.user_name = cf.get("user","name")
            self.pass_word = cf.get("user","password")
            self.server = SocketServer.ThreadingTCPServer((ip,port),FtpSesstion)
            threading.Thread.start(self)
        except Exception as e:
            print e
        else:
            pass
        finally:
            pass

    def run(self):
        self.server.serve_forever()

    def stop(self):
        pass
    @staticmethod
    def DebugQuit(num,frame):
        TheFtpApp.stop()


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