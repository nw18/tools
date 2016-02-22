import os
import sys
import socket
import ftplib
import thread
import urllib
bind_ip = "0.0.0.0"
bind_port = 80
root_path = ""
max_conn_count = 1024
cur_conn_count = 0
cur_lock = thread.allocate_lock()
res_bad_request = "HTTP/1.1 500 Bad Requect\r\n\r\n"
res_not_found = "HTTP/1.1 404 Not Found\r\nContent-type: text/html\r\n\r\n"; 
res_ok = "HTTP/1.1 200 OK\r\nContent-type: text/html\r\n\r\n"
res_file_ok = "HTTP/1.1 200 OK\r\nContent-type: */*\r\nContent-Length: {0}\r\n\r\n"
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
    if pos >= 0:
        ppath = ppath[0:pos]
        if ppath == "":
            ppath = "/"
        conn.send('<a href="' + urllib.quote(ppath)+ '">[Parent Directory]</a><br/>')
    if not path.endswith("/"):
        path += "/"
    for sp in os.listdir(w2l(path)):
        conn.send('<a href="' + urllib.quote(path + sp)+ '">' +sp+ '</a><br/>')

def  do_get(path,conn):
    if os.path.isdir(w2l(path)):
        list_dir(path,conn)
    else:
        send_file(path,conn)

def http_proc(conn,addr):
    global max_conn_count 
    global cur_conn_count
    if cur_conn_count >= max_conn_count:
        conn.close()
        return
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
        print "bad method or path:" , head_method[0] ,path
        conn.send(res_bad_request)
        conn.close()
        return
    try:
        cur_lock.acquire()
        cur_conn_count+=1
        cur_lock.release()
        do_get(path,conn)
        conn.close()
    except Exception, e:
        print e
    finally:
        cur_lock.acquire()
        cur_conn_count-=1
        cur_lock.release()
        pass
def main():
    global bind_ip,bind_port,root_path
    for arg in sys.argv[1:]:
        if arg.startswith("root:"):
            root_path = arg[5:]
        elif arg.startswith("port:"):
            bind_port = int(arg[5:])
        elif arg.startswith("ip:"):
            bind_ip = arg[3:]
    server_sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    server_sock.bind((bind_ip,bind_port))
    server_sock.listen(5)
    while True:
        thread.start_new_thread(http_proc,server_sock.accept())
main()
