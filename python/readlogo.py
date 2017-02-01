f = open("D:\\MyProgram\\tools\\java\\TinyServer\\logo.zip","rb")
data = f.read()
ss = ""
for d in data:
    if d >= 128 :
        ss += str(d - 256)
    else:
        ss += str(d)
    ss += ","
f.close()
f = open("D:\\MyProgram\\tools\\java\\TinyServer\\logo.txt","w+")
print(ss,file=f)
f.close()
