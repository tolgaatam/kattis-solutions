from sys import exit
from enum import IntEnum


class Move(IntEnum):
    UP = 0
    LEFT = 1
    DOWN = 2
    RIGHT = 3

    def __str__(self):
        return self.name.lower()


class Cell:
    def __init__(self, r, c):
        self.row = r
        self.column = c
        self.visited = False


maze = [[Cell(i, j) for j in range(203)] for i in range(203)]


def find_destination(cell, next_move):
    match next_move:
        case Move.UP:
            return maze[cell.row + 1][cell.column]
        case Move.LEFT:
            return maze[cell.row][cell.column - 1]
        case Move.DOWN:
            return maze[cell.row - 1][cell.column]
        case _:  # right
            return maze[cell.row][cell.column + 1]


def find_move_anti(given_move):
    return Move((given_move.value + 2) % 4)


def dfs_base(curr_cell):
    curr_cell.visited = True

    for move in Move:
        dest_cell = find_destination(curr_cell, move)
        if dest_cell.visited:
            continue
        print(move)
        match input()[0]:
            case "s":  # solved
                exit(0)
            case "w":  # wall
                continue
            case "o":  # ok
                dfs(dest_cell, move)
            case _:  # wrong
                exit(1)


def dfs(curr_cell, incoming_move):
    curr_cell.visited = True
    reverse_incoming_move = find_move_anti(incoming_move)

    for move in Move:
        if move == reverse_incoming_move:
            continue
        dest_cell = find_destination(curr_cell, move)
        if dest_cell.visited:
            continue
        print(move)
        match input()[0]:
            case "s":  # solved
                exit(0)
            case "w":  # wall
                continue
            case "o":  # ok
                dfs(dest_cell, move)
            case _:  # wrong
                exit(1)

    print(reverse_incoming_move)
    input()  # input must be "ok" as I'm returning to a previous cell


def main():
    dfs_base(maze[101][101])

    print("no way out")
    if input() == "solved":
        exit(0)
    else:
        exit(1)


if __name__ == "__main__":
    main()