/* 
   ------------------------------------------------------------------
   '########:'##:::::::'####:'########::::'###::::
   ##.....:: ##:::::::. ##::..... ##::::'## ##:::
   ##::::::: ##:::::::: ##:::::: ##::::'##:. ##::
   ######::: ##:::::::: ##::::: ##::::'##:::. ##:
   ##...:::: ##:::::::: ##:::: ##::::: #########:
   ##::::::: ##:::::::: ##::: ##:::::: ##.... ##:
   ########: ########:'####: ########: ##:::: ##:
   ........::........::....::........::..:::::..::

   eliza.java - a simplified version of Joseph Weizenbaum's Eliza

   -written by Akshat Singhal 
   at Oberlin College, in the Spring of  2007, for CSCI 364 
   ------------------------------------------------------------------
*/

import java.util.*;

public class eliza{

    /*-----------------*/
    /* main() */
    /*-----------------*/

    public static void main(String args[]){
	/* Initialization */
	boolean endloop=false;
	Scanner darkly  = new Scanner(System.in);
	Scanner currentline;
	/* -end init- */

	/* Print welcome message */
	System.out.println("The doctor is in.");
	System.out.println("What's on your mind?");
	/* -end printing- */

	/* Run a loop for I/O */ 
	while (!endloop){
	    System.out.print(" - ");
	    currentline=new Scanner(darkly.nextLine().toLowerCase());
	    if (currentline.findInLine("bye")==null)
		System.out.println(respond(currentline));//print a response
	    else {//Exit program if 'bye' was typed
		System.out.println("Alright then, goodbye!"+
				   " \n**[Akshat Singhal's mini-Eliza].**");
		endloop=true;
	    }
	}/* -end I/O loop- */
    }/* -end main() - */


    /*-----------------*/
    /* respond() - returns a response that Eliza would return, given an input
       string from the user */
    /*-----------------*/

    private static String respond(Scanner s){

	/* Init a HashMap of keyword-response pairs */
	HashMap<String,String[]> responses = new HashMap<String,String[]>();
	String[] temp0={"What does that suggest to you?",
			"I see.",
			"I'm not sure I understand you fully.",
			"Can you elaborate?",
			"That is quite interesting."			
	};
	responses.put("NOTFOUND", temp0);

	String[] temp1={"Can you think of a specific example?"};    
	responses.put("always", temp1);

	String[] temp2={"Is that the real reason?"};
	responses.put("because", temp2);

	String[] temp3={"Please don't apologize."};
	responses.put("sorry", temp3);

	String[] temp4={"You don't seem very certain."};
	responses.put("maybe", temp4);

	String[] temp5={"Do you really think so?"};
	responses.put("i think", temp5);

	String[] temp6={"We were discussing you, not me."};
	responses.put("you", temp6);

	String[] temp7={"Why do you think so?",
			"You seem quite positive."};
	responses.put("yes", temp7);

	String[] temp8={"Why not?",
			"Are you sure?"};
	responses.put("no", temp8);

	String[] temp9={"I am sorry to hear you are *.",
			"How long have you been *?",
			"Do you believe it is normal to be *?",
			"Do you enjoy being *?"
	};
	responses.put("i am", temp9);
	responses.put("i'm", temp9);

	String[] temp10={"Tell me more about such feelings.",
			 "Do you often feel *?",
			 "Do you enjoy feeling *?",
			 "Why do you feel that way?"
	};
	responses.put("i feel", temp10);

	String[] temp11={"Tell me more about your family.",
			 "How do you get along with your family?",
			 "Is your family important to you?"
	};
	responses.put("family", temp11);
	responses.put("mother", temp11);
	responses.put("father", temp11);
	responses.put("mom", temp11);
	responses.put("dad", temp11);
	responses.put("sister", temp11);
	responses.put("brother", temp11);
	responses.put("husband", temp11);
	responses.put("wife", temp11);

	String[] temp12={"What does that dream suggest to you?",
			 "Do you dream often?",
			 "What persons appear in your dreams?",
			 "Are you disturbed by your dreams?"		 
	};
	responses.put("dream", temp12);
	responses.put("nightmare", temp12);
       	
		      
	String[] keywords={"always","because","sorry","maybe","i think",
			   "you","yes","no","i am","i'm","i feel","family",
			   "mother","mom","dad","father","sister",
			   "brother","husband","wife","dream","nightmare"};
	/* -end hashmap init- */
	
	/* initialize variables */
	String response="";
	String[] response_array={""};
	boolean found=false;
	String currentkeyword="";
	/* - end init - */

	/* Loop through keywords */
	for(int i=0; i<keywords.length;i++){
	    if ((s.findInLine(currentkeyword=(String)keywords[i])!=null) 
		&& (responses.get(currentkeyword)!=null)){
		/*If a keyword is found in the current input, get a response
		  from HashMap and return it*/
		found=true;
		response_array=(String[])responses.get(currentkeyword);
		response=response_array[(int)((responsearray.length-1)*
					     Math.random())];
		/* If response has a *, replace it with the remainder of 
		   input string _with the last character removed if it is
		   a punctuation character_ */
		if (response.indexOf('*')!=-1){
		    String remaining_input;
		    if (s.hasNext() && 
			(remaining_input=s.nextLine().trim())!=null){
			response = response.substring(0,response.indexOf('*'))+
			    remaining_input
			    .substring(0,remaining_input.length()-1)
			    + remaining_input
			    .substring(remaining_input.length()-1,
				       remaining_input.length())
			    .replaceAll("[^A-Za-z]", "") +
			    response.substring(response.indexOf('*')+1, 
					       response.length());
			response=response.trim();
		    }
		    else
			response=response.replaceAll("[*]","");
		}
	    }
	}
	
	/*respond with a default message if no keywords were found in the 
	  input string */
	if (!found){
	    response_array=(String[])responses.get("NOTFOUND");
	    response=response_array[(int)((response_array.length-1)*
					 Math.random())];
	}
	return response;
    }/*- end respond() - */    
}
