#include <iostream>
#include <cstring>

using namespace std;

#define MASK(num) (1 << num)

int n,k;
int globalBestResult = -1;

bool updateGlobalBestResult(const int newResult) {
    if(newResult > globalBestResult) {
        globalBestResult = newResult;
        return true;
    }
    return false;
}

struct dfsResult {
    int maxDissimilarity;
    int bestCharacter;
};

struct diffOfMinTwoResult {
    int min;
    int min2;
    int minIndex;
};

void addArrays(int* pivot, const int* added) {
    for(int i = 0; i < n; i++) {
        pivot[i] += added[i];
    }
}

void subtractArrays(int* pivot, const int* added) {
    for(int i = 0; i < n; i++) {
        pivot[i] -= added[i];
    }
}

int minOfDissimList(const int* list) {
    int min = list[0];
    for(int i = 1; i < n; i++) {
        if(list[i] < min) {
            min = list[i];
        }
    }
    return min;
}

diffOfMinTwoResult diffOfMinTwoOfDissimList(const int* list) {
    int minIndex = 0;
    int min = list[0];
    int min2;
    if(min <= list[1]) {
        min2 = list[1];
    } else {
        minIndex = 1;
        min = list[1];
        min2 = list[0];
    }

    for(int i = 2; i < n; i++) {
        if(list[i] < min) {
            min2 = min;
            min = list[i];
            minIndex = i;
        } else if(list[i] < min2) {
            min2 = list[i];
        }
    }

    return diffOfMinTwoResult{min, min2, minIndex};
}

dfsResult dfsMaxDissimilarity(int*** precompDissimilarities, int* otherCharacters, int* dissimList, int currFeatureIndex, int currFeatureVal, int currCharacter) {
    const int* dissimToBeAdded = precompDissimilarities[currFeatureIndex][currFeatureVal];

    addArrays(dissimList, dissimToBeAdded);
    currCharacter += currFeatureVal << currFeatureIndex;

    if(currFeatureIndex == k-1) {
        const auto res = dfsResult{minOfDissimList(dissimList), currCharacter};
        subtractArrays(dissimList, dissimToBeAdded);
        return res;
    }

    const auto diffOfMinTwoResult = diffOfMinTwoOfDissimList(dissimList);
    const auto featuresLeft = k - 1 - currFeatureIndex;
    if(diffOfMinTwoResult.min + featuresLeft < globalBestResult) {
        // we can't beat the best result within this node, it doesn't matter what we return
        subtractArrays(dissimList, dissimToBeAdded);
        return dfsResult{
            diffOfMinTwoResult.min,
            currCharacter,
        };
    }
    if(diffOfMinTwoResult.min2 - diffOfMinTwoResult.min >= featuresLeft) {
        // we should give everything to diffOfMinTwoResult.minIndex to produce the best outcome of this node
        subtractArrays(dissimList, dissimToBeAdded);
        updateGlobalBestResult(diffOfMinTwoResult.min + featuresLeft);
        return dfsResult{
            diffOfMinTwoResult.min + featuresLeft,
            currCharacter + (( 1 << featuresLeft)-1 - (otherCharacters[diffOfMinTwoResult.minIndex] >> featuresLeft-currFeatureIndex) << featuresLeft-currFeatureIndex),
        };
    }

    const auto dfsResult0 = dfsMaxDissimilarity(precompDissimilarities, otherCharacters, dissimList, currFeatureIndex+1, 0, currCharacter);
    const auto dfsResult1 = dfsMaxDissimilarity(precompDissimilarities, otherCharacters, dissimList, currFeatureIndex+1, 1, currCharacter);

    subtractArrays(dissimList, dissimToBeAdded);
    if(dfsResult0.maxDissimilarity > dfsResult1.maxDissimilarity) {
        updateGlobalBestResult(dfsResult0.maxDissimilarity);
        return dfsResult0;
    }
    updateGlobalBestResult(dfsResult1.maxDissimilarity);
    return dfsResult1;
}

int main() {
    cin >> n >> k;
    int otherCharacters[n];
    int*** precompDissimilarities = new int**[k];
    for(int i = 0; i < k; i++) {
        precompDissimilarities[i] = new int*[2];
        for(int j = 0; j < 2; j++) {
            precompDissimilarities[i][j] = new int[n];
        }
    }

    string temp;
    for(int i =0; i < n; i++) {
        cin >> temp;
        otherCharacters[i] = stoi(temp, nullptr, 2);
    }

    // early exit if only one character
    if(n == 1) {
        const int finalCharacter = (1 << k)-1 - otherCharacters[0];

        for(int i = k-1; i >= 0; i--) {
            cout << ((finalCharacter & MASK(i)) >> i);
        }
        cout << endl;
        return 0;
    }

    for(int featureIndex = 0; featureIndex < k; featureIndex++) {
        for(int featureValue = 0; featureValue < 2; featureValue++) {
            const int featureMasked = featureValue << featureIndex;
            for(int person = 0; person < n; person++) {
                precompDissimilarities[featureIndex][featureValue][person] = (otherCharacters[person] & MASK(featureIndex)) != featureMasked;
            }
        }
    }

    int dissimList[n];
    memset(dissimList, 0, sizeof(int) * n);

    const auto dfsResult0 = dfsMaxDissimilarity(precompDissimilarities, otherCharacters, dissimList, 0, 0, 0);
    const auto dfsResult1 = dfsMaxDissimilarity(precompDissimilarities, otherCharacters, dissimList, 0, 1, 0);

    int finalCharacter;

    if(dfsResult0.maxDissimilarity > dfsResult1.maxDissimilarity) {
        finalCharacter = dfsResult0.bestCharacter;
    } else {
        finalCharacter = dfsResult1.bestCharacter;
    }

    for(int i = k-1; i >= 0; i--) {
        cout << ((finalCharacter & MASK(i)) >> i);
    }
    cout << endl;

}
