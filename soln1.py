import sys
import random
input = sys.stdin.readline

t = int(input())
out = []
for _ in range(t):
    n, m = map(int, input().split())
    # random XOR hash of the set of existing detectors covering each spot
    D = [0] * (n + 2)
    for _ in range(m):
        l, r = map(int, input().split())
        v = random.getrandbits(62)
        D[l] ^= v
        D[r + 1] ^= v
    cls = [0] * (n + 2)
    ids = {}
    h = 0
    for p in range(1, n + 1):
        h ^= D[p]
        if h not in ids:
            ids[h] = len(ids)
        cls[p] = ids[h]

    # R[p] = farthest end of a block starting at p with no repeated class
    last = [n + 1] * len(ids)
    R = [0] * (n + 2)
    R[n + 1] = n
    for p in range(n, 0, -1):
        x = cls[p]
        R[p] = min(R[p + 1], last[x] - 1)
        last[x] = p
    nxt = [x + 1 for x in R]
    nxt[n + 1] = n + 1

    # c = minimum number of blocks
    c = 0
    p = 1
    while p <= n:
        p = nxt[p]
        c += 1

    starts = None
    if c >= 3 and c % 2 == 1:
        # try c blocks whose first and last block share no class -> (c-1)/2 detectors
        J = c - 2
        cur = list(range(n + 2))
        up = nxt[:]
        while J:
            if J & 1:
                cur = [up[x] for x in cur]
            up = [up[x] for x in up]
            J >>= 1
        inF = [0] * len(ids)
        inL = [0] * len(ids)
        common = 0
        curS = -1
        for e1 in range(1, R[1] + 1):
            if e1 + 1 > n:
                break
            s = cur[e1 + 1]
            x = cls[e1]
            inF[x] = 1
            if inL[x]:
                common += 1
            if curS == -1:
                curS = s
                for q in range(s, n + 1):
                    y = cls[q]
                    inL[y] += 1
                    if inF[y] and inL[y] == 1:
                        common += 1
            else:
                while curS < s:
                    y = cls[curS]
                    inL[y] -= 1
                    if inF[y] and inL[y] == 0:
                        common -= 1
                    curS += 1
            if s <= n and R[s] == n and common == 0:
                starts = [1]
                q = e1 + 1
                while q < s:
                    starts.append(q)
                    q = nxt[q]
                starts.append(s)
                break

    res = []
    if starts is not None:
        K = (c - 1) // 2
        for i in range(2, K + 2):
            res.append((i, i + K - 1))
    else:
        starts = []
        p = 1
        while p <= n:
            starts.append(p)
            p = nxt[p]
        if c > 1:
            K = (c + 1) // 2
            for i in range(2, K + 2):
                res.append((i, min(i + K - 1, c)))
    starts.append(n + 1)
    out.append(str(len(res)))
    for a, b in res:
        out.append(str(starts[a - 1]) + " " + str(starts[b] - 1))
print("\n".join(out))
