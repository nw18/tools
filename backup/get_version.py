import sys
import xml.dom.minidom
reload(sys);
sys.setdefaultencoding("utf8")
dom=xml.dom.minidom.parse("iYJ/Info.plist")
nodes = dom.getElementsByTagName("key")
for i in range(0,len(nodes)):
    node = nodes[i]
    if node.firstChild.nodeValue == "CFBundleShortVersionString":
        verCodeNode = node
        while verCodeNode.nodeName != "string":
            verCodeNode = verCodeNode.nextSibling
        sys.stdout.write(verCodeNode.firstChild.nodeValue)
exit(0)
