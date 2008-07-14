	.file	"asmtest2.c"
	.section	".text"
	.align 4
	.global function1
	.type	function1, #function
	.proc	020
function1:
	!#PROLOGUE# 0
	save	%sp, -120, %sp
	!#PROLOGUE# 1
	st	%i0, [%fp+68]
	st	%i1, [%fp+72]
	ld	[%fp-20], %g1
	st	%g1, [%fp+68]
	ret
	restore
	.size	function1, .-function1
	.align 4
	.global function2
	.type	function2, #function
	.proc	020
function2:
	!#PROLOGUE# 0
	save	%sp, -120, %sp
	!#PROLOGUE# 1
	st	%i0, [%fp+68]
	st	%i1, [%fp+72]
	ld	[%fp-20], %g1
	st	%g1, [%fp+68]
	ret
	restore
	.size	function2, .-function2
	.align 4
	.global function3
	.type	function3, #function
	.proc	020
function3:
	!#PROLOGUE# 0
	save	%sp, -120, %sp
	!#PROLOGUE# 1
	st	%i0, [%fp+68]
	st	%i1, [%fp+72]
	st	%i2, [%fp+76]
	st	%i3, [%fp+80]
	st	%i4, [%fp+84]
	st	%i5, [%fp+88]
	ld	[%fp+68], %g1
	st	%g1, [%fp-20]
	ld	[%fp+72], %g1
	st	%g1, [%fp-20]
	ld	[%fp+76], %g1
	st	%g1, [%fp-20]
	ld	[%fp+80], %g1
	st	%g1, [%fp-20]
	ld	[%fp+84], %g1
	st	%g1, [%fp-20]
	ld	[%fp+88], %g1
	st	%g1, [%fp-20]
	ld	[%fp+92], %g1
	st	%g1, [%fp-20]
	ld	[%fp+96], %g1
	st	%g1, [%fp-20]
	ret
	restore
	.size	function3, .-function3
	.align 4
	.global function4
	.type	function4, #function
	.proc	020
function4:
	!#PROLOGUE# 0
	save	%sp, -120, %sp
	!#PROLOGUE# 1
	st	%i0, [%fp+68]
	st	%i1, [%fp+72]
	ld	[%fp-20], %o0
	ld	[%fp-24], %o1
	call	function2, 0
	 nop
	ld	[%fp-20], %o0
	ld	[%fp-24], %o1
	call	function1, 0
	 nop
	ld	[%fp-20], %g1
	st	%g1, [%fp+68]
	ret
	restore
	.size	function4, .-function4
	.align 4
	.global main
	.type	main, #function
	.proc	04
main:
	!#PROLOGUE# 0
	save	%sp, -112, %sp
	!#PROLOGUE# 1
	sethi	%hi(foofoo), %g1
	or	%g1, %lo(foofoo), %i5
	mov	1, %g1
	st	%g1, [%i5]
	sethi	%hi(barbar), %g1
	or	%g1, %lo(barbar), %i5
	mov	2, %g1
	st	%g1, [%i5]
	sethi	%hi(numbernumber), %g1
	or	%g1, %lo(numbernumber), %i5
	mov	1, %g1
	st	%g1, [%i5]
	sethi	%hi(pointerthing), %g1
	or	%g1, %lo(pointerthing), %i5
	sethi	%hi(foofoo), %g1
	or	%g1, %lo(foofoo), %g1
	st	%g1, [%i5]
	sethi	%hi(samplearray), %g1
	or	%g1, %lo(samplearray), %i5
	mov	1, %g1
	st	%g1, [%i5+8]
	mov	1, %g1
	mov	%g1, %i0
	ret
	restore
	.size	main, .-main
	.common	numbernumber,4,4
	.common	foofoo,4,4
	.common	barbar,4,4
	.common	pointerthing,4,4
	.common	samplearray,80,4
	.ident	"GCC: (GNU) 3.3.6"
