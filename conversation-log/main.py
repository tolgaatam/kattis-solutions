from collections import defaultdict, Counter

n = int(input())
word_total_count_map = defaultdict(int)
word_unique_user_count_map = defaultdict(int)
user_word_corpus_map = defaultdict(str)

for _ in range(n):
	[username, rest] = input().split(" ", 1) 
	user_word_corpus_map[username] += " " + rest

for username, word_corpus in user_word_corpus_map.items():
	word_list = word_corpus[1:].split(" ")
	word_counts = Counter(word_list)
	for word, count in word_counts.items():
		word_unique_user_count_map[word] += 1
		word_total_count_map[word] += count

user_count = len(user_word_corpus_map)
winning_words = [word for word, words_unique_user_count in word_unique_user_count_map.items() if words_unique_user_count == user_count]
winning_words.sort(key=lambda word: (-1*word_total_count_map[word] , word))

str_to_print = "\n".join(winning_words)

if len(str_to_print) == 0:
	print("ALL CLEAR")
else:
	print(str_to_print)