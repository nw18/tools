import sys
import xml.dom.minidom
reload(sys);
sys.setdefaultencoding("utf8")
channel_map = {}
use_last = False
if len(sys.argv) > 1:
    for channel_pair in sys.argv[1:]:
        if "noinc" == channel_pair:
            use_last = True
            continue
        channel_kv_ary = channel_pair.split(":")
        channel_map[channel_kv_ary[0]] = channel_kv_ary[1]
def change(code):
    dom=xml.dom.minidom.parse("iYJ/Info.plist")
    nodes = dom.getElementsByTagName("key")
    for i in range(0,len(nodes)):
        node = nodes[i]
        if node.firstChild.nodeValue == "CFBundleShortVersionString":
            verNode = node
            while verNode.nodeName != "string":
                verNode = verNode.nextSibling
        elif node.firstChild.nodeValue == "CFBundleVersion":
            verCodeNode = node
            while verCodeNode.nodeName != "string":
                verCodeNode = verCodeNode.nextSibling
    
    num = verCodeNode.firstChild.nodeValue
    verCodeNode.firstChild.nodeValue = str(int(num) / 1000 * 1000 + code)
    str_num = verNode.firstChild.nodeValue
    str_nums = str_num.split(".")
    nlast = len(str_nums) - 1
    str_nums[nlast] = str(code)
    str_num = ".".join(str_nums)
    verNode.firstChild.nodeValue = str_num
    f = open("iYJ/Info.plist","w+")
    dom.writexml(f)
    f.close()
code = -1
try:
    f = open("last_code","r")
    code = int(f.readline())
    f.close()
except Exception as e:
    if 'f' in dir():
        f.close

if not use_last:
    code += 1
    if code >= 1000:
        code = 0
elif code < 0:
    code = 0
    
try:
    #print code
    change(code)
except Exception as e:
    print e
    exit(-1)

try:
    f = open("last_code","w+")
    print >> f , code
    f.close()
except Exception as e:
    if 'f' in dir():
        f.close
    exit(-1)
exit(0)

