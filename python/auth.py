import os
import random
import md5
import sys
import getpass
import re
import time
import threading
auth_map={}
auth_id_map={}
auth_last_check_time=time.time()
auth_lock = threading.Lock()
auth_file="auth.conf"
CHECK_PEIROD = 5
TIMEOUT_TIME = 20 * 60
def gen_code():
	code = ""
	for i in range(0,32):
		code += "{0:02X}".format(random.randint(0,255))
	return code
def add_id(id):
	global auth_id_map
	auth_lock.acquire()
	auth_id_map[id] = time.time()
	auth_lock.release()
def check_id(id):
	global auth_id_map,auth_last_check_time
	del_list = []
	cur_time = time.time()
	auth_lock.acquire()
	if cur_time - auth_last_check_time > CHECK_PEIROD:
		for key in auth_id_map:
			if cur_time - auth_id_map[key] >= TIMEOUT_TIME:
				del_list.append(key)
		auth_last_check_time = cur_time
	for key in del_list:
		auth_id_map.pop(key)
	res = False
	if id in auth_id_map:
		auth_id_map[id] = cur_time
		res = True
	auth_lock.release()
	return res
def help():
	print "like:"
	print "python auth.py add [name]"
	print "python auth.py rmv [name]"
	print "python auth.py clear"
	print "python auth.py list"
def do_load():
	f = open(auth_file,"r")
	for line in f:
		fields = line.strip("\r\n").split(":")
		auth_map[fields[0]] = fields[1]
	f.close()
def do_clear():
	f = open(auth_file,"w+")
	f.close()
def do_list():
	f = open(auth_file,"r")
	for line in f:
		print line,
	f.close()
def do_add(user):
	pwd1 = getpass.getpass('password:')
	pwd2 = getpass.getpass('again:')
	if pwd1 != pwd2:
		print "not match."
		return
	lines = []
	m = md5.new()
	m.update(user+":"+pwd1)
	is_replace = False
	try:
		f = open(auth_file,"r")
		for line in f:
			if line == "":
				continue
			if line.startswith(user+":"):
				line = user+":"+m.hexdigest()
				is_replace = True
			lines.append(line.strip("\r\n"))
		f.close()
	except Exception ,e:
		pass
	if not is_replace:
		line = user+":"+m.hexdigest()
		lines.append(line.strip("\r\n"))
	f = open(auth_file,"w+")
	for line in lines:
		f.write(line + "\n")
	f.close()
def do_rmv(user):
	lines = []
	try:
		f = open(auth_file,"r")
		for line in f:
			if line != "" and not line.startswith(user+":"):
				lines.append(line.strip("\r\n"))
		f.close()
		f = open(auth_file,"w+")
		for line in lines:
			f.write(line + "\n")
		f.close()
	except Exception,e:
		pass
def do_auth(code,user,sum_md5_code):
	if user not in auth_map:
		print "user:",user,"does not exists."
		return False
	m = md5.new()
	m.update(code + auth_map[user])
	return m.hexdigest() == sum_md5_code
def is_well_user_name(user):
	if re.match(r"[\d|\w]+$",user):
		return True
	return False
if __name__ == "__main__":
	opt = ""
	user = ""
	if len(sys.argv) == 3:
		opt = sys.argv[1]
		user = sys.argv[2]
	elif len(sys.argv) == 2:
		opt = sys.argv[1]
	else:
		help()
		exit(0)
	if opt == "add":
		if not is_well_user_name(user):
			print "bad user name,be charactor and number only."
			exit(-1)
		do_add(user)
	elif opt == "rmv": 
		if not is_well_user_name(user):
			print "bad user name,be charactor and number only."
			exit(-1)
		do_rmv(user)
	elif opt == "clear" and user == "":
		do_clear()
	elif opt == "list" and user == "":
		do_list()
	else:
		help()
else:
	do_load()