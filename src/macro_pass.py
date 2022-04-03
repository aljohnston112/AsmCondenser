import glob

from config import PokecrystalFolder, TempFolder

Macro_List_File = TempFolder + "macro_list"


def main():
    asm_filenames = glob.glob(PokecrystalFolder + "/**/*.asm")
    with open(Macro_List_File, "w") as mf:
        for asm_filename in asm_filenames:
            with open(asm_filename) as f:
                for line in f:
                    if line.strip() != "":
                        if "MACRO" in line:
                            tokens = line.strip().split(":")
                            assert len(tokens) == 2
                            mf.write(tokens[0])
                            mf.write(",")
                            while "ENDM" not in line:
                                line = next(f).strip()
                        elif "EQUS" in line:
                            tokens = line.strip().split("EQUS")
                            mf.write(tokens[0].strip())
                            mf.write(",")
                        elif "EQU" in line:
                            tokens = line.strip().split("EQU")
                            mf.write(tokens[0].strip())
                            mf.write(",")
                        elif line.strip()[0] == ";":
                            pass
                        elif line.strip()[-1] == ":":
                            pass
                        elif "const" in line:
                            pass
                        elif "command" in line:
                            pass
                        else:
                            print(line)
                            break


if __name__ == "__macro_pass__":
    main()
