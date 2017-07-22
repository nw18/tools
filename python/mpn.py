import sys
import time
m = int(sys.argv[1])
n = int(sys.argv[2])
nx = []
while n > 0:
    nx.append(n%2)
    n = n >> 1
print(nx)
rx = [0 for i in nx]
rx[0] = m
for i in range(1,len(nx)):
    rx[i] = rx[i-1] * rx[i-1]
    print('cache',1<<i,time.clock())
res = 1
for i in range(0,len(nx)):
    if nx[i] != 0:
        res = res * rx[i]
        print('result',1<<i,time.clock())
print('get it!',time.clock())
f = open('res.txt','w+')
print(time.clock(),hex(res),file=f)
f.close()
