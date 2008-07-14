	.file	"asmtest3.c"
	.section	".text"
	.align 4
	.global main
	.type	main, #function
	.proc	04
main:
	!#PROLOGUE# 0
	save	%sp, -128, %sp
	!#PROLOGUE# 1
	mov	1, %g1
	st	%g1, [%fp-20]

	
	mov	3, %g1
	st	%g1, [%fp-24]

	
	mov	2, %g1
	st	%g1, [%fp-28]


	
	;; begin add-expr
	st	%g0, [%fp-32]
	ld	[%fp-20], %g1
	cmp	%g1, 0
	be	.LL2
	nop
	ld	[%fp-24], %g1
	cmp	%g1, 0
	be	.LL2
	nop
	ld	[%fp-28], %g1
	cmp	%g1, 0
	be	.LL2
	nop
	mov	1, %g1
	st	%g1, [%fp-32]
.LL2:
	; end add-expr

	;begin return-stmt
	ld	[%fp-32], %g1	
	mov	%g1, %i0
	ret
	restore
	;end return-stmt
	
	.size	main, .-main
	.ident	"GCC: (GNU) 3.3.6"
