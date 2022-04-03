

def get_comment(line):
    try:
        return line[line.index(";"):]
    except ValueError:
        return ""


def parse_line(file, temp_file, line):
    stripped_line = line.strip()
    if stripped_line != "":
        command = stripped_line.split()[0].lower()
        line_without_comment = line.split(";")[0]
        comment = get_comment(line)
        from src.character_map import character_map_conductor
        if command == "include":
            from src.parse_include import parse_include
            parse_include(line_without_comment)
        elif stripped_line[0] == ";":
            pass
        elif command == "charmap":
            if line_without_comment[-1] == "\"":
                line_without_comment += ";" + line.split(";")[1]
            character_map_conductor.parse_charmap(temp_file, line_without_comment)
        elif command == "pushc":
            character_map_conductor.push_character_map()
        elif command == "newcharmap":
            character_map_conductor.new_character_map(line_without_comment)
        elif command == "popc":
            character_map_conductor.pop_character_map()
        elif stripped_line.split()[1].lower() == "macro":
            from src.temp.parse_macro import parse_macro
            parse_macro(file, temp_file, line)
        else:
            print(line)
            raise Exception
        temp_file.write(comment)
    else:
        temp_file.write(line)
