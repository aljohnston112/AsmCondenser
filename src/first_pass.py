import glob
import os
from collections import defaultdict

import pandas as pandas

from macro_pass import Macro_List_File
from src.config import PokecrystalFolder, TempFolder

RoutineName = "_UpdateSound::"
TempFile = TempFolder + RoutineName


def find_line_number_in_file(filename, to_find):
    found_on_line_number = -1
    with open(filename) as asm_file:
        for line_number, line in enumerate(asm_file):
            line = line.strip()
            if to_find in line:
                found_on_line_number = line_number
    return found_on_line_number


def find_string_in_files(filenames, to_find):
    file_to_line_numbers = defaultdict(lambda: [])
    for filename in filenames:
        found_on_line = find_line_number_in_file(filename, to_find)
        if found_on_line != -1:
            file_to_line_numbers[filename].append(found_on_line)
    return file_to_line_numbers


def get_filename_and_line_number_of_routine(folder, routine_name):
    asm_filenames = glob.glob(folder + "/**/*.asm")
    filename_to_line_numbers = find_string_in_files(asm_filenames, routine_name)
    assert len(filename_to_line_numbers) == 1
    filename, lines = filename_to_line_numbers.popitem()
    assert len(lines) == 1
    line_number = lines[0]
    return filename, line_number


def get_readable_filenames(folder):
    filenames = []
    for subdir, dirs, files in os.walk(folder):
        for file in files:
            ext = file.split(".")[-1]
            if "." in file and \
                    ext != "pack" and ext != "idx" and ext != "bin" and ext != "png" and ext != "tilemap" and \
                    ext != "attrmap" and ext != "rle":
                filenames.append(os.path.abspath(os.path.join(subdir, file)))
    return filenames


def get_filename_and_line_number_of_string(folder, to_find):
    filenames = get_readable_filenames(folder)
    filename_to_line_numbers = find_string_in_files(filenames, to_find)
    return filename_to_line_numbers


def skip_and_return_comments(f, line_number):
    lines = []
    comments = []
    for _ in range(line_number + 1):
        lines.append(next(f))
    for i, line in enumerate(reversed(lines)):
        if i == 0 or line[0] == ";" or line.isspace():
            comments.insert(0, line)
        else:
            break
    return comments


def get_macros():
    return pandas.read_csv(Macro_List_File)


def write_comment(of, line):
    of.write(line)
    of.write("\n")


def write_ld(of, tokens):
    if len(tokens) == 3:
        of.write(tokens[1].strip()[:-1])
        of.write(" = ")
        of.write(tokens[2].strip())
    elif len(tokens) > 3:
        index = 0
        for i, token in enumerate(tokens):
            if ',' in token:
                index = i
        lhs = []
        for i in range(1, index + 1):
            lhs.extend(tokens[index].strip())
        lhs = lhs[:-1]
        of.write(lhs[0])
        of.write(" = ")
        rhs = []
        for i in range(index + 1, len(tokens)):
            rhs.extend(tokens[index].strip())
        of.write(rhs[0])
    else:
        print(len(tokens))
        print(tokens)
        raise Exception
    of.write("\n")


def write_and(of, tokens):
    assert len(tokens) == 2
    if len(tokens) == 2:
        of.write("A = (A or ")
        of.write(tokens[1].strip())
        of.write(")")
        of.write("\n")
        of.write("z = not A")
        of.write("\n")
        of.write("n = 0")
        of.write("\n")
        of.write("h = 1")
        of.write("\n")
        of.write("c = 0")
    of.write("\n")


def write_ret(of, tokens):
    assert len(tokens) == 2 or len(tokens) == 1
    if len(tokens) == 2:
        of.write("return if ")
        of.write(tokens[1].strip())
        of.write("\n")
    elif len(tokens) == 1:
        of.write("return")
        of.write("\n")


def write_xor(of, tokens):
    assert len(tokens) == 2
    if len(tokens) == 2:
        of.write("z = not ")
        of.write(tokens[1].strip())
        of.write("\n")
        of.write("n = 0")
        of.write("\n")
        of.write("h = 0")
        of.write("\n")
        of.write("c = 0")
    of.write("\n")


def write_label(of, line):
    of.write(line)
    of.write("\n")


def write_add(of, tokens):
    assert len(tokens) == 3 or len(tokens) == 2
    if len(tokens) == 2:
        tokens.append(tokens[-1])
    left = tokens[1].strip()
    if left[-1] == ",":
        left = left[:-1]
    of.write(left)
    of.write(" += ")
    of.write(tokens[2].strip())
    of.write("\n")
    if left == "hl":
        of.write("n = 0")
        of.write("\n")
        of.write("h = carry from bit 11")
        of.write("\n")
        of.write("c = carry from bit 15")
    elif left == "a":
        of.write("z if not ")
        of.write(left)
        of.write("\n")
        of.write("n = 0")
        of.write("\n")
        of.write("h = carry from bit 3")
        of.write("\n")
        of.write("c = carry from bit 7")
    else:
        raise Exception
    of.write("\n")


def write_bit(of, tokens):
    assert len(tokens) == 3
    of.write("z = not bit ")
    bit = tokens[1].strip()
    if bit[-1] == ",":
        bit = bit[:-1]
    of.write(bit)
    of.write(" of ")
    of.write(tokens[2].strip())
    of.write("\n")
    of.write("n = 0")
    of.write("\n")
    of.write("h = 1")
    of.write("\n")


def write_jp(of, tokens):
    assert len(tokens) == 3 or len(tokens) == 2
    if len(tokens) == 3:
        flag = tokens[1].strip()
        if flag[-1] == ",":
            flag = flag[:-1]
        of.write("if ")
        if flag == "nz" or flag == "nc":
            of.write("not ")
            flag = flag[1:]
        elif flag != "z" and flag != "c":
            print(tokens)
            raise Exception
        of.write(flag)
        of.write(" goto ")
        of.write(tokens[2].strip())
    elif len(tokens) == 2:
        of.write("goto ")
        of.write(tokens[1].strip())
    of.write("\n")


def write_cp(of, tokens):
    assert len(tokens) == 2
    if len(tokens) == 2:
        of.write("z = ((a - ")
        of.write(tokens[1].strip())
        of.write(") == 0)")
        of.write("\n")
        of.write("n = 1")
        of.write("\n")
        of.write("h = no borrow from bit 4")
        of.write("\n")
        of.write("c = (a < ")
        of.write(tokens[1].strip())
    of.write(")\n")


def write_jr(of, tokens):
    assert len(tokens) == 3 or len(tokens) == 2
    if len(tokens) == 3:
        flag = tokens[1].strip()
        if flag[-1] == ",":
            flag = flag[:-1]
        of.write("if ")
        if flag == "nc" or flag == 'nz':
            of.write("not ")
            flag = flag[1:]
        elif flag != "c" and flag != 'z':
            print(tokens)
            raise Exception
        of.write(flag)
        of.write(" goto ")
        of.write(tokens[2].strip())
    else:
        of.write("goto ")
        of.write(tokens[1].strip())
    of.write("\n")


def write_dec(of, tokens):
    assert len(tokens) == 2
    var = tokens[1].strip()
    if var != "bc" and var != "de" and var != "hl" and var != "sp":
        of.write(var)
        of.write("--")
        of.write("\n")
        of.write("z = not ")
        of.write(var)
        of.write("\n")
        of.write("n = 1")
        of.write("\n")
        of.write("h = no borrow from bit 4")
    else:
        raise Exception
    of.write("\n")


def write_res(of, tokens):
    assert len(tokens) == 3
    bit = tokens[1].strip()
    if bit[-1] == ",":
        bit = bit[:-1]
    if bit == "z":
        of.write("Reset bit ")
        of.write(bit)
        of.write(" of ")
        of.write(tokens[2].strip())
    of.write("\n")


def write_call(of, tokens):
    assert len(tokens) == 2
    for token in tokens:
        of.write(token)
        of.write(" ")
    of.write("\n")


def write_set(of, tokens):
    assert len(tokens) == 3
    bit = tokens[1].strip()
    if bit[-1] == ",":
        bit = bit[:-1]
    if bit == "z":
        of.write("Set bit ")
        of.write(bit)
        of.write(" of ")
        of.write(tokens[2].strip())
    of.write("\n")


def write_or(of, tokens):
    assert len(tokens) == 2
    if len(tokens) == 2:
        of.write("A = (A or ")
        of.write(tokens[1].strip())
        of.write(")")
        of.write("\n")
        of.write("z = not A")
        of.write("\n")
        of.write("n = 0")
        of.write("\n")
        of.write("h = 0")
        of.write("\n")
        of.write("c = 0")
    of.write("\n")


def write_inc(of, tokens):
    assert len(tokens) == 2
    var = tokens[1].strip()
    if var != "bc" and var != "de" and var != "hl" and var != "sp":
        of.write(var)
        of.write("++")
        of.write("\n")
        of.write("z = not ")
        of.write(var)
        of.write("\n")
        of.write("n = 0")
        of.write("\n")
        of.write("h = carry from bit 3")
    else:
        raise Exception
    of.write("\n")


def write_routine(of, tokens):
    assert len(tokens) == 1
    of.write(tokens[0])
    of.write("\n")


def write_push(of, tokens):
    assert len(tokens) == 2
    of.write("[sp] = ")
    of.write(tokens[1].strip())
    of.write("\n")
    of.write("sp-=2")
    of.write("\n")


def write_rept(f, of, tokens):
    assert len(tokens) == 2
    of.write("for _ in range(")
    of.write(tokens[1].strip())
    of.write(") {\n")


def write_pop(of, tokens):
    assert len(tokens) == 2
    of.write(tokens[1].strip())
    of.write(" = [sp]")
    of.write("\n")
    of.write("sp+=2")
    of.write("\n")


def write_sla(of, tokens):
    assert len(tokens) == 2
    of.write("c = bit 7 of ")
    of.write(tokens[1].strip())
    of.write("\n")
    of.write(tokens[1].strip())
    of.write("*=2")
    of.write("\n")
    of.write("z = not ")
    of.write(tokens[1].strip())
    of.write("\n")
    of.write("n = 1")
    of.write("\n")
    of.write("h = 1")
    of.write("\n")


def write_scf(of):
    of.write("n = 0")
    of.write("\n")
    of.write("h = 0")
    of.write("\n")
    of.write("c = 1")
    of.write("\n")


def main():
    macros = get_macros().columns.to_list()
    file, line_number = get_filename_and_line_number_of_routine(PokecrystalFolder, RoutineName)
    with open(file) as f:
        comments = skip_and_return_comments(f, line_number)
        with open(TempFile, "w") as of:
            for comment in comments:
                of.write(comment)
            for line in f:
                line = line.strip()
                tokens = (line.split(";")[0]).split()
                comment = line.split(";")[-1]
                if line != "" and line[0] != ";":
                    command = tokens[0].strip()
                    if "<<" in command:
                        pass
                        # TODO order of ops
                    if command == "ld" or command == "ldh":
                        write_ld(of, tokens)
                    elif command == "and":
                        write_and(of, tokens)
                    elif command == "ret":
                        write_ret(of, tokens)
                    elif command == "xor":
                        write_xor(of, tokens)
                    elif command[0] == ".":
                        write_label(of, line)
                    elif command == "add":
                        write_add(of, tokens)
                    elif command == "bit":
                        write_bit(of, tokens)
                    elif command == "jp":
                        write_jp(of, tokens)
                    elif command == "cp":
                        write_cp(of, tokens)
                    elif command == "jr":
                        write_jr(of, tokens)
                    elif command == "dec":
                        write_dec(of, tokens)
                    elif command == "res":
                        write_res(of, tokens)
                    elif command == "call":
                        write_call(of, tokens)
                    elif command == "set":
                        write_set(of, tokens)
                    elif command == "or":
                        write_or(of, tokens)
                    elif command == "inc":
                        write_inc(of, tokens)
                    elif command == "push":
                        write_push(of, tokens)
                    elif command == "pop":
                        write_pop(of, tokens)
                    elif command == "sla":
                        write_sla(of, tokens)
                    elif command == "scf":
                        assert len(tokens) == 1
                        write_scf(of)
                    elif command == "rept":
                        write_rept(f, of, tokens)
                    elif command == "endr":
                        of.write("}\n")
                    elif command[-1] == ":":
                        write_routine(of, tokens)
                    elif tokens[0] in macros:
                        of.write(line)
                        of.write("\n")
                    elif "dw" in line:
                        of.write(line)
                        of.write("\n")
                    else:
                        print(line)
                        raise Exception
                else:
                    write_comment(of, line)
                    comment = ""
                if not comment.isspace() and ";" in comment:
                    write_comment(of, ";" + comment)


main()
