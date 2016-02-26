import os
import re
tmp_path = os.path.split(os.path.realpath(__file__))[0]+"/upload_tmp_ios"
man_path = os.path.split(os.path.realpath(__file__))[0]+"/manifest.plist"
def gen_plist(tar_path,ipa_name):
	fi = open(man_path,"r")
	fo = open(tar_path,"w+")
	file_name = ipa_name[0:-4]
	print tar_path
	for line in fi:
		if re.search(r"<string>http.*ipa</string>",line) is not None:
			line = re.sub(r'http.*ipa','https://git.oschina.net/bjyqj/ZuoYeBenURL/raw/master/'+ipa_name,line)
		elif re.search(r"<string>[a-zA-Z]{0,1}[0-9.]+</string>",line) is not None:
			print line,re.search(r'[a-zA-Z]{0,1}[0-9\.]+',file_name).group()
			line = re.sub(r'[a-zA-Z]{0,1}[0-9\.]+',re.search(r'[a-zA-Z]{0,1}[0-9\.]+',file_name).group(),line)
		fo.write(line)
	fo.close()
	fi.close()
print tmp_path,man_path
for path in os.listdir(tmp_path):
	if path.endswith(".ipa"):
		plist_path = tmp_path + "/" + path[0:-3] + "plist"
		gen_plist(plist_path,path)
		url = "https://git.oschina.net/bjyqj/ZuoYeBenURL/raw/master/"+ path[0:-3] + "plist"
		lnk_path = tmp_path + "/" + path[0:-3] + "html"
		os.system('echo "<a href=\'itms-services://?action=download-manifest&url=' + url + '\'><script type=\'text/javascript\'>window.location = document.getElementsByTagName(\'a\')[0].getAttribute(\'href\');</script>' + path + '</a>" >> ' + lnk_path)
