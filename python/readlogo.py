f = open("D:\\MyProgram\\tools\\java\\TinyServer\\favicona.ico","rb")
data = f.read()
ss = ""
for d in data:
    if d >= 128 :
        ss += str(d - 256)
    else:
        ss += str(d)
    ss += ","
f.close()
f = open("D:\\MyProgram\\tools\\java\\TinyServer\\code.txt","w+")
print(ss,file=f)
f.close()
