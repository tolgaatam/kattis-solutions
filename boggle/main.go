package main

import (
	"bufio"
	"bytes"
	"fmt"
	"os"
	"strconv"
	"strings"
)

var Uint8Max uint8 = 255

func calculateCharOrder(char byte) byte {
	return char - 'A'
}

// ChildrenNodeArray type and methods
type ChildrenNodeArray [26]*TrieNode

func (c *ChildrenNodeArray) _createNodeForCharacter(char byte) *TrieNode {
	newNode := NewTrieNode()
	c[calculateCharOrder(char)] = newNode
	return newNode
}
func (c *ChildrenNodeArray) GetNodeForCharacter(char byte) *TrieNode {
	return c[calculateCharOrder(char)]
}
func (c *ChildrenNodeArray) GetOrCreateNodeForCharacter(char byte) *TrieNode {
	node := c.GetNodeForCharacter(char)
	if node == nil {
		node = c._createNodeForCharacter(char)
	}
	return node
}

// TrieNode type and methods
type TrieNode struct {
	children                      *ChildrenNodeArray
	wordEndingLastFoundCaseNumber uint8
}

func NewTrieNode() *TrieNode {
	return &TrieNode{
		children:                      nil,
		wordEndingLastFoundCaseNumber: Uint8Max,
	}
}
func (n *TrieNode) IsAWord() bool {
	return n.wordEndingLastFoundCaseNumber < Uint8Max
}
func (n *TrieNode) IsAWordUndiscoveredInThisCase(caseNumber uint8) bool {
	return n.IsAWord() && n.wordEndingLastFoundCaseNumber < caseNumber
}
func (n *TrieNode) MarkWord() {
	n.wordEndingLastFoundCaseNumber = 0
}
func (n *TrieNode) MarkWordFoundInThisCase(caseNumber uint8) {
	n.wordEndingLastFoundCaseNumber = caseNumber
}
func (n *TrieNode) _hasChildren() bool {
	return n.children != nil
}
func (n *TrieNode) IsPrefixOfAWord() bool {
	return n._hasChildren()
}
func (n *TrieNode) GetChildNodeForCharacter(char byte) *TrieNode {
	if n._hasChildren() {
		return n.children.GetNodeForCharacter(char)
	}
	return nil
}
func (n *TrieNode) GetOrCreateChildNodeForCharacter(char byte) *TrieNode {
	if !n._hasChildren() {
		n.children = &ChildrenNodeArray{}
	}
	return n.children.GetOrCreateNodeForCharacter(char)
}

// BoggleCell is a simple struct without methods
type BoggleCell struct {
	Row       int
	Column    int
	Character byte
	Neighbors []*BoggleCell
}

// CString is array of bytes to store iterated words efficiently
type CString struct {
	bytes []byte
}

func (c *CString) String() string {
	return string(c.bytes)
}
func (c *CString) Bytes() []byte {
	return c.bytes
}
func (c *CString) Len() int {
	return len(c.bytes)
}
func (c *CString) AddCharacter(char byte) {
	c.bytes = append(c.bytes, char)
}
func (c *CString) RemoveLastCharacter() {
	c.bytes = c.bytes[0 : len(c.bytes)-1]
}
func (c *CString) Copy() *CString {
	bytesCopy := make([]byte, len(c.bytes))
	copy(bytesCopy, c.bytes)
	return &CString{bytesCopy}
}

var headTrieNode = TrieNode{}

func addWordToDictionary(word []byte) {
	currNode := &headTrieNode
	wordLength := len(word)

	for currIndex := 0; currIndex < wordLength; currIndex++ {
		currChar := word[currIndex]
		currNode = currNode.GetOrCreateChildNodeForCharacter(currChar)
	}

	currNode.MarkWord()
}

var stdinReader = bufio.NewReaderSize(os.Stdin, 4096)

func readline() []byte {
	b, _, _ := stdinReader.ReadLine()
	return b
}
func asciiToUint8(str string) uint8 {
	val, _ := strconv.Atoi(str)
	return uint8(val)
}

func main() {
	outputBuilder := strings.Builder{}
	numberOfWords, _ := strconv.Atoi(string(readline()))

	for wordIdx := 0; wordIdx < numberOfWords; wordIdx++ {
		addWordToDictionary(readline())
	}

	readline()

	numberOfCases := asciiToUint8(string(readline()))

	var caseNumber uint8 = 1
	for ; caseNumber <= numberOfCases; caseNumber++ {
		var score int
		var numberOfFoundWords int
		var bestWord CString
		var bestWordLength int
		updateBestWord := func(word CString) {
			wordLength := word.Len()
			if wordLength > bestWordLength {
				bestWord = *word.Copy()
				bestWordLength = wordLength
			} else if wordLength == bestWordLength && bytes.Compare(word.Bytes(), bestWord.Bytes()) == -1 {
				bestWord = *word.Copy()
			}
		}

		var board [4][4]BoggleCell
		for i := 0; i < 4; i++ {
			line := readline()
			for j := 0; j < 4; j++ {
				board[i][j] = BoggleCell{
					Row:       i,
					Column:    j,
					Character: line[j],
				}
			}
		}

		for i := 0; i < 4; i++ {
			for j := 0; j < 4; j++ {
				cell := &board[i][j]
				if i > 0 {
					cell.Neighbors = append(cell.Neighbors, &board[i-1][j])
					if j > 0 {
						cell.Neighbors = append(cell.Neighbors, &board[i-1][j-1])
					}
					if j < 3 {
						cell.Neighbors = append(cell.Neighbors, &board[i-1][j+1])
					}
				}
				if i < 3 {
					cell.Neighbors = append(cell.Neighbors, &board[i+1][j])
					if j > 0 {
						cell.Neighbors = append(cell.Neighbors, &board[i+1][j-1])
					}
					if j < 3 {
						cell.Neighbors = append(cell.Neighbors, &board[i+1][j+1])
					}
				}
				if j > 0 {
					cell.Neighbors = append(cell.Neighbors, &board[i][j-1])
				}
				if j < 3 {
					cell.Neighbors = append(cell.Neighbors, &board[i][j+1])
				}
			}
		}

		var visited [4][4]bool
		var currWord CString

		// dfs
		var dfs func(*BoggleCell, *TrieNode)
		dfs = func(cell *BoggleCell, parentDictionaryNode *TrieNode) {
			myDictionaryNode := parentDictionaryNode.GetChildNodeForCharacter(cell.Character)
			if myDictionaryNode == nil {
				return
			}

			visited[cell.Row][cell.Column] = true
			currWord.AddCharacter(cell.Character)

			if myDictionaryNode.IsAWordUndiscoveredInThisCase(caseNumber) {
				myDictionaryNode.MarkWordFoundInThisCase(caseNumber)
				numberOfFoundWords++
				switch currWord.Len() {
				case 3, 4:
					score += 1
				case 5:
					score += 2
				case 6:
					score += 3
				case 7:
					score += 5
				case 8:
					score += 11
				}
				updateBestWord(currWord)
			}

			if myDictionaryNode.IsPrefixOfAWord() {
				for n := 0; n < len(cell.Neighbors); n++ {
					if !visited[cell.Neighbors[n].Row][cell.Neighbors[n].Column] {
						dfs(cell.Neighbors[n], myDictionaryNode)
					}
				}
			}

			visited[cell.Row][cell.Column] = false
			currWord.RemoveLastCharacter()
		}

		for i := 0; i < 4; i++ {
			for j := 0; j < 4; j++ {
				dfs(&board[i][j], &headTrieNode)
			}
		}

		outputBuilder.WriteString(fmt.Sprintf("%d %s %d\n", score, bestWord.String(), numberOfFoundWords))

		if caseNumber < numberOfCases {
			readline()
		}
	}

	fmt.Print(outputBuilder.String())
}
