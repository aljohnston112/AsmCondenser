import re

MacrosFromScan = []

MacroNames = []
Labels = []
Variables = []


def parse_macro_name(line):
    tokens = line.split(":")
    if len(tokens) == 2:
        if tokens[1].lower().strip() == "macro":
            name = tokens[0]
            return name
    print(line)
    raise Exception


def parse_macro(file, temp_file, line):
    from src.macro_pass import parse_macros, get_macros
    parse_macros()
    global MacrosFromScan
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
    macro_parser = MacroParser(temp_file, macro_lines, name=name)


class MacroParser:

    def __init__(self, temp_file, macro_lines, name=""):
        self.variables = []
        self.lines = []
        self.assignments = {}
        self.labels = []

        MacroNames.append(name)
        self.name = name
        self.tabs = 0
        self.write("def " + name + "(...) {")
        self.tabs = 1

        for stripped_line in macro_lines:
            from src.parse_line import get_comment
            comment = get_comment(stripped_line)
            line_without_comment = stripped_line.split(";")[0]
            if line_without_comment != "":
                if "assert " in line_without_comment:
                    self.parse_assert(line_without_comment)
                elif ":" == line_without_comment[-1]:
                    self.parse_label(line_without_comment)
                elif "if" == line_without_comment.strip()[:2]:
                    self.write_if(line_without_comment)
                elif "=" in line_without_comment:
                    self.parse_equal(line_without_comment)
                elif "redef" == line_without_comment[:5]:
                    self.parse_redef(line_without_comment)
                elif "else" == line_without_comment:
                    self.write_else()
                elif "endc" == line_without_comment:
                    self.tabs -= 1
                elif "endm" == line_without_comment.strip().lower():
                    self.end(temp_file)
                elif "db" == line_without_comment.split()[0]:
                    self.parse_db(line_without_comment)
                elif "equ" in line_without_comment.lower():
                    self.parse_equ(line_without_comment)
                elif "fail" in line_without_comment:
                    self.parse_fail(line_without_comment)
                elif "rsset" in line_without_comment:
                    self.parse_rsset(line_without_comment)
                elif line_without_comment.split()[0] in MacrosFromScan:
                    self.write(line_without_comment)
                else:
                    from src.temp import asm_parser
                    self.add_assignment(asm_parser.parse_asm(line_without_comment))
            if comment != "":
                temp_file.write(comment + "\n")

    def parse_label(self, line):
        tabs = self.tabs
        self.tabs = 0
        tokens = line.split(":")
        if tokens[1] == "":
            label = tokens[0]
            Labels.append(label)
            self.labels.append(label)
            self.write(label + ":")
            self.tabs = tabs
        else:
            print(tokens)
            raise Exception

    def parse_assert(self, line):
        self.write(line)

    def write_if(self, line_without_comment):
        self.write(line_without_comment.strip())
        self.tabs += 1

    def get_tabs(self):
        tabs = ""
        for _ in range(self.tabs):
            tabs += "\t"
        return tabs

    def write(self, param):
        self.lines.append(self.get_tabs() + param + "\n")

    def add_assignment(self, tokens):
        left = tokens[0].strip()
        right = tokens[1].strip()
        pattern = re.compile(r"(?<![\\\"])@(?![\\\"])")
        if re.match(pattern, left):
            left = left.replace("@", "pc")
        if left not in Variables:
            pattern = re.compile(r"((?<=[ ])|\b|)\\[1-9]((?=[ \n])|\b)")
            if not pattern.match(left):
                Variables.append(left)
        if "\"@\"" in right:
            right = right.replace("\"@\"", "")
            right.strip()
            if ", ," in right:
                right = right.replace(", ,")
            if right[-2:] == ", ":
                right = right[:-2]
        self.variables.append(right)
        self.variables.append(left)
        self.assignments[left] = right
        self.write(left + " = " + right)

    def parse_equal(self, line):
        tokens = line.split("=")
        if len(tokens) == 2:
            self.add_assignment(tokens)
        else:
            print(line)
            raise Exception

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
        for variable in self.variables:
            from src.validate import is_quoted
            if is_quoted(variable):
                from src.parse_include import strip_quotes
                variable = strip_quotes(variable)
            match = re.search(r"(^|(?<=[ \w(]))\\[1-9]((?=[ \n\w)])|$)", variable)
            if match and variable not in variables:
                variables.append(match[0])
        variables = ", ".join(variables).replace("\\", "p")
        self.lines[0] = self.lines[0].replace("...", variables)
        self.tabs -= 1
        self.write("}")
        for line_out in self.lines:
            pattern = re.compile(r"((?<=[ ])|\b|)\\((?=[ \n])|\b)")
            line_out = pattern.split(line_out)
            line_out = "p".join(list(filter(None, line_out)))
            temp_file.write(line_out)

    def parse_db(self, stripped_line):
        tokens = stripped_line.split()
        assert tokens[0] == "db"
        left = "ROM"
        right = stripped_line.split("db")[1]
        self.add_assignment([left, right])

    def parse_equ(self, line_without_comment):
        tokens = line_without_comment.split("EQU")
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
