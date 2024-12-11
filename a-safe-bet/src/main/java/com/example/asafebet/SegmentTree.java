package com.example.asafebet;

public class SegmentTree {
    private final int[] tree;
    private final int[] lazy;
    private final int size;

    public SegmentTree(int maxRange) {
        this.size = maxRange;
        this.tree = new int[4 * maxRange]; // Large enough to cover all ranges
        this.lazy = new int[4 * maxRange];
    }

    // Update range [l, r] by adding value
    public void update(int l, int r, int value) {
        update(0, 0, size - 1, l, r, value);
    }

    private void update(int node, int start, int end, int l, int r, int value) {
        // Apply any pending lazy updates
        if (lazy[node] != 0) {
            tree[node] += lazy[node] * (end - start + 1); // Update the current node
            if (start != end) { // Propagate to children
                lazy[2 * node + 1] += lazy[node];
                lazy[2 * node + 2] += lazy[node];
            }
            lazy[node] = 0; // Clear the lazy value
        }

        // No overlap
        if (start > r || end < l) {
            return;
        }

        // Total overlap
        if (start >= l && end <= r) {
            tree[node] += value * (end - start + 1);
            if (start != end) { // Propagate to children
                lazy[2 * node + 1] += value;
                lazy[2 * node + 2] += value;
            }
            return;
        }

        // Partial overlap
        int mid = (start + end) / 2;
        update(2 * node + 1, start, mid, l, r, value);
        update(2 * node + 2, mid + 1, end, l, r, value);
        tree[node] = tree[2 * node + 1] + tree[2 * node + 2]; // Update current node
    }

    // Query range [l, r]
    public int query(int l, int r) {
        return query(0, 0, size - 1, l, r);
    }

    private int query(int node, int start, int end, int l, int r) {
        // Apply any pending lazy updates
        if (lazy[node] != 0) {
            tree[node] += lazy[node] * (end - start + 1); // Update the current node
            if (start != end) { // Propagate to children
                lazy[2 * node + 1] += lazy[node];
                lazy[2 * node + 2] += lazy[node];
            }
            lazy[node] = 0; // Clear the lazy value
        }

        // No overlap
        if (start > r || end < l) {
            return 0;
        }

        // Total overlap
        if (start >= l && end <= r) {
            return tree[node];
        }

        // Partial overlap
        int mid = (start + end) / 2;
        int leftQuery = query(2 * node + 1, start, mid, l, r);
        int rightQuery = query(2 * node + 2, mid + 1, end, l, r);
        return leftQuery + rightQuery;
    }
}

