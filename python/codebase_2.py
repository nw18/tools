import sys
import threading
import time
class SafeList:
    def __init__(self,init_num):
        self._list = []
        self._lock = threading.Lock()
        self._signal = threading.Semaphore(init_num)

    def push(self,val,blocking = 1):
        if self._lock.acquire(blocking):
            self._list.append(val)
            self._signal.release()
            self._lock.release()
            return True
        return False

    def pop(self):
        self._signal.acquire()
        self._lock.acquire()
        val = self._list.pop(0)
        self._lock.release()
        return val
    
    @staticmethod
    def test():
        def test_proc(i,l):
            while True:
                val = l.pop()
                if type(val) is int:
                    print "i am "+ str(i) + " receive " + str(val) + "\n"
                elif type(val) is bool:
                    print "i am "+ str(i) + " bye.\n"
                    return
        l = SafeList(0)
        for i in range(0,10):
            threading._start_new_thread(test_proc,(i,l))
        for i in range(0,10):
            l.push(i)
        for i in range(0,10):
            l.push(False)

class ThreadPool:
    def __init__(self,fun,thread_num):
        self._fun = fun
        self._arg_list = SafeList(0)
        self._thread_num = thread_num
        for i in range(0,thread_num):
            threading._start_new_thread(ThreadPool._proxy_proc,(self._arg_list,self._fun))

    def fini(self):
        for i in range(0,self._thread_num):
            self._arg_list.push(False)
            
    def push(self,arg_list,blocking = 1):
        return self._arg_list.push(arg_list,blocking)
        
    @staticmethod
    def _proxy_proc(arg_list,fun):
        while True:
            arg = arg_list.pop()
            try:
                if type(arg) is list or type(arg) is tuple:
                    fstr = "fun("
                    if len(arg) > 0:
                        fstr += "arg[0]"
                    for i in range(1,len(arg)):
                        fstr += ",arg[" + str(i) + "]"
                    fstr += ")"
                    eval(fstr)
                elif type(arg) is not bool:
                    fun(arg)
                elif not arg:
                    return
            except Exception , e:
                print e
            
    @staticmethod
    def test():
        def test_proc(v):
            print threading.current_thread().ident,v
        tp = ThreadPool(test_proc,10)
        for i in range(0,20):
            tp.push([i])
        tp.fini()
        
#test case here.
if __name__ == '__main__' and len(sys.argv) == 2:
    print sys.argv[1] + ".test()"
    eval(sys.argv[1] + ".test()")
    time.sleep(1)
