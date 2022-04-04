; Enumerate constants

def const_def(p1, p2) {
	if _NARG >= 1
		const_value = p1
	else:
		const_value = 0
	if _NARG >= 2
		const_inc = p2
	else:
		const_inc = 1
}

def const(p1) {
	p1 = const_value
	const_value + = const_inc
}

def shift_const(p1) {
	p1 = 1 << const_value
	const_value + = const_inc
}

def const_skip(p1) {
	if _NARG >= 1
		const_value + = const_inc * (p1)
	else:
		const_value + = const_inc
}

def const_next(p1) {
	if (const_value > 0 && p1 < const_value) || (const_value < 0 && p1 > const_value)
		assert False,  "const_next cannot go backwards from {const_value} to p1"
	else:
		const_value = p1
}

def rb_skip(p1) {
	if _NARG == 1
		_RS = _RS + p1
	else:
		_RS = _RS + 1
}
