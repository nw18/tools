import sys
import time
import math
m = int(sys.argv[1])
n = int(sys.argv[2])
res_map = {}
cal_count = 0
def cal(m,n):
    global cal_count
    if n > 1:
        if res_map.__contains__(n):
            return res_map[n]
        r1 = cal(m,int(n/2))
        if n%2 == 0:
            r2 = r1 
        else:
            r2 = r1 * m
            print(n-int(n/2),time.clock(),cal_count)
        res = r1 * r2
        res_map[n] = res
        cal_count = cal_count + 1
        print(n,time.clock(),cal_count)
        return res
    else:
        return m
print(time.clock(),hex(cal(m,n)))