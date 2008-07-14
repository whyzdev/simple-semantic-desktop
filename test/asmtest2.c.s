	.section ".text"
	.align 4
	.global function1
function1:
	!#PROLOGUE# 0
	save %sp, -120, %sp
	!#PROLOGUE# 1
	st %i0, [%fp+68]
	st %i1, [%fp+72]
	ret
	restore
	.align 4
	.global function2
function2:
	!#PROLOGUE# 0
	save %sp, -120, %sp
	!#PROLOGUE# 1
	st %i0, [%fp+68]
	st %i1, [%fp+72]
	ret
	restore
	.align 4
	.global function3
function3:
	!#PROLOGUE# 0
	save %sp, -120, %sp
	!#PROLOGUE# 1
	st %i0, [%fp+68]
	st %i1, [%fp+72]
	st %i2, [%fp+76]
	st %i3, [%fp+80]
	st %i4, [%fp+84]
	st %i5, [%fp+88]
	ret
	restore
	.align 4
	.global function4
function4:
	!#PROLOGUE# 0
	save %sp, -120, %sp
	!#PROLOGUE# 1
	st %i0, [%fp+68]
	st %i1, [%fp+72]
	ret
	restore
	.align 4
	.global main
main:
	!#PROLOGUE# 0
	save %sp, -112, %sp
	!#PROLOGUE# 1
	ret
	restore
	.common numbernumber, 4, 4
	.common foofoo, 4, 4
	.common barbar, 4, 4
	.common asterisk, 4, 4
	.common samplearray, 80, 4
