#include <iostream>
#include <cstring>
#include <string>
#include <vector>
#include <climits>
#include <queue>

using namespace std;

#define MASK(num) (1 << num)

// A fast queue implementation fit for my purposes
class MyQueue {
public:
    explicit MyQueue(const unsigned int maxEnqueueSize) {
        container = new unsigned int[maxEnqueueSize];
    }
    ~MyQueue() {
        delete[] container;
    }
    void enqueue(const unsigned int character) {
        container[firstEmptyIndex++] = character;
    }
    unsigned int dequeue() { // possibly crashing behaviour, shouldn't be called without checking isEmpty()
        return container[nextIndexToFetch++];
    }
    bool isEmpty() const {
        return nextIndexToFetch == firstEmptyIndex;
    }
private:
    unsigned int* container;
    unsigned int nextIndexToFetch = 0;
    unsigned int firstEmptyIndex = 0;
};

int main() {
    unsigned int n,k;
    cin >> n >> k;
    const int maxNodes = 1 << k;
    unsigned int otherCharacters[n];
    string temp;
    for(unsigned int i = 0; i < n; i++) {
        cin >> temp;
        otherCharacters[i] = stoi(temp, nullptr, 2);
    }

    auto q = MyQueue(maxNodes);
    unsigned char visited[maxNodes];
    memset(&visited, 0, sizeof(unsigned char) * maxNodes);
    for(unsigned int i = 0; i < n; i++) {
        visited[otherCharacters[i]] = 1;
        q.enqueue(otherCharacters[i]);
    }
    unsigned int globalBestCharacter = otherCharacters[n-1];

    while(!q.isEmpty()) {
        const unsigned int ch = q.dequeue();

        for(unsigned int i = 1; i < maxNodes; i <<= 1) {
            const auto nextCh = ch ^ i;
            if(visited[nextCh] == 0) {
                visited[nextCh] = 1;
                q.enqueue(nextCh);
                globalBestCharacter = nextCh;
            }
        }
    }

    for(unsigned int i = k-1; i != UINT_MAX; i--) {
        cout << ((globalBestCharacter & MASK(i)) >> i);
    }
    cout << endl;
}