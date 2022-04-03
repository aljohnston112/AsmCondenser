

class CharacterMapConductor:
    def __init__(self):
        self.current_character_map_name = "main"
        self.string_to_byte_dict = {}
        self.character_map_stack = []

    def pop_character_map(self):
        character_map = self.character_map_stack.pop()
        self.current_character_map_name = character_map.name
        self.string_to_byte_dict = character_map.string_to_byte_dict

    def new_character_map(self, line):
        tokens = line.split()
        if len(tokens) == 2:
            current_character_map = tokens[1]
            self.current_character_map_name = current_character_map
            character_map = CharacterMap(self.current_character_map_name, self.string_to_byte_dict)
            self.character_map_stack.append(character_map)
            self.current_character_map_name = ""
        else:
            print(tokens)
            raise Exception

    def push_character_map(self):
        character_map = CharacterMap(self.current_character_map_name, self.string_to_byte_dict)
        self.character_map_stack.append(character_map)
        self.string_to_byte_dict = {}

    def parse_charmap(self, temp_file, line):
        import shlex
        args = shlex.split(line, posix=False)[1:]
        if len(args) == 3:
            args = args[0::2]
            string = args[0].strip()
            if string[-1] == ",":
                string = string[:-1]
            from src.validate import assert_quoted
            assert_quoted(string)
            from src.parse_include import strip_quotes
            string = strip_quotes(string)
            byte = args[1].strip()
            byte = bytes(byte.encode())
            self.string_to_byte_dict[string] = byte
        else:
            print(args)
            raise Exception


class CharacterMap:
    def __init__(self, name, string_to_byte_dict):
        self.name = name
        self.string_to_byte_dict = string_to_byte_dict


character_map_conductor = CharacterMapConductor()
