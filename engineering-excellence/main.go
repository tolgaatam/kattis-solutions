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

var epsilon = 1e-4
var candidates = make([]Point, 0, 64)

func addIntersectionOfTwoLines(P1, D1, P2, D2 Point) {
	denom := cross(D1, D2)
	if math.Abs(denom) > epsilon {
		diff := P2.sub(P1)
		t := cross(diff, D2) / denom
		cand := P1.add(D1.mul(t))
		candidates = append(candidates, cand)
	}
}

func addIntersectionsOfLineWithCircle(P0, D, O Point, r float64) {
	if length(D) > epsilon {
		u := D.mul(1.0 / length(D))
		o := P0.sub(O)
		bcoef := 2 * dot(u, o)
		cc := dot(o, o) - r*r
		dscr := bcoef*bcoef - 4*cc
		if dscr >= -epsilon {
			if dscr < 0 {
				dscr = 0
			}
			t1 := (-bcoef + math.Sqrt(dscr)) / 2.0
			t2 := (-bcoef - math.Sqrt(dscr)) / 2.0
			candidates = append(candidates, P0.add(u.mul(t1)))
			candidates = append(candidates, P0.add(u.mul(t2)))
		}
	}
}

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

// Check polygon convexity (allow collinear) assuming P1 - P2 - P3 are consecutive points in CCW order
func isConvexCCW(P1, P2, P3 Point) bool {
	v1 := P2.sub(P1)
	v2 := P3.sub(P2)
	return cross(v1, v2) >= -epsilon
}

// angle at center between v1 and v2 should be >= 90 degrees -> dot <= 0
func isAngleAtLeast90(v1, v2 Point) bool {
	return dot(v1, v2) <= epsilon
}

func validateCandidate(prevA, A, candidate, B, nextB Point) bool {
	// skip if candidate is extremely close to existing adjacent vertices
	if length(candidate.sub(A)) < epsilon || length(candidate.sub(B)) < epsilon {
		return false
	}

	// angle prevA-A-cand at A should be >= 90
	if !isAngleAtLeast90(prevA.sub(A), candidate.sub(A)) {
		return false
	}
	// angle cand-B-nextB at B should be >= 90
	if !isAngleAtLeast90(candidate.sub(B), nextB.sub(B)) {
		return false
	}
	// angle at new point P' should be >= 90 (A-P'-B)
	if !isAngleAtLeast90(A.sub(candidate), B.sub(candidate)) {
		return false
	}

	// polygon convexity
	if !isConvexCCW(A, candidate, B) || !isConvexCCW(prevA, A, candidate) || !isConvexCCW(candidate, B, nextB) {
		return false
	}

	return true
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
		radius := length(B.sub(A)) * 0.5
		if radius <= epsilon { // TODO: A != B is guaranteed in the problem. no need for this check
			continue
		}

		candidates = candidates[:0] // reset candidate slice

		// two points on perpendicular bisector (endpoints of perpendicular diameter)
		abDir := B.sub(A)
		perpDir := perp(abDir)
		pl := length(perpDir)
		if pl > epsilon {
			u := perpDir.mul(1.0 / pl)
			// TODO: only one of these points is needed. remove unnecessary one for efficiency later
			candidates = append(candidates, center.add(u.mul(radius)))
			candidates = append(candidates, center.add(u.mul(-radius)))
		}

		/* TODO: we can check the validity of the perpendicular bisector here, and if it passes the angle and convexity tests,
		   we can directly compute the perimeter increase and avoid adding unnecessary candidates
		*/

		// intersection of the circle with perpA line (through A perpendicular to prevA-A)
		vA := prevA.sub(A)
		vPerpA := perp(vA)
		addIntersectionsOfLineWithCircle(A, vPerpA, center, radius)

		// intersection of the circle with perpB line (through B perpendicular to nextB-B)
		vB := nextB.sub(B)
		vPerpB := perp(vB)
		addIntersectionsOfLineWithCircle(B, vPerpB, center, radius)

		// intersection of perpA and perpB lines
		addIntersectionOfTwoLines(A, vPerpA, B, vPerpB)

		// intersection of circle with the contA line extending prevA-A (convexity Limit)
		addIntersectionsOfLineWithCircle(A, vA, center, radius)

		// intersection of circle with the contB line extending nextB-B (convexity Limit)
		addIntersectionsOfLineWithCircle(B, vB, center, radius)

		// intersection of the contA line and contB line
		addIntersectionOfTwoLines(A, vA, B, vB)

		// intersection of perpA line and contB line
		addIntersectionOfTwoLines(A, vPerpA, B, vB)

		// intersection of perpB line and contA line
		addIntersectionOfTwoLines(B, vPerpB, A, vA)

		oldDist := length(P.sub(A)) + length(P.sub(B))

		// validate candidates
		for _, candidate := range candidates {
			if !validateCandidate(prevA, A, candidate, B, nextB) {
				continue
			}

			newDist := length(candidate.sub(A)) + length(candidate.sub(B))
			inc := newDist - oldDist
			if inc > maxPerimeterIncrease+epsilon {
				maxPerimeterIncrease = inc
			}
		}
	}

	fmt.Printf("%.12f\n", maxPerimeterIncrease)
}
