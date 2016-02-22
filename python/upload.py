import sys
from ftplib import FTP
import time
import os

root = "~/home_book/"
cur_dir = ""
file_list = []
port = 21
is_debug = False
address = "localhost"
user_name = "Anonymous"
password = ""
is_recover = False
for arg in sys.argv[1:]:
    if arg.startswith("type:"):
        if arg[5:] == "android" or arg[5:] == "ios":
            cur_dir = root + arg[5:] + "/"
        else:
            print "bad parmeter1:",arg
            exit(-1)
    elif arg.startswith("file:"):
        fp = arg[5:]
        if not os.path.exists(fp):
            print "bad file:",fp
            exit(-1)
        if os.path.isfile(fp):
            file_list.append(fp)
        elif os.path.isdir(fp):
            for fn in os.listdir(fp):
                file_list.append(fp + "/" + fn)
    elif arg.startswith("port:"):
        port = int(arg[5:])
    elif arg.startswith("user:"):
        user_name = arg[5:]
    elif arg.startswith("pass:"):
        password = arg[5:]
    elif arg.startswith("addr:"):
        address = arg[5:]
    elif arg == "debug":
        is_debug = True
    elif arg == "recover":
        is_recover = True
    else:
        print "bad parameter:",arg
        exit(-1)
if "" == cur_dir or len(file_list) == 0:
    print("should set type and file.")
    exit(-1)

def gettoday():
    return time.strftime("%Y%m%d",time.localtime(time.time()))

def ftpconnect(host, username, password):
    ftp = FTP()
    if is_debug:
        ftp.set_debuglevel(2)
    ftp.connect(host, port)
    ftp.login(username, password)
    return ftp

def downloadfile(ftp, remotepath, localpath):
    bufsize = 1024
    fp = open(localpath,'wb')
    ftp.retrbinary('RETR ' + remotepath, fp.write, bufsize)
#ftp.set_debuglevel(0)
    fp.close()

def ensuredir(ftp,dir_name):
    if dir_name not in ftp.nlst():
        ftp.mkd(dir_name)

def uploadfile(ftp, remotepath, localpath):
    bufsize = 1024
    fp = open(localpath, 'rb')
    ftp.storbinary('STOR '+ remotepath , fp, bufsize)
#ftp.set_debuglevel(0)
    fp.close()

ftp = ftpconnect(address,user_name,password)
ftp.cwd(cur_dir)
ensuredir(ftp,gettoday())
ftp.cwd(gettoday())
rmt_file_list = ftp.nlst()
for file_path in file_list:
    file_name = file_path[file_path.rfind("/")+1:]
    if file_name in rmt_file_list:
        if is_recover:
            ftp.delete(file_name)
        else:
            continue
    uploadfile(ftp,file_name,file_path)
ftp.quit()