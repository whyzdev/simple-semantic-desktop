	.section	".text"
	.align	4
	.global	main
main:
	!#PROLOGUE#	0
	save	%sp, -208, %sp
	!#PROLOGUE#	1
	mov	2, %l0
	smul	%l0, 4, %l0
	add	%fp, %l0, %l1
	add	%l1, -80, %l1
	mov	19, %l0
	st	%l0, [%l1]
	mov	2, %l0
	smul	%l0, 4, %l0
	add	<notfound>, %l0, <notfound>
	ld	[<notfound>], <notfound>
	mov	<notfound>, %i0
	ret
	restore
