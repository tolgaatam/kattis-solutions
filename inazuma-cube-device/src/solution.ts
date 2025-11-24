import * as fs from 'fs';

// Read the entire input at once
const inputBuffer = fs.readFileSync(0);
let inputBufferIdx = 0;

// Note: normally we would have checks for end of input, but the problem guarantees valid input in foreseeable amounts.
function readInt(): number {
    let res = 0;

    while (inputBuffer[inputBufferIdx] <= 32) {
        inputBufferIdx++;
    }

    while (inputBuffer[inputBufferIdx] > 32) {
        res = res * 10 + (inputBuffer[inputBufferIdx] - 48);
        inputBufferIdx++;
    }
    return res;
}


// Main Solution
function solve() {
    const n = readInt();
    const m = readInt();
    const k = readInt();

    const states = new Int32Array(n + 1).fill(0);
    // We'll use 1-based indexing for convenience to match the problem description, making the index-0 a dummy index.
    for (let c = 1; c <= n; c++) {
        states[c] = readInt();
    }

    // head[u] stores the index of the first edge for node u
    // next[i] stores the index of the next edge in the list
    // to[i] stores the destination node for edge i
    const head = new Int32Array(n + 1).fill(-1);
    const next = new Int32Array(m);
    const to = new Int32Array(m);

    // Build the graph
    for (let i = 0; i < m; i++) {
        const u = readInt();

        // Add edge u -> v at index i
        to[i] = readInt();
        next[i] = head[u];
        head[u] = i;
    }

    let totalStrikes = 0;

    // The algorithm is indeed very straightforward due to the problem constraints.
    // This problem needs a topologically sorted approach to ensure we handle dependencies correctly.
    // However, since relations are promised to be between a->b such that a < b, we can simply iterate from 1 to n without breaking the topological order in the DAG.
    // Also, the problem states that we should print "-1" if it's impossible, but given the constraints, it's always possible to achieve the goal as there are no cycles.
    for (let c = 1; c <= n; c++) {
        const neededStrikes = (k - states[c]) % k;
        totalStrikes += neededStrikes;

        // Propagate the strikes to connected components if needed
        if (neededStrikes > 0) {
            let edgeIdx = head[c];
            while (edgeIdx !== -1) {
                const neighbor = to[edgeIdx];
                states[neighbor] = (states[neighbor] + neededStrikes) % k;
                edgeIdx = next[edgeIdx];
            }
        }
    }

    console.log(totalStrikes.toString());
}

solve();