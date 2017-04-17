import sys
import os.path

config = {"ip": "127.0.0.1", "port": 8080, "root": ".", "max_count": 128, "mime": "./mime.conf"};
mime_map = {"": "application/octet-stream"}


def load_mime():
    if not os.path.exists(config["mime"]):
        return
    f = open(config["mime"], "r")
    for line in f:
        fields = line.split(":")
        if len(fields) != 2 or fields[1] == "":
            break
        mime_map[fields[0]] = fields[1].strip(" \r\n")
    f.close()


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

def w2l(path):
    return config["root"] + path