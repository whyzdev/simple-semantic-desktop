	.file	"asmtest3.c"
	.section	".text"
	.align 4
	.global main
	.type	main, #function
	.proc	04
main:
	!#PROLOGUE# 0
	save	%sp, -208, %sp
	!#PROLOGUE# 1
	mov	19, %g1
	st	%g1, [%fp-104]
	ld	[%fp-104], %g1
	mov	%g1, %i0
	ret
	restore
	.size	main, .-main
	.ident	"GCC: (GNU) 3.3.6"
