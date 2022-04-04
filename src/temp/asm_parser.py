

def parse_ld(line):
    if "ld" == line[:2]:
        line = line[2:].strip()
        tokens = line.split(",")
        if len(tokens) == 2:
            left = tokens[0].strip()
            right = tokens[1].strip()
            return [left, right]
        else:
            print(line)
            raise Exception
    else:
        print(line)
        raise Exception


def parse_asm(line):
    command = line.split()[0].strip()
    if command == "ld" or command == "ldh":
        return parse_ld(line)
    else:
        print(line)
        raise Exception
