#a simple http procxy
import sys
import socket
config = {"ip":"0.0.0.0","port":3128,"max_conn":16,"parent":"","server":""}

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
if __name__ == "__main__":
	main()
