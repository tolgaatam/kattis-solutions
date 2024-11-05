I decided to implement this in Kotlin as a challenge. I don't think that Java and Kotlin are best-suited for Kattis problems, and general runtime statistics at Kattis proves so. I assume that Kattis measures the time interval that JVM is spinning up and warming too, which makes Java/Kotlin look really slow.

`naive.kt` and `reverse.kt` is too slow to pass the tests. `graphlike.kt` passed the challenge with 0.86 seconds. Further optimizations might be possible.
