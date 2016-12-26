from  multiprocessing import Pool
import socket
def Foo(i):
    try:
        sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM,socket.IPPROTO_TCP)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        sock.bind(("",80))
        sock.listen(1)
        sock.accept()
        sock.close()
        return(i, sock.getsockname())
    except Exception,e:
        return (i,e)
def Bar(i):
    print(i)
if __name__ == '__main__':
    pool = Pool(10)
    for i in range(10):
        pool.apply_async(func=Foo, args=(i,), callback=Bar)
    pool.close()
    pool.join()
    pool.terminate()
