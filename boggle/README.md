This problem needs to be solved by using a trie data structure for efficient search of words and prefixes. 

I wasn't sure in the beginning if using Go was the best choice, but the result worked out brilliant. Now, the runtime became as low as 0.40seconds, which is the 3rd best of all.

Removing some pointer passing might help with performance even further, as Go manages stack better than heap.