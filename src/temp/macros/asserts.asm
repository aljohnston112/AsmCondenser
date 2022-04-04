; Macros to verify assumptions about the data or code

def table_width(p1) {
	CURRENT_TABLE_WIDTH = p1
	if _NARG == 2
		REDEF CURRENT_TABLE_START = S "p2"
	else:
		REDEF CURRENT_TABLE_START = S "._table_width\@"
{CURRENT_TABLE_START}:
}

def assert_table_length(p1) {
	x = p1
	assert x * CURRENT_TABLE_WIDTH == @ - {CURRENT_TABLE_START},"{CURRENT_TABLE_START}: expected {d:x} entries, each {d:CURRENT_TABLE_WIDTH} bytes"
}

def list_start() {
	list_index = 0
	if _NARG == 1
		REDEF CURRENT_LIST_START = S "p1"
	else:
		REDEF CURRENT_LIST_START = S "._list_start\@"
{CURRENT_LIST_START}:
}

def li(p1) {
	assert !STRIN(p1, "@"), STRCAT("String terminator \"@\" in list entry: ", p1)
	ROM = p1
	list_index + = 1
}

def assert_list_length(p1) {
	x = p1
	assert x == list_index,"{CURRENT_LIST_START}: expected {d:x} entries, got {d:list_index}"
}

;\1: map id
def def_grass_wildmons() {
	REDEF CURRENT_GRASS_WILDMONS_MAP = S "p1"
	REDEF CURRENT_GRASS_WILDMONS_LABEL = S "._def_grass_wildmons_p1"
{CURRENT_GRASS_WILDMONS_LABEL}:
	map_id p1
}

def end_grass_wildmons() {
	assert GRASS_WILDDATA_LENGTH == @ - {CURRENT_GRASS_WILDMONS_LABEL},"def_grass_wildmons {CURRENT_GRASS_WILDMONS_MAP}: expected {d:GRASS_WILDDATA_LENGTH} bytes"
}

;\1: map id
def def_water_wildmons() {
	REDEF CURRENT_WATER_WILDMONS_MAP = S "p1"
	REDEF CURRENT_WATER_WILDMONS_LABEL = S "._def_water_wildmons_p1"
{CURRENT_WATER_WILDMONS_LABEL}:
	map_id p1
}

def end_water_wildmons() {
	assert WATER_WILDDATA_LENGTH == @ - {CURRENT_WATER_WILDMONS_LABEL},"def_water_wildmons {CURRENT_WATER_WILDMONS_MAP}: expected {d:WATER_WILDDATA_LENGTH} bytes"
}
