	.file	"asmtest1.c"
	.section	".text"
	.align 4
	.global function1
	.type	function1, #function
	.proc	020
function1:
	!#PROLOGUE# 0
	save	%sp, -144, %sp
	!#PROLOGUE# 1
	st	%i0, [%fp+68]
	st	%i1, [%fp+72]
	st	%i2, [%fp+76]
	st	%i3, [%fp+80]
	st	%i4, [%fp+84]
	st	%i5, [%fp+88]
	ret
	restore
	.size	function1, .-function1
	.align 4
	.global main
	.type	main, #function
	.proc	020
main:
	!#PROLOGUE# 0
	save	%sp, -128, %sp
	!#PROLOGUE# 1
	mov	7, %g1
	st	%g1, [%sp+92]
	mov	8, %g1
	st	%g1, [%sp+96]
	mov	1, %o0
	mov	2, %o1
	mov	3, %o2
	mov	4, %o3
	mov	5, %o4
	mov	6, %o5
	call	function1, 0
	 nop
	nop
	ret
	restore
	.size	main, .-main
	.common	numbernumber,4,4
	.common	foofoo,4,4
	.common	barbar,4,4
	.common	pointerthing,4,4
	.common	samplearray,80,4
	.ident	"GCC: (GNU) 3.3.6"
