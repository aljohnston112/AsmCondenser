; Macros to verify assumptions about the data or code

def table_width(p1, p2){
	CURRENT_TABLE_WIDTH= \1
	if _NARG == 2:
		CURRENT_TABLE_START= "\2"
	else:
		CURRENT_TABLE_START= "._table_width\@"
{CURRENT_TABLE_START}:
}

assert_table_length: MACRO
x= \1
assert x * CURRENT_TABLE_WIDTH == @ - {CURRENT_TABLE_START},"{CURRENT_TABLE_START}: expected {d:x} entries, each {d:CURRENT_TABLE_WIDTH} bytes"
}

def list_start(p1){
	list_index= 0
	if _NARG == 1:
		CURRENT_LIST_START= "\1"
	else:
		CURRENT_LIST_START= "._list_start\@"
{CURRENT_LIST_START}:
}

