all: major nand or fn4
or: classfiles
	java generateIO 0
	read -p 'Sample debug output (Press enter)'
	java brain or_input or_exp_output 0.2 0.9 debug > or_config
	java brain or_input or_config > or_actual_output
	cat or_actual_output
	diff or_exp_output or_actual_output 
	read -p 'OR function successfully trained if diff gave no output'
major: classfiles
	java generateIO 1
	java brain major_input major_exp_output 0.02 0.5  > major_config
	java brain major_input major_config > major_actual_output
	diff major_exp_output major_actual_output 
	read -p 'majority function successfully trained if diff gave no output'
odd: classfiles
	java generateIO 2
	java brain odd_input odd_exp_output 0.02 0.9  > odd_config
	java brain odd_input odd_exp_output 0.02 0.9  > odd_config1
	java brain odd_input odd_exp_output 0.1 0.5  > odd_config2
	java brain odd_input odd_exp_output 0.01 0.44  > odd_config3
	java brain odd_input odd_config > odd_actual_output
	java brain odd_input odd_config1 > odd_actual_output1
	java brain odd_input odd_config2 > odd_actual_output2
	java brain odd_input odd_config3 > odd_actual_output3
	read -p 'odd_actual_output{1-3} created'
fn4: classfiles
	java generateIO 3
	java brain fn4_input fn4_exp_output 0.02 0.5  > fn4_config
	java brain fn4_input fn4_config > fn4_actual_output
	diff fn4_exp_output fn4_actual_output 
	read -p 'FN4 (odd number function) successfully trained if diff gave no output'
nand: classfiles
	java generateIO 4
	java brain nand_input nand_exp_output 0.1 0.9  > nand_config
	java brain nand_input nand_config > nand_actual_output
	diff nand_exp_output nand_actual_output 
	read -p 'NAND function successfully trained if diff gave no output'
classfiles:
	javac *.java
clean:
	rm *.class *~
clean_data:
	rm *_*
