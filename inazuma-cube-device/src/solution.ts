import * as fs from 'fs';

// Read the entire input at once
const inputBuffer = fs.readFileSync(0, 'utf-8');
let inputBufferIdx = 0;


// Skips whitespace and reads the next meaningful token
function readString(): string {
    let startIdx = inputBufferIdx;

    // Skip leading whitespace
    while (inputBufferIdx < inputBuffer.length && inputBuffer.charCodeAt(inputBufferIdx) <= 32) {
        inputBufferIdx++;
    }

    if (inputBufferIdx >= inputBuffer.length) return "";

    startIdx = inputBufferIdx;

    // Read until next whitespace
    while (inputBufferIdx < inputBuffer.length && inputBuffer.charCodeAt(inputBufferIdx) > 32) {
        inputBufferIdx++;
    }

    return inputBuffer.substring(startIdx, inputBufferIdx);
}


// Reads the next token as an integer
function readInt(): number {
    return parseInt(readString(), 10);
}


// Main Solution
function solve() {
    const n = readInt();
    const m = readInt();
    const k = readInt();

    const initialStates: number[] = new Array(n + 1).fill(0);
    // We'll use 1-based indexing for convenience to match the problem description, making the index-0 a dummy index.
    for (let c = 1; c <= n; c++) {
        initialStates[c] = readInt();
    }

    const adj: number[][] = Array.from({ length: n + 1 }, () => []);

    for (let i = 0; i < m; i++) {
        const u = readInt();
        const v = readInt();
        adj[u].push(v);
    }

    // Array to track incoming strikes from chain reactions
    const incoming: number[] = new Array(n + 1).fill(0);

    let totalStrikes = 0;

    // The algorithm is indeed very straightforward due to the problem constraints.
    // This problem needs a topologically sorted approach to ensure we handle dependencies correctly.
    // However, since relations are promised to be between a->b such that a < b, we can simply iterate from 1 to n without breaking the topological order in the DAG.
    // Also, the problem states that we should print "-1" if it's impossible, but given the constraints, it's always possible to achieve the goal as there are no cycles.
    for (let c = 1; c <= n; c++) {
        const totalHits = initialStates[c] + incoming[c];

        const neededStrikes = (k - (totalHits % k)) % k;
        totalStrikes += neededStrikes;

        // Propagate the strikes to connected components
        for (const neighbor of adj[c]) {
            incoming[neighbor] += neededStrikes;
        }
    }

    console.log(totalStrikes.toString());
}

solve();