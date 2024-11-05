This problem needs to be solved by using a trie data structure for efficient search of words and prefixes. 

I do not know if using Go was the most brilliant choice, but the result worked out fine. For now, the runtime became as low as 0.73seconds.

Removing some pointer passing might help with performance, as Go manages stack better than heap.