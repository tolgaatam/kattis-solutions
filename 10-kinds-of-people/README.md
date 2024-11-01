This solution solves the problem in 0.02 seconds. As there are a lot of 0.01second solutions, I do not show up in the leaderboard yet.

Further improvement ideas:

- Put a fence of width-1 around the board to make the inner part of BFS leaner.
- Store pair<int, int> in the queue rather than a compound integer
- Access matrices directly like `matrix[r][c]` , rather than `matrix[ACC(r,c)]`

NOTE: Applied all the techniques above, still at 0.02 seconds. Giving up :)