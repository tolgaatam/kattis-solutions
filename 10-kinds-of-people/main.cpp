#include <iostream>
#include <cstring>
#include <vector>
#include <queue>
#include <climits>

using namespace std;

#define BOUNDARY USHRT_MAX
#define BOUNDARY_GROUP UINT_MAX
#define GROUP_NOT_SET 0

#define BINARY 0
#define DECIMAL 1

#define ACC(rr,cc) ((rr) * ((cols)+2) + (cc))
#define SEW(rr,cc) (((rr) << 10) | (cc))
#define ROW_OF_SEWED(s) ((s) >> 10)
#define COL_OF_SEWED(s) ((s) & 0x3ff)


int main() {
    int rows, cols;
    cin >> rows >> cols;

    unsigned char matrix[(rows+2) * (cols+2)];
    unsigned int matrixGroups[(rows+2) * (cols+2)];
    memset(matrix, 0, (rows+2) * (cols+2) * sizeof(unsigned char));
    memset(matrixGroups, 0, (rows+2) * (cols+2) * sizeof(unsigned int));

    for (int i = 1; i <= rows; ++i) {
        getchar_unlocked();
        for (int j = 1; j <= cols; ++j) {
            matrix[ACC(i, j)] = getchar_unlocked() - 48;
        }
    }
    getchar_unlocked();

    for(int j = 0; j <= cols+1; j++) {
        matrix[ACC(0, j)] = BOUNDARY;
        matrix[ACC(rows+1, j)] = BOUNDARY;
        matrixGroups[ACC(0, j)] = BOUNDARY_GROUP;
        matrixGroups[ACC(rows+1, j)] = BOUNDARY_GROUP;
    }
    for(int i = 0; i <= rows+1; i++) {
        matrix[ACC(i, 0)] = BOUNDARY;
        matrix[ACC(i, cols+1)] = BOUNDARY;
        matrixGroups[ACC(i, 0)] = BOUNDARY_GROUP;
        matrixGroups[ACC(i, cols+1)] = BOUNDARY_GROUP;
    }

    // process the matrix and find out connected components
    queue<int> q;
    unsigned int currGroupId = 0;
    for(int startR = 1; startR <= rows; ++startR) {
        for(int startC = 1; startC <= cols; ++startC) {
            if(matrixGroups[ACC(startR, startC)] != GROUP_NOT_SET) {
                continue;
            }

            matrixGroups[ACC(startR, startC)] = ++currGroupId;
            const unsigned char currGroupPersonType = matrix[ACC(startR, startC)];
            q.push(SEW(startR, startC));
            while (!q.empty()) {
                const int sewed = q.front();
                q.pop();
                const int r = ROW_OF_SEWED(sewed);
                const int c = COL_OF_SEWED(sewed);

                if(matrixGroups[ACC(r-1, c)] == GROUP_NOT_SET && matrix[ACC(r-1, c)] == currGroupPersonType) {
                    matrixGroups[ACC(r-1, c)] = currGroupId;
                    q.push(SEW(r-1, c));
                }
                if(matrixGroups[ACC(r+1, c)] == GROUP_NOT_SET && matrix[ACC(r+1, c)] == currGroupPersonType) {
                    matrixGroups[ACC(r+1, c)] = currGroupId;
                    q.push(SEW(r+1, c));
                }
                if(matrixGroups[ACC(r, c-1)] == GROUP_NOT_SET && matrix[ACC(r, c-1)] == currGroupPersonType) {
                    matrixGroups[ACC(r, c-1)] = currGroupId;
                    q.push(SEW(r, c-1));
                }
                if(matrixGroups[ACC(r, c+1)] == GROUP_NOT_SET && matrix[ACC(r, c+1)] == currGroupPersonType) {
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

        if(matrixGroups[ACC(row1, col1)] == matrixGroups[ACC(row2, col2)]) {
            if(matrix[ACC(row1, col1)] == BINARY) {
                cout << "binary" << endl;
            } else {
                cout << "decimal" << endl;
            }
        } else {
            cout << "neither" << endl;
        }
    }
}