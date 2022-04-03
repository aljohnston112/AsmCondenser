import os.path


def strip_quotes(param):
    return param[1:-1]


def parse_include(line):
    tokens = line.split()
    assert len(tokens) == 2
    param = tokens[1]
    from src.validate import assert_quoted
    assert_quoted(param)
    param = strip_quotes(param)
    from src.config import PokecrystalFolder
    file_name = PokecrystalFolder + param
    with open(file_name, "r") as file:
        from src.config import TempFolder
        temp_file_name = TempFolder + param
        if "/" in param:
            folder = "/".join(param.split("/")[:-1])
            folder = TempFolder + folder + "/"
            if not os.path.exists(folder):
                os.makedirs(folder)
        with open(temp_file_name, "w") as temp_file:
            for line in file:
                from src.parse_line import parse_line
                parse_line(file, temp_file, line)


if __name__ == "__main__":
    from src.config import MainInclude
    parse_include(MainInclude)
