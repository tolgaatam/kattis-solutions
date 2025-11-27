package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

type Node struct {
	parent      *Node
	size        int
	distrustSet map[int]struct{}
}

var nodes []Node

func initialize(n int) {
	// initializes disjoint set structures
	nodes = make([]Node, 0, n)
	for i := 0; i < n; i++ {
		nodes = append(nodes, Node{
			parent:      nil, // roots have nil parent
			size:        1,
			distrustSet: make(map[int]struct{}),
		})
	}
}

func find(node *Node) *Node {
	if node.parent == nil {
		return node // node is a root
	}
	node.parent = find(node.parent) // path compression
	return node.parent
}

func union(nodeA, nodeB *Node) {
	rootA := find(nodeA)
	rootB := find(nodeB)
	if rootA == rootB {
		return
	}

	// make sure rootA is the larger set
	if rootA.size < rootB.size {
		rootA, rootB = rootB, rootA
	}

	rootB.parent = rootA
	rootA.size += rootB.size

	// Merge distrust sets
	for x := range rootB.distrustSet {
		rootA.distrustSet[x] = struct{}{}
	}
	rootB.distrustSet = nil // help GC clear stuff
}

func NewStdinIntegerReaderFn() func() (int, error) {
	// Fast streaming integer scanner using bufio.Reader. This avoids reading
	// the whole stdin into memory and is fast enough. `nextInt` returns the next positive integer.
	reader := bufio.NewReaderSize(os.Stdin, 1<<16)
	return func() (int, error) {
		val := 0
		// skip separators
		c, err := reader.ReadByte()
		for err == nil && (c == ' ' || c == '\n') {
			c, err = reader.ReadByte()
		}
		if err != nil {
			return 0, err
		}
		for err == nil && c >= '0' && c <= '9' {
			val = val*10 + int(c-'0')
			c, err = reader.ReadByte()
		}
		if err == nil {
			_ = reader.UnreadByte()
		}
		return val, nil
	}
}

func main() {
	nextInt := NewStdinIntegerReaderFn()
	outputBuilder := strings.Builder{}

	N, _ := nextInt()
	M, _ := nextInt()
	Q, _ := nextInt()

	initialize(N + 1)

	// Read distrust pairs - store distrust pair indices
	for i := 0; i < M; i++ {
		a, _ := nextInt()
		b, _ := nextInt()
		nodes[a].distrustSet[i] = struct{}{}
		nodes[b].distrustSet[i] = struct{}{}
	}

	// Process proposals
	for i := 0; i < Q; i++ {
		a, _ := nextInt()
		b, _ := nextInt()

		rootA := find(&nodes[a])
		rootB := find(&nodes[b])

		// Check intersection: if any distrust pair index overlaps, REFUSE
		refuse := false

		// Iterate over smaller set to speed up intersection test
		smallerRoot, largerRoot := rootA, rootB
		if len(rootA.distrustSet) > len(rootB.distrustSet) {
			smallerRoot, largerRoot = rootB, rootA
		}

		for x := range smallerRoot.distrustSet {
			if _, exists := largerRoot.distrustSet[x]; exists {
				refuse = true
				break
			}
		}

		if refuse {
			outputBuilder.WriteString("REFUSE\n")
		} else {
			union(&nodes[a], &nodes[b])
			outputBuilder.WriteString("APPROVE\n")
		}

		// Flush output every 15000 bytes to avoid large memory usage and optimize I/O waiting times
		// This threshold is chosen based on empirical testing on Kattis.
		if outputBuilder.Len() >= 15000 {
			fmt.Print(outputBuilder.String())
			outputBuilder.Reset()
		}
	}

	if outputBuilder.Len() > 0 {
		fmt.Print(outputBuilder.String())
	}
}
