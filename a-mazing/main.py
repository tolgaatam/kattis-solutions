from enum import IntEnum


class Move(IntEnum):
    UP = 0
    LEFT = 1
    DOWN = 2
    RIGHT = 3

    def __str__(self):
        return self.name.lower()

    def reverse(self):
        return Move((self.value + 2) % 4)

    def possible_next_moves(self):
        match self:
            case Move.UP:
                return Move.UP, Move.LEFT, Move.RIGHT
            case Move.LEFT:
                return Move.LEFT, Move.DOWN, Move.UP
            case Move.DOWN:
                return Move.DOWN, Move.RIGHT, Move.LEFT
            case _:  # RIGHT
                return Move.RIGHT, Move.UP, Move.DOWN

    def caused_movement(self):
        match self:
            case Move.UP:
                return -203
            case Move.LEFT:
                return -1
            case Move.DOWN:
                return 203
            case _:  # RIGHT
                return 1


visited = [False for i in range(203*203)]


def find_destination(cell, next_move):
    return cell + next_move.caused_movement()


def dfs_base(curr_cell):
    visited[curr_cell] = True

    for move in Move:
        dest_cell = find_destination(curr_cell, move)
        if visited[dest_cell]:
            continue
        print(move)
        match input()[0]:
            case "o":  # ok
                dfs(dest_cell, move)
            case "s":  # solved
                exit(0)


def dfs(curr_cell, incoming_move):
    visited[curr_cell] = True

    for move in incoming_move.possible_next_moves():
        dest_cell = find_destination(curr_cell, move)
        if visited[dest_cell]:
            continue
        print(move)
        match input()[0]:
            case "o":  # ok
                dfs(dest_cell, move)
            case "s":  # solved
                exit(0)

    print(incoming_move.reverse())
    input()  # input must be "ok" as I'm returning to a previous cell


def main():
    dfs_base(101 * 203 + 101)

    print("no way out")
    if input()[0] == "s":  # solved
        exit(0)
    else:
        exit(1)


if __name__ == "__main__":
    main()
