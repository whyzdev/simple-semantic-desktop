	.file	"jdasmtest.c"
	.section	".text"
	.align 4
	.global main
	.type	main, #function
	.proc	04
main:
	!#PROLOGUE# 0
	save	%sp, -200, %sp
	!#PROLOGUE# 1
	st	%g0, [%fp-20]
	sethi	%hi(a), %g1
	or	%g1, %lo(a), %o4
	sethi	%hi(a), %g1
	or	%g1, %lo(a), %o5
	mov	1, %g1
	st	%g1, [%o5+4]
	mov	1, %g1
	st	%g1, [%o4]
.LL2:
	ld	[%fp-20], %g1
	cmp	%g1, 19
	ble	.LL4
	nop
	b	.LL3
	 nop
.LL4:
	mov	19, %o5
	ld	[%fp-20], %g1
	sub	%o5, %g1, %g1
	sll	%g1, 2, %o5
	add	%fp, -16, %g1
	add	%o5, %g1, %o1
	sethi	%hi(a), %g1
	or	%g1, %lo(a), %o3
	ld	[%fp-20], %g1
	sll	%g1, 2, %o2
	sethi	%hi(a), %g1
	or	%g1, %lo(a), %o5
	ld	[%fp-20], %g1
	sll	%g1, 2, %g1
	add	%g1, %o5, %o4
	sethi	%hi(a), %g1
	or	%g1, %lo(a), %o5
	ld	[%fp-20], %g1
	sll	%g1, 2, %g1
	add	%g1, %o5, %g1
	ld	[%o4-4], %o5
	ld	[%g1-8], %g1
	add	%o5, %g1, %g1
	st	%g1, [%o3+%o2]
	st	%g1, [%o1-88]
	ld	[%fp-20], %g1
	add	%g1, 1, %g1
	st	%g1, [%fp-20]
	b	.LL2
	 nop
.LL3:
	ld	[%fp-104], %o0
	call	printi, 0
	 nop
	mov	%g1, %i0
	ret
	restore
	.size	main, .-main
	.common	a,80,4
	.ident	"GCC: (GNU) 3.3.6"
