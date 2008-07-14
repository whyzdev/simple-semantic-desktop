import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)

/**
 * Write a description of class ai_World here.
 * 
 * @author Esmail Bonakdarian
 * @version April 2007
 */
public class ai_World extends World
{

    /**
     * Constructor for objects of class ai_World.
     * 
     */
    public ai_World()
    {    
        super(12, 10, 60);        
        setBackground("cell2.jpg");

	// comment this next statement out if you 
	// want to have some automatic initialization 
	// at startup
	//
	//	populate();

    }


    /**
     * place rocks .. determined by previous interactive placement
     */    
    public void populate()
    {
        
    	Wall wall_1 = new Wall();
    	addObject(wall_1, 2, 1);
    
    	Wall wall_2 = new Wall();
    	addObject(wall_2, 2, 2);
    
    	Wall wall_3 = new Wall();
    	addObject(wall_3, 2, 3);
    
    	Wall wall_4 = new Wall();
    	addObject(wall_4, 3, 3);
    
    	Wall wall_5 = new Wall();
    	addObject(wall_5, 4, 3);
    
    	Wall wall_6 = new Wall();
    	addObject(wall_6, 4, 4);
    
    	Wall wall_7 = new Wall();
    	addObject(wall_7, 4, 5);
    
    	Wall wall_8 = new Wall();
    	addObject(wall_8, 8, 5);
    
    	Wall wall_9 = new Wall();
    	addObject(wall_9, 8, 6);
    
    	Wall wall_10 = new Wall();
    	addObject(wall_10, 8, 7);
    
    	Wall wall_11 = new Wall();
    	addObject(wall_11, 9, 7);
    
    	Wall wall_12 = new Wall();
    	addObject(wall_12, 9, 8);
    
    	Wall wall_13 = new Wall();
    	addObject(wall_13, 9, 9);
    
    	Wall wall_14 = new Wall();
    	addObject(wall_14, 3, 9);
    
    	Wall wall_15 = new Wall();
    	addObject(wall_15, 3, 8);
    
    	Wall wall_16 = new Wall();
    	addObject(wall_16, 2, 8);


	/*
	// uncomment this block if you want obstacles
	// and the goal placed.


	// place 5 rocks randomly
	randomWall(5);

	// place goal at fixed position
	Goal goal = new Goal();
	addObject(goal, 0, 0);

	// place agent at fixed position
	ebAgent3 agent = new ebAgent3();
	addObject(agent, 6, 6);
	*/
	
	
    }

    /**
     * randomly place some more obstacles
     */
    public void randomWall(int number)
    {
    	for (int i = 0; i < number; i++)
    	    {
		Wall wall = new Wall();
		int x = Greenfoot.getRandomNumber(getWidth());
		int y = Greenfoot.getRandomNumber(getHeight());
		addObject(wall, x, y);
	    }
    }
    
}
