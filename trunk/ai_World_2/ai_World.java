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
        
        
        // width, height, cell dimension
        super(70, 50, 15); 
//      super(30, 20, 15);
//      super(7, 5, 15);
        setBackground("small_cell2.jpg");

	int x_width = getWidth();
	int y_width = getHeight();

	for(int x = 0; x < x_width; x++)
	    for(int y = 0; y < y_width; y++)
		if ( (x == 0) || (x == x_width-1) ||
		     (y == 0) || (y == y_width-1))
		    {
			Wall wall = new Wall();
			addObject(wall, x, y);
		    }


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

	// for good measure

	randomWall(888);
    }



    /**
     * place rocks .. determined by previous interactive placement
     * agent and goal
     */    
    public void populatePlus()
    {

	populate();

	randomWall(888);

	// place goal at fixed position
	Goal goal = new Goal();
	addObject(goal, 1, 1);

	// place agent at fixed position
	ai_Agent agent = new ai_Agent();
	addObject(agent, 6, 6);
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
