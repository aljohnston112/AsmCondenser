; Some functions load the predef id
; without immediately calling Predef.
def lda_predef(p1) {
	a = (p1Predef - PredefPointers) / 3
}

