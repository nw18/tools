import os
import sys
import socket
import threading,time,signal
import ConfigParser
import SocketServer

class FtpApp:
    AuthConfig = "./ftp.conf"
    def __init__(self):
        pass
    def start(self):
        if not os.path.isfile(FtpApp.AuthConfig):
            print "config file not exists " , FtpApp.AuthConfig
            return False
        try:
            cf = ConfigParser.ConfigParser()
            cf.read(FtpApp.AuthConfig)
            self.ip = cf.get("ftp","ip")
            self.port = cf.getint("ftp","port")
            self.root = cf.get("ftp","root")
            self.writeable = cf.get("ftp","writeable") == "True"
            self.user_name = cf.get("user","name")
            self.pass_word = cf.get("user","password")
            self.server = SocketServer.ThreadingTCPServer((self.ip,self.port),FtpSesstion)
            #threading.Thread.start(self)
        except Exception as e:
            print e
            return False
        finally:
            return True

    def run(self):
        self.server.serve_forever()

    def stop(self):
        def do_stop(self):
            print "shutdown call."
            self.server.shutdown()
            print "shutdown called."
        threading._start_new_thread(do_stop,(self,))

    @staticmethod
    def DebugQuit(num,frame):
        print "about to close"
        theApp.stop()
    def isWirteAble(self):
        return self.writeable
    def changeDirectory(self,subDir):
        tar = self.root + subDir
        tar = tar.replace("//","/")
        tar = tar.replace("/../","/")
        return tar

class FtpSesstion(SocketServer.StreamRequestHandler):
    def __init__(self):
        self.user_name = ""
        self.root_path = ""
        self.app = FtpApp.TheFtpApp
        pass
    def handle(self):
        line = self.rfile.readline()
        print line
        self.request.close()

theApp = FtpApp()
def main():
    if theApp.start():
        signal.signal(signal.SIGINT,FtpApp.DebugQuit)
        signal.signal(signal.SIGTERM,FtpApp.DebugQuit)
        theApp.run()
    else:
        print "ftp app start fail."

if __name__ == "__main__":
    main()