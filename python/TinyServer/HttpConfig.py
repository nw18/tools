import sys
import os.path

config = {"ip": "127.0.0.1", "port": 8080, "root": ".", "max_count": 128, "mime": "./mime.conf"};
mime_map = {"": "application/octet-stream"}


def load_mime():
    global mime_map
    if not os.path.exists(config["mime"]):
        return
    f = open(config["mime"], "r")
    for line in f:
        fields = line.split(":")
        if len(fields) != 2 or fields[1] == "":
            print ("skip:",line)
            continue
        mime_map[fields[0]] = fields[1].strip(" \r\n")
    f.close()

def init_code():
    if sys.platform.startswith('win'):
        page_code = "gb2312"
    else:
        page_code = "utf-8"
    config["page_template"] = '<html>\
    <head>\
    <meta name="viewport" content="width=1024px, initial-scale=1" />\
    <meta http-equiv="Content-Type" content="text/html; charset=' + page_code + '"/>\
    <title>TinyServer</title>\
    <style>a{{font-size:100%}}</style></head>\
    <body><table><tr><td>{0}</td></tr></table></body></html>'
    for key in mime_map.keys():
        if mime_map[key] == "text/plain":
            mime_map[key] += ";charset=" + page_code

def load_sys_argv():
    for arg in sys.argv[1:]:
        pos = arg.find(":")
        if (pos > 0 and pos < len(arg)):
            key = arg[:pos]
            value = arg[pos + 1:]
            if (key not in config):
                print ("unknown:", key, config.keys())
                continue
            if type(config[key]) is int:
                config[key] = int(value)
            elif type(config[key]) is str:
                config[key] = value
            else:
                print ("unhandled arg type:", type(config[key]))
        else:
            print("bad parameter:", arg)
            exit(-1)
    load_mime()
    init_code()


def w2l(path):
    return config["root"] + path