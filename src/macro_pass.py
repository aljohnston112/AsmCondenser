from src.config import TempFolder

Macro_List_File = TempFolder + "macro_list"


def get_macros():
    import pandas
    return pandas.read_csv(Macro_List_File).columns.to_list()


def parse_macros():
    import glob
    from src.config import PokecrystalFolder
    asm_filenames = glob.glob(PokecrystalFolder + "**/*.asm", recursive=True)
    with open(Macro_List_File, "w") as mf:
        for asm_filename in asm_filenames:
            with open(asm_filename) as f:
                for line in f:
                    line = line.split(";")[0]
                    if line.strip() != "":
                        tokens = line.strip().split()
                        if len(tokens) == 2:
                            if "macro" == tokens[1].strip().lower() and ":" == tokens[0].strip()[-1:]:
                                mf.write(tokens[0].strip()[:-1])
                                mf.write(",")
                                while "ENDM" not in line:
                                    line = next(f).strip()


if __name__ == "__main__":
    parse_macros()
