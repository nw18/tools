import os
import sys
import socket
import thread
import urllib
import time
import platform
import codebase_2 as codebase
import select
bind_ip = "0.0.0.0"
bind_port = 80
root_path = ""
cur_lock = thread.allocate_lock()
res_bad_request = "HTTP/1.1 500 Bad Requect\r\n\r\n"
res_not_found = "HTTP/1.1 404 Not Found\r\nContent-type: text/html\r\n\r\n"; 
res_ok = "HTTP/1.1 200 OK\r\nContent-type: text/html\r\n\r\n"
res_file_ok = "HTTP/1.1 200 OK\r\nContent-type: */*\r\nContent-Length: {0}\r\n\r\n"
res_redirect = "HTTP/1.1 301 Moved Permanently\r\nContent-type: text/html\r\nLocation: {0}\r\n\r\n"
if sys.getdefaultencoding() == 'ascii':
    page_code = 'gb2312'
else:
    page_code = sys.getdefaultencoding()
page_template = '<html><head><meta http-equiv="Content-Type" \
content="text/html; charset=' + page_code + '"/><title>TinyServer</title>\
<style>a{{font-size:12pt}}</style></head><body><table><tr><td>{0}</td></tr></table></body></html>';
def w2l(path):
    global root_path
    return root_path + path
def send_file(path,conn):
    file_size = os.path.getsize(w2l(path))
    conn.send(res_file_ok.format(file_size))
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
        out_list.append(['<a href="' + urllib.quote(ppath)+ '">[Parent Directory]</a>','[Size]','[Create Time]','[Modify Time]'])
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
        except Exception , e:
            continue
        out_list2.append(['<a href="' + urllib.quote(path + sp)+ '">' +sp+ '</a>',file_size,file_ctime,file_mtime])
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
        else:
            print "bad parameter:",arg
            exit(-1)
    if is_exit:
        post_exit()
        exit(0)
    tp = codebase.ThreadPool(http_proc,128)
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
