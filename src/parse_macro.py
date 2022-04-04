import re

from parse_rgbasm import parse_global_label, parse_if, parse_equal
from src.temp.asm_parser import assert_start_and_strip


MacrosFromScan = []

MacroNames = []
Labels = []


def parse_macro_name(line):
    tokens = line.split(":")
    if len(tokens) == 2:
        if tokens[1].lower().strip() == "macro":
            name = tokens[0]
            return name
    print(line)
    raise Exception





def parse_lines(macro_parser, macro_lines):
    for line in macro_lines:
        stripped_line = line.strip()
        from src.parse_line import get_comment
        comment = get_comment(stripped_line)
        line_without_comment = stripped_line.split(";")[0]
        if "" != line_without_comment:
            if starts_with(line_without_comment, "assert"):
                macro_parser.write_assert(line_without_comment)
            elif starts_with(line_without_comment, "if"):
                line = parse_if(line_without_comment)
                macro_parser.write_if(line)
            elif ":" == line_without_comment[-1]:
                line = parse_global_label(line_without_comment)
                macro_parser.write_label(line)
            elif "=" in line_without_comment:
                line = parse_equal(line_without_comment)
            elif starts_with(line_without_comment, "redef"):
                self.parse_redef(line_without_comment)
            elif starts_with(line_without_comment, "else"):
                self.write_else()
            elif starts_with(line_without_comment, "endc"):
                self.tabs -= 1
            elif starts_with(line_without_comment, "endm"):
                self.end(temp_file)
            elif starts_with(line_without_comment, "db"):
                self.parse_db(line_without_comment)
            elif starts_with(line_without_comment, "dw"):
                self.parse_dw(line_without_comment)
            elif "equ" in line_without_comment.lower():
                self.parse_equ(line_without_comment)
            elif "equs" in line_without_comment.lower():
                self.parse_equs(line_without_comment)
            elif starts_with(line_without_comment, "fail"):
                self.parse_fail(line_without_comment)
            elif starts_with(line_without_comment, "rsset"):
                self.parse_rsset(line_without_comment)
            elif line_without_comment.split()[0] in MacrosFromScan:
                self.parse_macro_call(line_without_comment)
            else:
                from src.temp import asm_parser
                if starts_with(line_without_comment, "ldhl"):
                    print(line_without_comment)
                    raise Exception
                elif starts_with(line_without_comment, "ldh"):
                    ldh = asm_parser.parse_ldh(line_without_comment)
                    tokens = ldh.split("=")
                    self.add_assignment(tokens)
                elif starts_with(line_without_comment, "ld"):
                    ld = asm_parser.parse_ld(line_without_comment)
                    tokens = ld.split("=")
                    self.add_assignment(tokens)
                elif starts_with(line_without_comment, "call"):
                    call = asm_parser.parse_call(line_without_comment)
                    self.write(call)
                elif starts_with(line_without_comment, "jp"):
                    jp = asm_parser.parse_jp(line_without_comment)
                    self.write(jp)
                elif starts_with(line_without_comment, "rst"):
                    rst = asm_parser.parse_rst(line_without_comment)
                    self.write(rst)
                elif starts_with(line_without_comment, "push"):
                    push = asm_parser.parse_push(line_without_comment)
                    self.write(push)
                elif starts_with(line_without_comment, "pop"):
                    pop = asm_parser.parse_pop(line_without_comment)
                    self.write(pop)
                else:
                    print(line_without_comment)
                    raise Exception
        if comment != "":
            temp_file.write(comment + "\n")


def parse_macro(file, temp_file, line):
    global MacrosFromScan
    from src.macro_pass import get_macros
    MacrosFromScan = get_macros()
    macro_lines = []
    cont = False
    stripped_line = line.strip()
    while stripped_line != "ENDM":
        if not cont:
            if stripped_line[-1] == "\\":
                cont = True
                stripped_line = stripped_line[:-1]
            macro_lines.append(stripped_line.strip())
        else:
            if stripped_line[-1] != "\\":
                cont = False
            else:
                stripped_line = stripped_line[:-1]
            macro_lines[-1] += stripped_line
        line = next(file)
        stripped_line = line.strip()
    macro_lines.append(line)
    name = parse_macro_name(macro_lines[0])
    macro_lines = macro_lines[1:]
    macro_parser = MacroParser(temp_file, name=name)
    parse_lines(macro_parser, macro_lines)


def starts_with(line, string):
    index = len(string)
    return string.lower() == line.lower()[:index]


class MacroParser:

    def __init__(self, temp_file, name=""):
        self.temp_file = temp_file
        MacroNames.append(name)
        self.name = name
        self.tabs = 0
        self.write("def " + name + "(...) {")
        self.tabs = 1

    def write(self, param):
        self.temp_file.write(self.get_tabs() + param + "\n")

    def write_if(self, line):
        self.write(line)
        self.tabs += 1

    def write_label(self, label):
        tabs = self.tabs
        self.tabs = 0
        Labels.append(label)
        self.write(label + ":")
        self.tabs = tabs

    def get_tabs(self):
        tabs = ""
        for _ in range(self.tabs):
            tabs += "\t"
        return tabs

    def add_assignment(self, tokens):
        left = tokens[0].strip()
        right = tokens[1].strip()
        # This is looking for an @ without quotes because
        # "@" is a character and @ is the program counter
        pattern = re.compile(r"(?<![\\\"])@(?![\\\"])")
        if re.match(pattern, left):
            left = left.replace("@", "PC")

        if "\"@\"" in right:
            # "@" is not a variable
            right = right.replace("\"@\"", "")
            right.strip()
            if ", ," in right:
                right = right.replace(", ,")
            if right[-2:] == ", ":
                right = right[:-2]
        self.write(left + " = " + right)

    def parse_redef(self, line):
        assert "EQUS" in line
        equs = line.split("REDEF")[1]
        tokens = equs.split("EQUS")
        if len(tokens) == 2:
            self.add_assignment(tokens)
        else:
            print(line)
            raise Exception

    def write_else(self):
        self.tabs -= 1
        self.write("else:")
        self.tabs += 1

    def end(self, temp_file):
        variables = []
        ugh = self.lines[0]
        for variable in self.variables:
            from src.validate import is_quoted
            if is_quoted(variable):
                from src.parse_include import strip_quotes
                variable = strip_quotes(variable)
            # Looking for macro arguments
            match = re.search(r"\\[1-9]", variable)
            if match and variable not in variables:
                variables.append(match[0])
        variables = ", ".join(variables).replace("\\", "p")
        self.lines[0] = self.lines[0].replace("...", variables)
        self.tabs -= 1
        self.write("}")
        for line_out in self.lines:
            # Looking to replace r"\" with "p" in macro arguments
            pattern = re.compile(r"\\(?=[1-9])")
            line_out = pattern.split(line_out)
            line_out = "p".join(list(filter(None, line_out)))
            temp_file.write(line_out)

    def parse_db(self, stripped_line):
        stripped_line = assert_start_and_strip(stripped_line, "db")
        self.write("ROM[SECTION].append(" + stripped_line + ")")

    def parse_dw(self, line):
        stripped_line = assert_start_and_strip(line, "dw")
        self.write("ROM[SECTION].append(" + stripped_line + ")")

    def parse_equ(self, line_without_comment):
        tokens = line_without_comment.split("EQU")
        assert len(tokens) == 2
        self.add_assignment(tokens)

    def parse_equs(self, line_without_comment):
        tokens = line_without_comment.split("EQUS")
        assert len(tokens) == 2
        self.add_assignment(tokens)

    def parse_fail(self, line_without_comment):
        assert "fail" == line_without_comment[:4]
        line_without_comment = line_without_comment.replace("fail", "assert False, ", 1)
        self.write(line_without_comment)

    def parse_rsset(self, line_without_comment):
        assert "rsset" == line_without_comment[:5]
        tokens = line_without_comment.split("rsset")
        assert len(tokens) == 2
        tokens[0] = "_RS"
        self.add_assignment(tokens)

    def parse_macro_call(self, line):
        tokens = line.split()
        if len(tokens) == 2:
            self.write(tokens[0] + "(" + tokens[1] + ")")
        else:
            print(line)
            raise Exception
