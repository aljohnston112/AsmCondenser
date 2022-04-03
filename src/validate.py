

def is_quoted(param):
    return param[0] == "\"" and param[-1] == "\""


def assert_quoted(param):
    assert is_quoted(param)


