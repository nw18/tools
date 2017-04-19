import urllib
import time
import os
from HttpConfig import *

res_bad_request = "HTTP/1.1 500 Bad Requect\r\n\r\n"
res_not_found = "HTTP/1.1 404 Not Found\r\nContent-type: text/html\r\n\r\n";
res_ok = "HTTP/1.1 200 OK\r\nContent-type: text/html\r\n\r\n"
res_file_ok = "HTTP/1.1 200 OK\r\nContent-Length: {0}\r\nContent-type: {1}\r\n\r\n"
res_redirect = "HTTP/1.1 302 Temporarily Moved\r\nLocation: {0}\r\n\r\n"
res_text_ok = "HTTP/1.1 200 OK\r\nContent-type:text/plain\r\nContent-Length: {0}\r\n\r\n{1}"
res_text_ok_cookie = "HTTP/1.1 200 OK\r\nSet-Cookie: auid={2}; path=/;\r\nContent-type:text/plain\r\nContent-Length: {0}\r\n\r\n{1}"


def send_head(path, conn):
    file_size = os.path.getsize(w2l(path))
    pos = path.rfind(".")
    file_ext = ""
    mime_name = mime_map[file_ext]
    if pos > 0:
        file_ext = path[pos + 1:].lower()
    if file_ext in mime_map:
        mime_name = mime_map[file_ext]
    conn.send(res_file_ok.format(file_size, mime_name))
    return file_size


def send_file(path, conn):
    file_size = send_head(path, conn)
    f = open(w2l(path), "rb")
    print ("sending file", "({1}):{0}".format(path, file_size))
    sz_read = 0
    try:
        while True:
            data = f.read(1024 * 4)
            if not data:
                break
            sz_read += len(data)
            conn.send(data)
    except Exception, e:
        print ("Exception:", e)
    finally:
        print ("Result:", "send file({1}):{0}:".format(path, sz_read))
        f.close()


def list_dir(path, conn):
    lpath = w2l(path)
    ppath = path
    pos = ppath.rfind("/")
    out_list = []
    out_list2 = []
    if pos >= 0:
        ppath = ppath[0:pos]
        if ppath == "":
            ppath = "/"
        out_list.append(
            ['<a href="' + urllib.quote(ppath) + '">[Parent Directory]</a>', '[Size]', '[Create]', '[Modify]'])
    if not path.endswith("/"):
        path += "/"
    if not lpath.endswith("/"):
        lpath += "/"
    try:
        dir_list = os.listdir(lpath)
    except Exception, e:
        dir_list = []
    for sp in sorted(dir_list):
        lsp = lpath + sp
        # skip the hiden files.
        if sp.startswith("."):
            continue
        try:
            file_ctime = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(os.path.getctime(lsp)))
            file_mtime = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(os.path.getmtime(lsp)))
            if os.path.isdir(lsp):
                file_size = ''
            else:
                file_size = str(os.path.getsize(lsp))
            file_info = ['<a href="' + urllib.quote(path + sp) + '">' + sp + '</a>', file_size, file_ctime, file_mtime]
            if file_size == "":
                out_list.append(file_info)
            else:
                out_list2.append(file_info)
        except Exception, e:
            continue
    out_list.extend(out_list2)
    for i in range(0, len(out_list)):
        out_list[i] = '&nbsp;&nbsp;</td><td>'.join(out_list[i])
    result_page = config["page_template"].format('</td></tr><tr><td>'.join(out_list))
    conn.send(res_file_ok.format(len(result_page), "text/html"))
    conn.send(result_page)


def do_get(path, conn, heads, params):
    lpath = w2l(path)
    if not os.path.exists(lpath):
        print ("bad path:", "{0} {1}".format(path, lpath))
        conn.send(res_bad_request)
        return
    if os.path.isdir(lpath):
        list_dir(path, conn)
    else:
        send_file(path, conn)


def http_proc(conn):
    try:
        while True:
            head_lines = conn.recv()
            if len(head_lines) < 3:
                break
            head_method = head_lines[0].split(" ")
            head_properties = {}
            for line in head_lines[1:]:
                if line == "":
                    break
                kv = line.split(":")
                head_properties[kv[0].lower()] = kv[1].strip()
            path = urllib.unquote(head_method[1])
            if path.find('/../') >= 0:
                print ("bad path:", path)
                conn.send(res_bad_request)
                break
            if head_method[0] != "GET":
                conn.send(res_bad_request)
                break
            params = {}
            pos = path.find("?")
            if pos > 0:
                for param_pair in path[pos + 1:].split("&"):
                    param_pair = param_pair.split("=")
                    if len(param_pair) != 2 or param_pair[0] == "":
                        print ("bad get parameter:", str(param_pair))
                        conn.send(res_bad_request)
                        raise Exception("bad get query string")
                    params[param_pair[0]] = param_pair[1]
                path = path[0:pos]
            do_get(path, conn, head_properties, params)
            if head_properties["connection"] == "close" or ("force_short" in config and config["force_short"]):
                break
    except Exception, e:
        print ("http_proc Exception: ", e)
    finally:
        conn.close()

