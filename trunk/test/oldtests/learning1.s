
	.section	".text"
	.align 4
	.global main


main:
	!#PROLOGUE# 0
	save	%sp, -112, %sp
	!#PROLOGUE# 1
	mov	1, %o0
	call	function1, 0
	 nop
	nop
	ret
	restore
	.size	main, .-main
	.align 4
	.global function1


function1:
	!#PROLOGUE# 0
	save	%sp, -112, %sp
	!#PROLOGUE# 1
	st	%i0, [%fp+68]
	ret
	restore
	.size	function1, .-function1
	.common	numbernumber,4,4
	.common	foofoo,4,4
	.common	barbar,4,4
	.ident	"GCC: (GNU) 3.3.6"
