p=input
t=int
f=float
S=t(p())
l=[[f(e) for e in p().split()] for _ in range(S)]
T=t(p())
for _ in range(T):
    [xa,ya,xb,yb]=map(f,p().split())
    sm=True
    for i in range(S):
        m=(l[i][3]-l[i][1])/(l[i][2]-l[i][0]) if l[i][2]!=l[i][0] else 2**20
        ao=ya-l[i][1]-m*(xa-l[i][0])
        bo=yb-l[i][1]-m*(xb-l[i][0])
        if(ao>=0)!=(bo>=0):
            sm=not sm
    print("same" if sm else "different")