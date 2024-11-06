from sys import stdout, exit


class Cell:
    def __init__(self, r, c):
        self.row = r
        self.column = c
        self.walls = [False] * 4  # True -> wall
        self.visited = False


class StackElement:
    def __init__(self, cell, incoming_move):
        self.cell = cell
        self.incoming_move = incoming_move


maze = [[Cell(i, j) for j in range(203)] for i in range(203)]


def find_destination(cell, next_move):
    match next_move:
        case 0:  # up
            return maze[cell.row + 1][cell.column]
        case 1:  # left
            return maze[cell.row][cell.column - 1]
        case 2:  # down
            return maze[cell.row - 1][cell.column]
        case _:  # right
            return maze[cell.row][cell.column + 1]


def find_move_anti(given_move):
    return (given_move + 2) % 4


def output(s):
    stdout.write(s + '\n')
    stdout.flush()


move_names = ["down", "left", "up", "right"]

def dfs_base(curr_cell):
    curr_cell.visited = True

    for move in range(4):
        if curr_cell.walls[move]:
            continue
        dest_cell = find_destination(curr_cell, move)
        if dest_cell.visited:
            continue
        output(move_names[move])
        match input():
            case "solved":
                exit(0)
            case "wall":
                curr_cell.walls[move] = True
            case "ok":
                dfs(dest_cell, move)
            case _:  # wrong
                exit(1)

def dfs(curr_cell, incoming_move):
    curr_cell.visited = True
    reverse_incoming_move = find_move_anti(incoming_move)

    for move in range(4):
        if move == reverse_incoming_move:
            continue
        if curr_cell.walls[move]:
            continue
        dest_cell = find_destination(curr_cell, move)
        if dest_cell.visited:
            continue
        output(move_names[move])
        match input():
            case "solved":
                exit(0)
            case "wall":
                curr_cell.walls[move] = True
            case "ok":
                dfs(dest_cell, move)
            case _:  # wrong
                exit(1)

    output(move_names[reverse_incoming_move])
    input()  # input must be "ok" as I'm returning to a previous cell


def main():
    dfs_base(maze[101][101])

    output("no way out")
    response = input()
    if response == "solved":
        exit(0)
    else:
        exit(1)


if __name__ == "__main__":
    main()
