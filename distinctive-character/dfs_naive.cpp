#include <iostream>
#include <cstring>
#include <climits>

using namespace std;

#define MASK(num) (1 << num)

unsigned int n,k;
unsigned int globalBestDissimilarity = 0;
unsigned int globalBestCharacter;

void dfs(unsigned int* otherCharacters, unsigned int currFeatureIndex, unsigned int currFeatureVal, unsigned int currCharacter){
    currCharacter += currFeatureVal << currFeatureIndex;

    if(currFeatureIndex == k-1) {
        unsigned int minDissimilarity = UINT_MAX;
        for(unsigned int i = 0; i < n; i++) {
              unsigned int dissimilarity = __builtin_popcount(currCharacter ^ otherCharacters[i]);
              if(dissimilarity < minDissimilarity){
                minDissimilarity = dissimilarity;
              }
        }

        if(minDissimilarity > globalBestDissimilarity){
            globalBestDissimilarity = minDissimilarity;
            globalBestCharacter = currCharacter;
        }

        return;
    }

    dfs(otherCharacters, currFeatureIndex+1, 0, currCharacter);
    dfs(otherCharacters, currFeatureIndex+1, 1, currCharacter);
}

int main() {
    cin >> n >> k;
    unsigned int otherCharacters[n];
    string temp;
    for(unsigned int i =0; i < n; i++) {
        cin >> temp;
        otherCharacters[i] = stoi(temp, nullptr, 2);
    }

    // early exit if only one character
    if(n == 1) {
        const unsigned int finalCharacter = (1 << k)-1 - otherCharacters[0];

        for(int i = k-1; i >= 0; i--) {
            unsigned int ii = i;
            cout << ((finalCharacter & MASK(ii)) >> ii);
        }
        cout << endl;
        return 0;
    }

    dfs(otherCharacters, 0, 0, 0);
    dfs(otherCharacters, 0, 1, 0);

    for(int i = k-1; i >= 0; i--) {
        unsigned int ii = i;
        cout << ((globalBestCharacter & MASK(ii)) >> ii);
    }
    cout << endl;
}