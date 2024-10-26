package main

import (
	"bufio"
	"fmt"
	"os"
	"sort"
	"strconv"
	"strings"
)

// This implementation has some precalculated values and dirty tricks to make it performant.
// This is not how I code normally... Sorry for the eye bleed.

// PowersOf3 : powers of 3 from 3^0 till 3^9
var PowersOf3 = [10]int{1, 3, 9, 27, 81, 243, 729, 2187, 6561, 19683}

// MissingMiddleAddingList : for given number of parts in a class, yields the amount to be added to the score (for all the middles that it lacks in the beginning)
var MissingMiddleAddingList = [11]int{29524, 9841, 3280, 1093, 364, 121, 40, 13, 4, 1, 0}

type Person struct {
	Name  string
	Point int
}

var people = [100]Person{}
var input string

func main() {
	stdinReader := bufio.NewReader(os.Stdin)
	outputBuilder := strings.Builder{}

	input, _ = stdinReader.ReadString('\n')
	numberOfCases, _ := strconv.Atoi(strings.TrimSpace(input))

	// TODO: Remove debug print
	fmt.Printf("Number of cases: %d\n", numberOfCases)

	for caseIdx := 0; caseIdx < numberOfCases; caseIdx++ {
		input, _ = stdinReader.ReadString('\n')
		numberOfPeople, _ := strconv.Atoi(strings.TrimSpace(input))

		// TODO: Remove debug print
		fmt.Printf("Number of people in case %d: %d\n", caseIdx, numberOfPeople)

		thisCasePeople := people[0:numberOfPeople]

		// declare the variables needed within the next loop here
		var (
			classesUnified       string
			classesUnifiedLength int
			classCursorPos       int
			currClass            string
			numberOfClasses      int
			comp                 int
			isNotMiddle          int
		)
		for personIdx := 0; personIdx < numberOfPeople; personIdx++ {
			input, _ = stdinReader.ReadString('\n')
			lineParts := strings.Fields(input)

			thisCasePeople[personIdx].Name = lineParts[0][0 : len(lineParts[0])-1]
			classesUnified = lineParts[1]
			classesUnifiedLength = len(classesUnified)

			thisCasePeople[personIdx].Point = 0

			// TODO: remove debug prints
			fmt.Printf("Name: %s\nClasses-unif: %s\n", thisCasePeople[personIdx].Name, classesUnified)

			classCursorPos = 0
			numberOfClasses = 0
			for classCursorPos < classesUnifiedLength {
				currClass = classesUnified[classCursorPos : classCursorPos+1] // just get "l", "m" or "u"

				// TODO: remove debug prints
				fmt.Printf("%s", currClass)

				comp = strings.Compare(currClass, "m")
				thisCasePeople[personIdx].Point += (comp + 1) * PowersOf3[numberOfClasses] // add 0 for l, 1 for m, 2 for u to the number in base3

				isNotMiddle = comp * comp
				classCursorPos += 7 - isNotMiddle

				numberOfClasses++
			}

			// TODO: remove debug prints
			fmt.Println("\n")

			thisCasePeople[personIdx].Point *= PowersOf3[10-numberOfClasses]            // shift by 10-numberOfClasses digits in base3
			thisCasePeople[personIdx].Point += MissingMiddleAddingList[numberOfClasses] // add 1's to the missing digits
		}

		sort.Slice(thisCasePeople, func(i, j int) bool {
			return thisCasePeople[i].Point > thisCasePeople[j].Point ||
				(thisCasePeople[i].Point == thisCasePeople[j].Point && thisCasePeople[i].Name < thisCasePeople[j].Name)
		})

		for personIdx := 0; personIdx < numberOfPeople; personIdx++ {
			outputBuilder.WriteString(thisCasePeople[personIdx].Name)
			outputBuilder.WriteRune('\n')
		}
		outputBuilder.WriteString("==============================\n")
	}

	fmt.Print(outputBuilder.String())
}
