import os
import sys
import socket
import thread
import urllib
import time
import platform
import codebase
import auth
import select
#config files
config = {}
config["mime"] = "./mime.conf"
config["pem_file"] = "./server.pem"
#parameters [port:8080] [root:/home] [ip:192.169.0.1] https
config["bind_ip"] = "0.0.0.0"
config["bind_port"] = 80
config["root_path"] = ""
config["is_https"] = False
config["auth"] = "./auth"
auth_spec_path = "/__auth__"#inner name of auth,use to warp auth dir or as a inner object.
#global variant
mime_map = {"":"application/octet-stream"}
#response string
res_bad_request = "HTTP/1.1 500 Bad Requect\r\n\r\n"
res_not_found = "HTTP/1.1 404 Not Found\r\nContent-type: text/html\r\n\r\n"; 
res_ok = "HTTP/1.1 200 OK\r\nContent-type: text/html\r\n\r\n"
res_file_ok = "HTTP/1.1 200 OK\r\nContent-Length: {0}\r\nContent-type: {1}\r\n\r\n"
res_redirect = "HTTP/1.1 302 Temporarily Moved\r\nLocation: {0}\r\n\r\n"
res_text_ok = "HTTP/1.1 200 OK\r\nContent-type:text/plain\r\nContent-Length: {0}\r\n\r\n{1}"
res_text_ok_cookie = "HTTP/1.1 200 OK\r\nSet-Cookie: auid={2}; path=/;\r\nContent-type:text/plain\r\nContent-Length: {0}\r\n\r\n{1}"
#page template
page_template = ""
def init_code():
    global page_template
    page_code = "utf-8"
    if sys.getdefaultencoding() == "ascii":
        page_code = "gb2312"
    else:
        page_code = sys.getdefaultencoding()
    page_template = '<html>\
	<head>\
	<meta http-equiv="Content-Type" content="text/html; charset=' + page_code + '"/>\
	<title>TinyServer</title>\
	<style>a{{font-size:12pt}}</style></head>\
	<body><table><tr><td>{0}</td></tr></table></body></html>'
def pase_param():
    global config
    is_exit = False
    for arg in sys.argv[1:]:
        if arg.startswith("root:"):
            config["root_path"] = arg[5:]
        elif arg.startswith("port:"):
            config["bind_port"] = int(arg[5:])
        elif arg.startswith("ip:"):
            config["bind_ip"] = arg[3:]
        elif arg.startswith("auth:"):
            auth_spec_path = arg[4:]
            if not os.path.isdir(config["auth"]):
                print "bad path:",config["auth"]
                exit(-1)
        elif arg == "exit":
            is_exit = True
        elif arg == "https":
            if not os.path.exists(config["pem_file"]):
                print "not exists file:",config["pem_file"]
                exit(-1)
            config["is_https"] = True
        else:
            print "bad parameter:",arg
            exit(-1)
    if is_exit:
        post_exit()
        exit(0)
def load_mime():
    global mime_map
    if not os.path.exists(config["mime"]):
        return
    f = open(config["mime"],"r")
    for line in f:
        fields = line.split(":")
        if len(fields) != 2 or fields[1] == "":
            break
        mime_map[fields[0]] = fields[1].strip(" \r\n")
    f.close()
def w2l(path):
    return config["root_path"] + path
def send_head(lpath,path,conn):
    file_size = os.path.getsize(lpath)
    pos = path.rfind(".")
    file_ext = ""
    mime_name = "application/octet-stream"
    if pos > 0:
        file_ext = path[pos+1:].lower()
    if file_ext in mime_map:
        mime_name = mime_map[file_ext]
    conn.send(res_file_ok.format(file_size,mime_name))
    return file_size
def send_file(lpath,path,conn):
    file_size = send_head(lpath,path,conn)
    f = open(lpath,"rb")
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

def list_dir(lpath,path,conn):
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
    if not lpath.endswith("/"):
        lpath += "/"
    try:
        dir_list = os.listdir(lpath)
    except Exception , e:
        dir_list = []
    for sp in sorted(dir_list):
        lsp = lpath + sp
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
def do_auth(path,conn,params):
    try:
        if len(path) == len(auth_spec_path):#do auth with md5.
            if params["method"] == "auth":
                if not ("id" in params and "user" in params and "md5" in params):
                    conn.send(res_bad_request)
                    return
                id_str = params["id"]
                user_name = params["user"]
                sum_md5 = params["md5"]
                if len(id_str) != 32 * 2 or user_name == "" or sum_md5 == "":
                    conn.send(res_bad_request)
                    return
                res_msg = ""
                if auth.do_auth(id_str,user_name,sum_md5):
                    res_msg = "ok"
                    auth.add_id(id_str)
                    conn.send(res_text_ok_cookie.format(len(res_msg),res_msg,id_str))
                else:
                    res_msg = "fail"
                    conn.send(res_text_ok.format(len(res_msg),res_msg))
            elif params["method"] == "login":#generate a auth id.
                code = auth.gen_code()
                print "login:",code
                conn.send(res_text_ok.format(len(code),code))
        else:
            lpath = config["auth"] + path[len(auth_spec_path):]
            send_file(lpath,path,conn)
    except Exception , e:
        print "do_auth:",e
    finally:
        conn.send(res_bad_request)
def do_get(path,conn,params,id):
    if path.startswith(auth_spec_path):
        do_auth(path,conn,params)
        return
    if not auth.check_id(id):
        addr = conn.getsockname()
        port = ":" + str(addr[1])
        if addr[1] == 80:
            port = ""
        if addr[0] == "0.0.0.0":
            host_addr = "127.0.0.1" + port
        else:
            host_addr = addr[0] + port
        prefix = "http://"
        if config["is_https"]:
            prefix = "https://"
        print "redirect:",addr,host_addr
        conn.send(res_redirect.format(prefix+ host_addr + auth_spec_path +"/login.html"))
        conn.close()
        return
    lpath = w2l(path)
    if not os.path.exists(lpath):
        print "bad path:",path,lpath
        conn.send(res_bad_request)
        return
    if os.path.isdir(lpath):
        list_dir(lpath,path,conn)
    else:
        send_file(lpath,path,conn)

def http_proc(conn,addr):
    try:
        req = conn.recv(16*1024)
        print req
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
            head_properties[kv[0].lower()] = kv[1].strip()
        cookie_id = ""
        if "cookie" in head_properties:
            for kv in head_properties["cookie"].split(";"):
                if kv.startswith("auid="):
                    cookie_id = kv[5:].strip()
                    print "cookie:",kv,cookie_id
                    break
        path = urllib.unquote(head_method[1])
        if path.find('/../') >=0 :
            print "bad path:",path
            conn.send(res_bad_request)
            conn.close()
            return
        if head_method[0] != "GET":
            conn.send(res_bad_request)
            conn.close()
            return
        params = {}
        pos = path.find("?")
        if pos > 0:
            for param_pair in path[pos+1:].split("&"):
                param_pair = param_pair.split("=")
                if len(param_pair) != 2 or param_pair[0] == "":
                    print "bad get parameter:",param_pair
                    conn.send(res_bad_request)
                    conn.close()
                    return
                params[param_pair[0]] = param_pair[1]
            path = path[0:pos]
        do_get(path,conn,params,cookie_id)
    except Exception, e:
        print e
    finally:
        conn.close()
def post_exit():
    s = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
    s.sendto("exit",("127.0.0.1",config["bind_port"]))
    s.close()
def main():
    pase_param()
    init_code()
    load_mime()
    tp = codebase.ThreadPool(http_proc,128)
    if config["is_https"]:
        from OpenSSL import SSL
        ctx = SSL.Context(SSL.SSLv23_METHOD)
        ctx.use_privatekey_file (config["pem_file"])
        ctx.use_certificate_file(config["pem_file"])
        server_sock = SSL.Connection(ctx,socket.socket(socket.AF_INET,socket.SOCK_STREAM))
    else:
        server_sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    server_sock.bind((config["bind_ip"],config["bind_port"]))
    signal_sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
    signal_sock.bind((config["bind_ip"],config["bind_port"]))
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
