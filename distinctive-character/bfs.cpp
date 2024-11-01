#include <iostream>
#include <cstring>
#include <vector>
#include <climits>
#include <queue>

using namespace std;

#define MASK(num) (1 << (num))

int main() {
    unsigned int n,k;
    cin >> n >> k;
    const int maxNodes = 1 << k;
    unsigned int otherCharacters[n];
    for(unsigned int i = 0; i < n; i++) {
        unsigned int val = 0;
        getchar_unlocked();
        for(unsigned int m = 1 << k-1; m > 0; m >>= 1) {
            val += (getchar_unlocked() - '0') * m;
        }

        otherCharacters[i] = val;
    }
    getchar_unlocked();

    queue<unsigned int> q;
    bool visited[maxNodes];
    memset(visited, 0, sizeof(visited));
    for(unsigned int i = 0; i < n; i++) {
        visited[otherCharacters[i]] = true;
        q.push(otherCharacters[i]);
    }

    unsigned int globalBestCharacter;

    while(!q.empty()) {
        const unsigned int ch = q.front();
        q.pop();

        for(unsigned int i = 1; i < maxNodes; i <<= 1) {
            const auto nextCh = ch ^ i;
            if(!visited[nextCh]) {
                visited[nextCh] = true;
                q.push(nextCh);
                globalBestCharacter = nextCh;
            }
        }
    }

    for(unsigned int i = k-1; i != UINT_MAX; i--) {
        cout << ((globalBestCharacter & MASK(i)) >> i);
    }
    cout << endl;
}