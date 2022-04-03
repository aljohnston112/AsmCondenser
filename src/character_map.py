

current_character_map = "main"
character_map_stack = {}
string_to_byte_dict = {}


def push_character_map():
    character_map_stack[current_character_map] = string_to_byte_dict


def parse_charmap(temp_file, stripped_line):
    import shlex
    args = shlex.split(stripped_line, posix=False)[1:]
    if len(args) == 3:
        args = args[0::2]
        string = args[0].strip()
        if string[-1] == ",":
            string = string[:-1]
        from src.validate import assert_quoted
        assert_quoted(string)
        from src.parse_main import strip_quotes
        string = strip_quotes(string)
        byte = args[1].strip()
        byte = bytes(byte.encode())
        string_to_byte_dict[string] = byte
    else:
        print(args)
        raise Exception