package main

import (
	"bufio"
	"fmt"
	"os"
)

type Point struct{ x, y float64 }

func NewStdinIntegerReaderFn() func() (int, error) {
	// Fast streaming integer scanner using bufio.Reader. This avoids reading
	// the whole stdin into memory and is fast enough. `nextInt` returns the next signed integer.
	reader := bufio.NewReaderSize(os.Stdin, 1<<16)
	return func() (int, error) {
		sign := 1
		val := 0
		// skip separators
		c, err := reader.ReadByte()
		for err == nil && (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
			c, err = reader.ReadByte()
		}
		if err != nil {
			return 0, err
		}
		if c == '-' {
			sign = -1
			c, err = reader.ReadByte()
		}
		for err == nil && c >= '0' && c <= '9' {
			val = val*10 + int(c-'0')
			c, err = reader.ReadByte()
		}
		if err == nil {
			_ = reader.UnreadByte()
		}
		return val * sign, nil
	}
}

func main() {
	nextInt := NewStdinIntegerReaderFn()

	numberOfPoints, _ := nextInt()
	points := make([]Point, numberOfPoints)
	for i := 0; i < numberOfPoints; i++ {
		x, _ := nextInt()
		y, _ := nextInt()
		points[i] = Point{float64(x), float64(y)}
	}

	maxPerimeterIncrease := 0.0
	epsilon := 1e-9

	/* rest of the code goes here


	 */

	fmt.Printf("%.12f\n", maxPerimeterIncrease)

}
