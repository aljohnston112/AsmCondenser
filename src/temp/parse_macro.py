import re


MacroNames = []
Labels = []
Variables = []


def parse_macro(file, temp_file, line):
    macro_lines = []
    cont = False
    stripped_line = line.strip()
    while stripped_line != "ENDM":
        if not cont:
            if stripped_line[-1] == "\\":
                cont = True
                line = stripped_line[:-1]
            macro_lines.append(line.strip())
        else:
            if stripped_line[-1] != "\\":
                cont = False
            else:
                stripped_line = stripped_line[:-1]
            macro_lines[-1] += stripped_line
        line = next(file)
        stripped_line = line.strip()
    macro_lines.append(line)
    macro_parser = MacroParser(temp_file, macro_lines)


class MacroParser:

    def __init__(self, temp_file, macro_lines):
        self.name = None
        self.variables = []
        self.lines = []
        self.tabs = 0
        self.assignments = {}
        self.labels = []
        for line in macro_lines:
            stripped_line = line.strip()
            if "assert" in line:
                self.add_line(stripped_line)
            elif ":" in line:
                self.parse_colon(stripped_line)
            elif "=" in line:
                self.parse_equal(stripped_line)
            elif "redef" in line.lower():
                self.parse_redef(stripped_line)
            elif "else" == stripped_line:
                self.write_else()
            elif "endc" == stripped_line:
                self.tabs -= 1
            elif "endm" == stripped_line.lower():
                self.end(temp_file)
            else:
                print(line)
                raise Exception

    def get_tabs(self):
        tabs = ""
        for _ in range(self.tabs):
            tabs += "\t"
        return tabs

    def add_line(self, param):
        self.lines.append(self.get_tabs() + param + "\n")

    def parse_colon(self, line):
        tokens = line.split(":")
        if len(tokens) == 2:
            if tokens[1].strip().lower() == "macro":
                name = tokens[0].strip()
                MacroNames.append(name)
                self.name = name
                self.add_line("def " + name + "(...){")
                self.tabs += 1
            elif tokens[1] == "":
                label = tokens[0].strip()
                Labels.append(label)
                self.labels.append(label)
                tabs = self.tabs
                self.tabs = 0
                self.add_line(label + ":")
                self.tabs = tabs
            else:
                print(tokens)
                raise Exception
        else:
            print(tokens)
            raise Exception

    def add_assignment(self, tokens):
        left = tokens[0].strip()
        right = tokens[1].strip()
        if left not in Variables:
            Variables.append(left)
        self.variables.append(right)
        self.assignments[left] = right
        self.add_line(left + "= " + right)

    def parse_equal(self, line):
        tokens = line.split("=")
        if len(tokens) == 2:
            self.add_assignment(tokens)
        elif len(tokens) == 3:
            assert tokens[1] == ""
            self.add_line(line + ":")
            self.tabs += 1
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
        self.add_line("else:")
        self.tabs += 1

    def end(self, temp_file):
        variables = []
        for variable in self.variables:
            from src.validate import is_quoted
            if is_quoted(variable):
                from src.parse_include import strip_quotes
                variable = strip_quotes(variable)
            pattern = re.compile(r"(?<![\w\\])(?=[\w\\])\\[1-9]\b")
            if pattern.match(variable) and variable not in variables:
                variables.append(variable)
        variables = ", ".join(variables).replace("\\", "p")
        self.lines[0] = self.lines[0].replace("...", variables)
        self.tabs -= 1
        self.add_line("}")
        for line_out in self.lines:
            temp_file.write(line_out)
