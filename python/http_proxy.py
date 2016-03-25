#a simple http procxy
import sys
import socket

config = {"ip":"0.0.0.0","port":3128,"max_conn":16,"parent":"","server":"127.0.0.1:80"}

def main():
	global config
	for arg in sys.argv:
		arg_pair = arg.split(":")
		if len(arg_pair) != 2 or arg_pair[0] not in config:
			print "bad parameter",arg
			return
		name = arg_pair[0]
		value = arg_pair[1]
		if type(config[name]) is int:
			config[name] = int(value)
		else:
			config[name] = value
	if "parent" in config:
		address = config["server"].split(":")
		port = 80
		if len(address) == 2:
			port = int(address[1])
		run_as_middleman(address[0],port)
	else:
		run_as_proxy(config["port"])

def run_as_middleman(ip,port):
	pass
def run_as_proxy(port):
	pass

if __name__ == "__main__":
	main()
