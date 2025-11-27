package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

var (
	parents      []int
	sizes        []int
	distrustSets []map[int]struct{}
)

func initialize(n int) {
	// initializes disjoint set structures
	parents = make([]int, n)
	sizes = make([]int, n)
	distrustSets = make([]map[int]struct{}, n)

	for i := 0; i < n; i++ {
		parents[i] = i
		sizes[i] = 1
	}
}

func find(x int) int {
	if parents[x] != x {
		parents[x] = find(parents[x]) // path compression
	}
	return parents[x]
}

func union(a, b int) {
	rootA := find(a)
	rootB := find(b)
	if rootA == rootB {
		return
	}

	// make sure rootA is the larger set
	if sizes[rootA] < sizes[rootB] {
		rootA, rootB = rootB, rootA
	}

	parents[rootB] = rootA
	sizes[rootA] += sizes[rootB]

	if distrustSets[rootA] == nil {
		distrustSets[rootA] = distrustSets[rootB]
	} else {
		for x := range distrustSets[rootB] {
			distrustSets[rootA][x] = struct{}{}
		}
	}
	distrustSets[rootB] = nil // help GC clear stuff
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

	// Read distrust pairs
	for i := 0; i < M; i++ {
		a, _ := nextInt()
		b, _ := nextInt()
		if distrustSets[a] == nil {
			distrustSets[a] = map[int]struct{}{
				i: {},
			}
		} else {
			distrustSets[a][i] = struct{}{}
		}

		if distrustSets[b] == nil {
			distrustSets[b] = map[int]struct{}{
				i: {},
			}
		} else {
			distrustSets[b][i] = struct{}{}
		}
	}

	// Process proposals
	for i := 0; i < Q; i++ {
		a, _ := nextInt()
		b, _ := nextInt()

		rootA := find(a)
		rootB := find(b)

		// check intersection: if any distrust pair index overlaps, REFUSE
		refuse := false

		if len(distrustSets[rootA]) > 0 && len(distrustSets[rootB]) > 0 {
			// iterate over smaller set to speed up intersection test
			if len(distrustSets[rootA]) > len(distrustSets[rootB]) {
				rootA, rootB = rootB, rootA
			}

			for x := range distrustSets[rootA] {
				if _, exists := distrustSets[rootB][x]; exists {
					refuse = true
					break
				}
			}
		}

		if refuse {
			outputBuilder.WriteString("REFUSE\n")
		} else {
			union(rootA, rootB)
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
