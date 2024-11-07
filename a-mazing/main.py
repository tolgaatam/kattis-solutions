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
                return -1, 0
            case Move.LEFT:
                return 0, -1
            case Move.DOWN:
                return 1, 0
            case _:  # RIGHT
                return 0, 1


class Cell:
    def __init__(self, r, c):
        self.row = r
        self.column = c
        self.visited = False


maze = [[Cell(i, j) for j in range(203)] for i in range(203)]


def find_destination(cell, next_move):
    m_row, m_col = next_move.caused_movement()
    return maze[cell.row + m_row][cell.column + m_col]


def dfs_base(curr_cell):
    curr_cell.visited = True

    for move in Move:
        dest_cell = find_destination(curr_cell, move)
        if dest_cell.visited:
            continue
        print(move)
        match input()[0]:
            case "o":  # ok
                dfs(dest_cell, move)
            case "s":  # solved
                exit(0)


def dfs(curr_cell, incoming_move):
    curr_cell.visited = True

    for move in incoming_move.possible_next_moves():
        dest_cell = find_destination(curr_cell, move)
        if dest_cell.visited:
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
    dfs_base(maze[101][101])

    print("no way out")
    if input()[0] == "s":  # solved
        exit(0)
    else:
        exit(1)


if __name__ == "__main__":
    main()
