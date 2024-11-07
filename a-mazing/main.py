from sys import stdout

MOVE_NAMES = ("up\n", "left\n", "down\n", "right\n")
MOVE_CAUSED_MOVEMENT = (-203, -1, 203, 1)
MOVE_POSSIBLE_NEXT_MOVES = ((3, 0, 1), (0, 1, 2), (1, 2, 3), (2, 3, 0))
MOVE_REVERSE_MOVES = (2, 3, 0, 1)

visited = [False] * (203 * 203)


def input_bf_fc():
    return ord(input()[0])

def input_bf():
    return input()

def dfs_base(curr_cell):
    visited[curr_cell] = True

    for move in range(4):
        dest_cell = curr_cell + MOVE_CAUSED_MOVEMENT[move]
        if visited[dest_cell]:
            continue
        stdout.write(MOVE_NAMES[move])
        stdout.flush()
        match input_bf_fc():
            case 111:  # ok
                dfs(dest_cell, move)
            case 115:  # solved
                exit(0)


def dfs(curr_cell, incoming_move):
    visited[curr_cell] = True

    for move in MOVE_POSSIBLE_NEXT_MOVES[incoming_move]:
        dest_cell = curr_cell + MOVE_CAUSED_MOVEMENT[move]
        if visited[dest_cell]:
            continue
        stdout.write(MOVE_NAMES[move])
        stdout.flush()
        match input_bf_fc():
            case 111:  # ok
                dfs(dest_cell, move)
            case 115:  # solved
                exit(0)

    stdout.write(MOVE_NAMES[MOVE_REVERSE_MOVES[incoming_move]])
    stdout.flush()
    input_bf()  # input must be "ok" as I'm returning to a previous cell


def main():
    dfs_base(101 * 203 + 101)

    print("no way out")
    if input_bf_fc() == 115:  # solved
        exit(0)
    else:
        exit(1)


if __name__ == "__main__":
    main()