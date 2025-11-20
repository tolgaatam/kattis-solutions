package main

import (
	"bufio"
	"fmt"
	"math"
	"os"
)

type Point struct{ x, y float64 }

func (p Point) sub(q Point) Point   { return Point{p.x - q.x, p.y - q.y} }
func (p Point) add(q Point) Point   { return Point{p.x + q.x, p.y + q.y} }
func (p Point) mul(s float64) Point { return Point{p.x * s, p.y * s} }
func dot(a, b Point) float64        { return a.x*b.x + a.y*b.y }
func cross(a, b Point) float64      { return a.x*b.y - a.y*b.x }
func len2(a Point) float64          { return dot(a, a) }
func length(a Point) float64        { return math.Sqrt(len2(a)) }
func perp(a Point) Point            { return Point{-a.y, a.x} }

var epsilon = 1e-9

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

// Check polygon convexity (allow collinear) assuming A - Pnew - B are consecutive points in CCW order
func isConvexCCW(A, Pnew, B Point) bool {
	v1 := Pnew.sub(A)
	v2 := B.sub(Pnew)
	return cross(v1, v2) >= -epsilon
}

// angle at center between v1 and v2 should be >= 90 degrees -> dot <= 0
func isAngleAtLeast90(v1, v2 Point) bool {
	return dot(v1, v2) <= 1e-9
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

	// iterate each point as candidate to replace
	for i := 0; i < numberOfPoints; i++ {
		A := points[(i-1+numberOfPoints)%numberOfPoints]
		prevA := points[(i-2+numberOfPoints)%numberOfPoints]
		P := points[i]
		B := points[(i+1)%numberOfPoints]
		nextB := points[(i+2)%numberOfPoints]

		// circle with diameter AB
		center := A.add(B).mul(0.5)
		r := length(B.sub(A)) * 0.5
		if r <= epsilon {
			continue
		}

		candidates := make([]Point, 0, 7)

		// two points on perpendicular bisector (endpoints of perpendicular diameter)
		abDir := B.sub(A)
		perpDir := perp(abDir)
		pl := length(perpDir)
		if pl > epsilon {
			u := perpDir.mul(1.0 / pl)
			candidates = append(candidates, center.add(u.mul(r)))
			candidates = append(candidates, center.add(u.mul(-r)))
		}

		// intersection of circle with line through A perpendicular to prevA-A
		vA := prevA.sub(A)
		dA := perp(vA)
		if length(dA) > epsilon {
			u := dA.mul(1.0 / length(dA))
			// line: A + t*u
			// solve ||A + t*u - center||^2 = r^2
			o := A.sub(center)
			bcoef := 2 * dot(u, o)
			cc := dot(o, o) - r*r
			dscr := bcoef*bcoef - 4*cc
			if dscr >= -epsilon {
				if dscr < 0 {
					dscr = 0
				}
				t1 := (-bcoef + math.Sqrt(dscr)) / 2.0
				t2 := (-bcoef - math.Sqrt(dscr)) / 2.0
				candidates = append(candidates, A.add(u.mul(t1)))
				candidates = append(candidates, A.add(u.mul(t2)))
			}
		}

		// intersection of circle with line through B perpendicular to B-nextB
		vB := nextB.sub(B)
		dB := perp(vB)
		if length(dB) > epsilon {
			u := dB.mul(1.0 / length(dB))
			o := B.sub(center)
			bcoef := 2 * dot(u, o)
			cc := dot(o, o) - r*r
			dscr := bcoef*bcoef - 4*cc
			if dscr >= -epsilon {
				if dscr < 0 {
					dscr = 0
				}
				t1 := (-bcoef + math.Sqrt(dscr)) / 2.0
				t2 := (-bcoef - math.Sqrt(dscr)) / 2.0
				candidates = append(candidates, B.add(u.mul(t1)))
				candidates = append(candidates, B.add(u.mul(t2)))
			}
		}

		// intersection of line through B perpendicular to B-nextB with line through A perpendicular to prevA-A
		if length(dA) > epsilon && length(dB) > epsilon {
			denom := cross(dA, dB)
			if math.Abs(denom) > epsilon {
				// lines are not parallel
				o := A.sub(B)
				tA := cross(o, dB) / denom
				// tB := cross(o, dA) / denom
				intersectP := A.add(dA.mul(tA))
				candidates = append(candidates, intersectP)
			}
		}

		// validate candidates
		for _, cand := range candidates {
			// skip if candidate is extremely close to existing adjacent vertices
			if length(cand.sub(A)) < 1e-9 || length(cand.sub(B)) < 1e-9 {
				continue
			}

			// angle prevA-A-cand at A should be >= 90
			if !isAngleAtLeast90(prevA.sub(A), cand.sub(A)) {
				continue
			}
			// angle cand-B-nextB at B should be >= 90
			if !isAngleAtLeast90(cand.sub(B), nextB.sub(B)) {
				continue
			}
			// angle at new point P' should be >= 90 (A-P'-B)
			if !isAngleAtLeast90(A.sub(cand), B.sub(cand)) {
				continue
			}

			// polygon convexity
			if !isConvexCCW(A, cand, B) {
				continue
			}

			oldDist := length(P.sub(A)) + length(P.sub(B))
			newDist := length(cand.sub(A)) + length(cand.sub(B))
			inc := newDist - oldDist
			if inc > maxPerimeterIncrease+epsilon {
				maxPerimeterIncrease = inc
			}
		}
	}

	fmt.Printf("%.12f\n")

}
