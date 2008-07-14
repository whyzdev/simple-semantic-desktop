	.section	".text"
	.align	4
	.global	main_void
main_void:
	!#PROLOGUE#	0
	save	%sp, -120, %sp
	!#PROLOGUE#	1
	sethi	%hi(a), %l0
	or	%l0, %lo(a), %l1
	mov	19, %l0
	st	%l0, [%l1]
	mov	2, %l0
	smul	%l0, 4, %l0
	sethi	%hi(b), %l1
	or	%l1, %lo(b), %l2
	add	%l2, %l0, %l2
	mov	3, %l0
	st	%l0, [%l2]
	add	%fp, -8, %l0
	sethi	%hi(a), %l1
	or	%l1, %lo(a), %l2
	ld	[%l2], %l2
	st	%l2, [%l0]
	add	%fp, -4, %l0
	mov	2, %l1
	smul	%l1, 4, %l1
	sethi	%hi(b), %l2
	or	%l2, %lo(b), %l2
	add	%l2, %l1, %l2
	ld	[%l2], %l2
	st	%l2, [%l0]
	sethi	%hi(a), %l0
	or	%l0, %lo(a), %l1
	mov	1, %l0
	mov	%l0, %o0
	mov	2, %l0
	mov	%l0, %o1
	call	f_int_int, 0
	nop
	nop
	st	%o0, [%l1]
	sethi	%hi(a), %l0
	or	%l0, %lo(a), %l1
	ld	[%l1], %l1
	mov	%l1, %i0
	ret
	restore
	.align	4
	.global	f_int_int
f_int_int:
	!#PROLOGUE#	0
	save	%sp, -112, %sp
	!#PROLOGUE#	1
	st	%i0, [%fp+68]
	st	%i1, [%fp+72]
	mov	4, %l0
	mov	%l0, %i0
	ret
	restore
	.common	a, 4, 4
	.common	b, 80, 4
