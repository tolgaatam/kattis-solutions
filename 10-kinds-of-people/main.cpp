#include <iostream>
#include <cstring>
#include <vector>
#include <queue>

using namespace std;

#define ACC(rr,cc) ((rr) * (cols) + (cc))
#define SEW(rr,cc) (((rr) << 10) | (cc))
#define ROW_OF_SEWED(s) ((s) >> 10)
#define COL_OF_SEWED(s) ((s) & 0x3ff)

int main() {
    int rows, cols;
    cin >> rows >> cols;

    unsigned char matrix[rows * cols];
    unsigned int matrixGroups[rows * cols];
    memset(matrix, 0, rows * cols * sizeof(unsigned char));
    memset(matrixGroups, 0, rows * cols * sizeof(unsigned int));

    int matrixIndex = 0;
    for (int i = 0; i < rows; ++i) {
        getchar_unlocked();
        for (int j = 0; j < cols; ++j) {
            matrix[matrixIndex++] = getchar_unlocked() - 48;
        }
    }
    getchar_unlocked();

    // process the matrix and find out connected components
    queue<int> q;
    unsigned int currGroupId = 0;
    for(int startR = 0; startR < rows; ++startR) {
        for(int startC = 0; startC < cols; ++startC) {
            if(matrixGroups[ACC(startR,startC)] != 0) {
                continue;
            }

            matrixGroups[ACC(startR, startC)] = ++currGroupId;
            unsigned char currGroupPersonType = matrix[ACC(startR,startC)];
            q.push(SEW(startR, startC));
            while (!q.empty()) {
                int sewed = q.front();
                q.pop();
                const int r = ROW_OF_SEWED(sewed);
                const int c = COL_OF_SEWED(sewed);

                if(r > 0 && matrixGroups[ACC(r-1, c)] == 0 && matrix[ACC(r-1, c)] == currGroupPersonType) {
                    matrixGroups[ACC(r-1, c)] = currGroupId;
                    q.push(SEW(r-1, c));
                }
                if(r < rows-1 && matrixGroups[ACC(r+1, c)] == 0 && matrix[ACC(r+1, c)] == currGroupPersonType) {
                    matrixGroups[ACC(r+1, c)] = currGroupId;
                    q.push(SEW(r+1, c));
                }
                if(c > 0 && matrixGroups[ACC(r, c-1)] == 0 && matrix[ACC(r, c-1)] == currGroupPersonType) {
                    matrixGroups[ACC(r, c-1)] = currGroupId;
                    q.push(SEW(r, c-1));
                }
                if(c < cols-1 && matrixGroups[ACC(r, c+1)] == 0 && matrix[ACC(r, c+1)] == currGroupPersonType) {
                    matrixGroups[ACC(r, c+1)] = currGroupId;
                    q.push(SEW(r, c+1));
                }
            }
        }
    }

    int numberOfQueries;
    cin >> numberOfQueries;

    int row1, col1, row2, col2;
    for (int i = 0; i < numberOfQueries; ++i) {
        cin >> row1 >> col1 >> row2 >> col2;
        row1--; col1--; row2--; col2--;

        if(matrixGroups[ACC(row1, col1)] == matrixGroups[ACC(row2, col2)]) {
            if(matrix[ACC(row1, col1)] == 0) {
                cout << "binary" << endl;
            } else {
                cout << "decimal" << endl;
            }
        } else {
            cout << "neither" << endl;
        }
    }
}