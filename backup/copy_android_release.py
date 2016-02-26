import os
import sys
import xml.dom.minidom
import time
reload(sys);
if len(sys.argv) != 1 and len(sys.argv) != 2:
    exit(-1)
def gettoday():
    return time.strftime("%Y%m%d",time.localtime(time.time()))

sys.setdefaultencoding("utf8")
dom=xml.dom.minidom.parse("AndroidManifest.xml")
root = dom.documentElement
str_num = root.getAttribute("android:versionName")
str_path = "~/upload_tmp_android_release"
if not os.path.exists(str_path):
    os.system("mkdir " + str_path)
os.system("chmod 775 " + str_path)
os.system("rm -rf " + str_path + "/*")
if len(sys.argv) == 2:
    str_num += "-" + sys.argv[1]
if os.path.exists("./bin/YJHomework-release.apk"):
    os.system("cp ./bin/YJHomework-release.apk " + str_path + "/YJHomework-" + str_num + ".apk")
elif os.path.exists("./bin/YJHomework-debug.apk"):
    os.system("cp ./bin/YJHomework-debug.apk " + str_path + "/YJHomework-" + str_num + ".apk")
os.system("chmod 775 " + str_path + "/YJHomework-" + str_num + ".apk")
if os.path.exists("./bin/proguard/mapping.txt"):
    os.system("cp ./bin/proguard/mapping.txt " + str_path + "/mapping-" + str_num + ".txt")
    os.system("chmod 775 " + str_path + "/mapping-" + str_num + ".txt")
exit(0)
