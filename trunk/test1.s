	.file	"test1.c"
	.section	".text"
	.align 4
	.global main
	.type	main, #function
	.proc	04
main:
	!#PROLOGUE# 0
	save	%sp, -136, %sp
	!#PROLOGUE# 1
	st	%g0, [%fp-20]
	st	%g0, [%fp-24]
	st	%g0, [%fp-28]
	st	%g0, [%fp-32]
	mov	2, %g1
	st	%g1, [%fp-36]
	mov	2, %g1
	st	%g1, [%fp-20]
	mov	3, %g1
	st	%g1, [%fp-24]
	ld	[%fp-20], %i5
	ld	[%fp-24], %g1
	add	%i5, %g1, %g1
	st	%g1, [%fp-28]
	mov	2, %g1
	st	%g1, [%fp-20]
	mov	%g1, %i0
	ret
	restore
	.size	main, .-main
	.ident	"GCC: (GNU) 3.3.6"
