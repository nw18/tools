import os
import sys
import socket
import thread
import urllib
import time
import platform
import codebase_2 as codebase
import select
#config files
mime_config = "./mime.conf"
fpem = './server.pem'
#parameters [port:8080] [root:/home] [ip:192.169.0.1] https
bind_ip = "0.0.0.0"
bind_port = 80
root_path = ""
is_https = False
page_code = "utf-8"
#global variant
mime_map = {"":"application/octet-stream"}
cur_lock = thread.allocate_lock()
#response string
res_bad_request = "HTTP/1.1 500 Bad Requect\r\n\r\n"
res_not_found = "HTTP/1.1 404 Not Found\r\nContent-type: text/html\r\n\r\n"; 
res_ok = "HTTP/1.1 200 OK\r\nContent-type: text/html\r\n\r\n"
res_file_ok = "HTTP/1.1 200 OK\r\nContent-type: {1}\r\nContent-Length: {0}\r\n\r\n"
res_redirect = "HTTP/1.1 301 Moved Permanently\r\nContent-type: text/html\r\nLocation: {0}\r\n\r\n"
#page template
page_template = '<html><head><meta http-equiv="Content-Type" \
content="text/html; charset=' + page_code + '"/><title>TinyServer</title>\
<style>a{{font-size:12pt}}</style></head><body><table><tr><td>{0}</td></tr></table></body></html>'
def pase_param():
    global is_https,bind_port,bind_ip,root_path,is_https
    is_exit = False
    for arg in sys.argv[1:]:
        if arg.startswith("root:"):
            root_path = arg[5:]
        elif arg.startswith("port:"):
            bind_port = int(arg[5:])
        elif arg.startswith("ip:"):
            bind_ip = arg[3:]
        elif arg == "exit":
            is_exit = True
        elif arg == "https":
            if not os.path.exists(fpem):
                print "not exists file:",fpem
                exit(-1)
            is_https = True
        else:
            print "bad parameter:",arg
            exit(-1)
    if is_exit:
        post_exit()
        exit(0)
def init_code():
    global page_code
    if sys.getdefaultencoding() == 'ascii':
        page_code = 'gb2312'
    else:
        page_code = sys.getdefaultencoding()
def load_mime():
    global mime_map
    if not os.path.exists(mime_config):
        return
    f = open(mime_config,"r")
    for line in f:
        fields = line.split(":")
        if len(fields) != 2 or fields[1] == "":
            break
        mime_map[fields[0]] = fields[1]
    f.close()
def w2l(path):
    global root_path
    return root_path + path
def send_file(path,conn):
    file_size = os.path.getsize(w2l(path))
    pos = path.rfind(".")
    file_ext = ""
    mime_name = "application/octet-stream"
    if pos > 0:
        file_ext = path[pos+1:]
    if file_ext in mime_map:
        mime_name = mime_map[file_ext]
    conn.send(res_file_ok.format(file_size,mime_map))
    f = open(w2l(path),"rb")
    print path,file_size
    sz_read = 0
    try:
        while True:
            data = f.read(1024)
            if not data:
                break
            sz_read += len(data)
            conn.send(data)
    except Exception, e:
        print e
    finally:
        print "sume read:",sz_read
        f.close()

def list_dir(path,conn):
    ppath = path
    pos = ppath.rfind("/")
    out_list = []
    out_list2 = []
    if pos >= 0:
        ppath = ppath[0:pos]
        if ppath == "":
            ppath = "/"
        out_list.append(['<a href="' + urllib.quote(ppath)+ '">[Parent Directory]</a>','[Size]','[Create]','[Modify]'])
    if not path.endswith("/"):
        path += "/"
    try:
        dir_list = os.listdir(w2l(path))
    except Exception , e:
        dir_list = []
    for sp in sorted(dir_list):
        lsp = w2l(path + sp)
        try:
            file_ctime = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(os.path.getctime(lsp)))
            file_mtime = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(os.path.getmtime(lsp)))
            if os.path.isdir(lsp):
                file_size = ''
            else:
                file_size = str(os.path.getsize(lsp))
            file_info = ['<a href="' + urllib.quote(path + sp)+ '">' +sp+ '</a>',file_size,file_ctime,file_mtime]
            if file_size == "":
                out_list.append(file_info)
            else:
                out_list2.append(file_info)
        except Exception , e:
            continue
    out_list.extend(out_list2)
    for i in range(0,len(out_list)):
        out_list[i] = '&nbsp;&nbsp;</td><td>'.join(out_list[i])
    conn.send(res_ok)
    conn.send(page_template.format('</td></tr><tr><td>'.join(out_list)))
              
def  do_get(path,conn):
    if os.path.isdir(w2l(path)):
        list_dir(path,conn)
    else:
        send_file(path,conn)

def http_proc(conn,addr):
    req = conn.recv(16*1024)
    head_lines = req.split("\r\n")
    if len(head_lines) < 3:
        conn.close()
        return
    head_method = head_lines[0].split(" ")
    head_properties = {}
    for line in head_lines[1:]:
        if line == "":
            break
        kv = line.split(":")
        head_properties[kv[0]] = kv[1]
    path = urllib.unquote(head_method[1])
    if path.find('/../') >=0 :
        print "bad path:",path
        conn.send(res_bad_request)
        conn.close()
        return
    if head_method[0] != "GET" or not os.path.exists(w2l(path)):
        print "bad method or path:" , head_method[0] ,path,w2l(path)
        conn.send(res_bad_request)
        conn.close()
        return
    try:
        do_get(path,conn)
    except Exception, e:
        print e
    finally:
        conn.close()
def post_exit():
    s = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
    s.sendto("exit",("127.0.0.1",bind_port))
    s.close()
def main():
    global root_path,bind_ip,bind_port
    pase_param()
    init_code()
    load_mime()
    tp = codebase.ThreadPool(http_proc,128)
    if is_https:
        from OpenSSL import SSL
        ctx = SSL.Context(SSL.SSLv23_METHOD)
        ctx.use_privatekey_file (fpem)
        ctx.use_certificate_file(fpem)
        server_sock = SSL.Connection(ctx,socket.socket(socket.AF_INET,socket.SOCK_STREAM))
    else:
        server_sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    server_sock.bind((bind_ip,bind_port))
    signal_sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
    signal_sock.bind((bind_ip,bind_port))
    server_sock.listen(5)
    def do_exit():
        print "about to exit."
        server_sock.close()
        signal_sock.close()
        tp.fini()
        print "bye."
        exit(0)
    while True:
        try:
            rlist,wlist,elist = select.select([server_sock,signal_sock],[],[server_sock,signal_sock])
        except Exception , e:
            print e
            break
        if len(rlist) > 0:
            if server_sock in rlist:
                tp.push(server_sock.accept())
            else:
                data,addr = signal_sock.recvfrom(4*1024)
                if data == "exit" and addr[0] == "127.0.0.1":
                    do_exit()
        if len(elist) > 0:
            print "server error:",server_sock in elist,"signal error:",signal_sock in elist
            do_exit()
            
main()
