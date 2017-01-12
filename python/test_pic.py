class Array:
    def __init__(self,w,h):
        self.w = w
        self.h = h
        self.a = [i for i in range(0,w*h)]
    def print_out(self):
        for y in range(0,self.h):
            for x in range(0,self.w):
                print "\t"+str(self.a[y*self.w+x]),
            print ""
    def reverse(self):
        w = self.w
        h = self.h
        
        t = self.w;
        self.w = self.h
        self.h = t

a = Array(10,7)
a.print_out()
a.reverse()
a.print_out()