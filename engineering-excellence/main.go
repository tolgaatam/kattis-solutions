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

	// helper funcs

	// Check polygon convexity (allow collinear) for CCW order after replacing index i with pNew
	isConvexWithReplacement := func(points []Point, i int, pNew Point) bool {
		n := len(points)
		np := make([]Point, n)
		copy(np, points)
		np[i] = pNew
		for j := 0; j < n; j++ {
			prev := np[(j-1+n)%n]
			cur := np[j]
			next := np[(j+1)%n]
			if cross(cur.sub(prev), next.sub(cur)) < -1e-9 { // allow small negative due to precision
				return false
			}
		}
		return true
	}

	// angle at center between v1 and v2 should be >= 90 degrees -> dot <= 0
	isAngleAtLeast90 := func(v1, v2 Point) bool {
		return dot(v1, v2) <= 1e-9
	}

	// compute original perimeter
	origPerim := 0.0
	for i := 0; i < numberOfPoints; i++ {
		a := points[i]
		b := points[(i+1)%numberOfPoints]
		origPerim += length(b.sub(a))
	}

	// iterate each point as candidate to replace
	for i := 0; i < numberOfPoints; i++ {
		n := numberOfPoints
		A := points[(i-1+n)%n]
		prevA := points[(i-2+n)%n]
		P := points[i]
		B := points[(i+1)%n]
		nextB := points[(i+2)%n]

		// circle with diameter AB
		center := A.add(B).mul(0.5)
		r := length(B.sub(A)) * 0.5
		if r <= epsilon {
			continue
		}

		candidates := make([]Point, 0, 10)

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
			if dscr >= -1e-12 {
				if dscr < 0 {
					dscr = 0
				}
				t1 := (-bcoef + math.Sqrt(dscr)) / 2.0
				t2 := (-bcoef - math.Sqrt(dscr)) / 2.0
				candidates = append(candidates, A.add(u.mul(t1)))
				candidates = append(candidates, A.add(u.mul(t2)))
			}
		}

		// intersection of circle with line through B perpendicular to B-nextB (i.e., nextB-B)
		vB := nextB.sub(B)
		dB := perp(vB)
		if length(dB) > epsilon {
			u := dB.mul(1.0 / length(dB))
			o := B.sub(center)
			bcoef := 2 * dot(u, o)
			cc := dot(o, o) - r*r
			dscr := bcoef*bcoef - 4*cc
			if dscr >= -1e-12 {
				if dscr < 0 {
					dscr = 0
				}
				t1 := (-bcoef + math.Sqrt(dscr)) / 2.0
				t2 := (-bcoef - math.Sqrt(dscr)) / 2.0
				candidates = append(candidates, B.add(u.mul(t1)))
				candidates = append(candidates, B.add(u.mul(t2)))
			}
		}

		// intersection of circle with the continuation of the prevA-A line (line through A and prevA)
		vLineA := A.sub(prevA)
		if length(vLineA) > epsilon {
			u := vLineA.mul(1.0 / length(vLineA))
			o := A.sub(center)
			bcoef := 2 * dot(u, o)
			cc := dot(o, o) - r*r
			dscr := bcoef*bcoef - 4*cc
			if dscr >= -1e-12 {
				if dscr < 0 {
					dscr = 0
				}
				t1 := (-bcoef + math.Sqrt(dscr)) / 2.0
				t2 := (-bcoef - math.Sqrt(dscr)) / 2.0
				candidates = append(candidates, A.add(u.mul(t1)))
				candidates = append(candidates, A.add(u.mul(t2)))
			}
		}

		// intersection of circle with the continuation of the B-nextB line (line through B and nextB)
		vLineB := nextB.sub(B)
		if length(vLineB) > epsilon {
			u := vLineB.mul(1.0 / length(vLineB))
			o := B.sub(center)
			bcoef := 2 * dot(u, o)
			cc := dot(o, o) - r*r
			dscr := bcoef*bcoef - 4*cc
			if dscr >= -1e-12 {
				if dscr < 0 {
					dscr = 0
				}
				t1 := (-bcoef + math.Sqrt(dscr)) / 2.0
				t2 := (-bcoef - math.Sqrt(dscr)) / 2.0
				candidates = append(candidates, B.add(u.mul(t1)))
				candidates = append(candidates, B.add(u.mul(t2)))
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
			if !isConvexWithReplacement(points, i, cand) {
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

	fmt.Printf("%.12f\n", maxPerimeterIncrease)

}
