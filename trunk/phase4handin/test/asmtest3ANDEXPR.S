	
; int main(void){
;   int a;
;   int b;
;   int c;
;   int d;
  
;   a=1;
;   b=3;
;   c=2;
;   d=a&&b&&c;
;   return d;
; }
  

	.section	".text"
	.align	4
	.global	main
main0:
	!#PROLOGUE#	0
	save	%sp, -128, %sp
	!#PROLOGUE#	1

	;;begin a=1
	add	%fp, -16, %l0
	mov	1, %l4
	mov	%l4, %l5
	st	%l5, [%l0]

	;;begin b=3
	add	%fp, -12, %l0
	mov	3, %i0
	mov	%i0, %i1
	st	%i1, [%l0]

	;;begin c=2
	add	%fp, -8, %l0
	mov	2, %i3
	mov	%i3, %i4
	st	%i4, [%l0]


	;; begin add-expr	
	add	%fp, -4, %l0	; *%l0==d
	ld	[%fp-16], %g0	; load a
	ld	[%fp-12], %g2	; load b
	cmp	%g2, 0		; if b is zero,
	be	LL1:		; branch to end
	nop			
	ld	[%fp-8], %g3	; load c
	cmp	%g3, 0		; if c is zero
	be	LL1:		; branch to end
	nop
	mov	1, %g0		; set a to zero
LL1::
	;; end add-expr
	
	mov	%g0, %g3	; 
	st	%g3, [%l0]	; store result in d
	ret
	restore
