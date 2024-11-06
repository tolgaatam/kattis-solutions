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
            return maze[cell.row - 1][cell.column]
        case 1:  # right
            return maze[cell.row][cell.column + 1]
        case 2:  # down
            return maze[cell.row + 1][cell.column]
        case _:  # left
            return maze[cell.row][cell.column - 1]


def find_move_anti(given_move):
    return (given_move + 2) % 4


move_names = ["down", "left", "up", "right"]


def main():
    stack = [StackElement(maze[101][101], None)]
    while True:
        stack_object = stack[-1]
        curr_cell = stack_object.cell
        incoming_move = stack_object.incoming_move
        curr_cell.visited = True
        move_chosen = None
        for move in filter(lambda m: (not find_destination(curr_cell, m).visited) and (not curr_cell.walls[m]), range(4)):
            stdout.write(move_names[move] + '\n')
            stdout.flush()
            response = input()
            match response:
                case "solved":
                    exit(0)
                case "wall":
                    curr_cell.walls[move] = True
                case "ok":
                    move_chosen = move
                    break
                case _:  # wrong
                    exit(1)

        if move_chosen is not None:
            stack.append(StackElement(find_destination(curr_cell, move_chosen), move_chosen))
        else:
            if incoming_move is None:
                stdout.write("no way out\n")
                stdout.flush()
                response = input()
                if response == "solved":
                    exit(0)
                else:
                    exit(1)

            stdout.write(move_names[find_move_anti(incoming_move)] + '\n')
            stdout.flush()
            response = input()
            match response:
                case "ok":
                    stack.pop()
                case "solved":  # not expected
                    exit(0)
                case _:  #
                    exit(1)


if __name__ == "__main__":
    main()
