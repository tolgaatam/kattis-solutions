i = input
x = int
while(r:=i())!="0 0":
    [N,M]=map(x,r.split())
    print(N+M-len(set(x(i()) for _ in range(N+M))))