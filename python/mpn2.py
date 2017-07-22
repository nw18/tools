import time
import sys
m = int(sys.argv[1])
n = int(sys.argv[2])
r = m
step = int(m / 100)
for i in range(1,n):
    r = r * m
    if i % step ==0:
        print(i,time.clock())
print('get it!',time.clock())
f = open('res2.txt','w+')
print(time.clock(),hex(r),file=f)
f.close()
