n = int(input())
word_total_count_map = {}
word_unique_user_count_map = {}
user_wordset_map = {}

for user_idx in range(n):
	line_elements = input().split()
	username = line_elements[0]
	wordlist = line_elements[1:]

	if username in user_wordset_map:
		wordset = user_wordset_map[username]
	else:
		wordset = set()
		user_wordset_map[username] = wordset

	for word in wordlist:
		word_seen_before = False
		if word in word_total_count_map:
			word_seen_before = True
			word_total_count_map[word] += 1
		else:
			word_total_count_map[word] = 1

		if word not in wordset:
			wordset.add(word)
			if word_seen_before:
				word_unique_user_count_map[word] += 1
			else:
				word_unique_user_count_map[word] = 1

user_count = len(user_wordset_map)
winning_words = sorted([(-1*word_total_count_map[k] , k) for k,v in word_unique_user_count_map.items() if v == user_count])

if len(winning_words) == 0:
	print("ALL CLEAR")
else:
	for word in winning_words:
		print(word[1])