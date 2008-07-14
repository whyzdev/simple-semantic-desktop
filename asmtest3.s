	.file	"asmtest3.c"
	.section	".text"
	.align 4
	.global main
	.type	main, #function
	.proc	04
main:
	!#PROLOGUE# 0
	save	%sp, -120, %sp
	!#PROLOGUE# 1
	sethi	%hi(a), %g1
	or	%g1, %lo(a), %o5
	mov	19, %g1
	st	%g1, [%o5]
	sethi	%hi(b), %g1
	or	%g1, %lo(b), %o5
	mov	3, %g1
	st	%g1, [%o5+8]
	sethi	%hi(a), %g1
	or	%g1, %lo(a), %g1
	ld	[%g1], %g1
	st	%g1, [%fp-20]
	sethi	%hi(b), %g1
	or	%g1, %lo(b), %g1
	ld	[%g1+8], %g1
	st	%g1, [%fp-24]
	mov	1, %o0
	mov	2, %o1
	call	f, 0
	 nop
	mov	%o0, %o5
	sethi	%hi(a), %g1
	or	%g1, %lo(a), %g1
	st	%o5, [%g1]
	sethi	%hi(a), %g1
	or	%g1, %lo(a), %g1
	ld	[%g1], %g1
	mov	%g1, %i0
	ret
	restore
	.size	main, .-main
	.align 4
	.global f
	.type	f, #function
	.proc	04
f:
	!#PROLOGUE# 0
	save	%sp, -112, %sp
	!#PROLOGUE# 1
	st	%i0, [%fp+68]
	st	%i1, [%fp+72]
	mov	4, %g1
	mov	%g1, %i0
	ret
	restore
	.size	f, .-f
	.common	a,4,4
	.common	b,80,4
	.ident	"GCC: (GNU) 3.3.6"
