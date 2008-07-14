import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)
import java.awt.*;   // for use of Point
import java.util.*;  // for use of ArrayList

/**
 * Write a description of class ai_Agent here.
 * 
 * @author Esmail Bonakdarian
 * @version April 1, 2007
 */
public class ai_Agent extends Actor
{    

    private static final int EAST  = 0;
    private static final int WEST  = 1;
    private static final int NORTH = 2;
    private static final int SOUTH = 3;

    /*
    private static final int NORTH_EAST = 4;
    private static final int NORTH_WEST = 5;
    private static final int SOUTH_EAST = 6;
    private static final int SOUTH_WEST = 7;
    */

    // number of directions - update if change
    private static final int DIRECTIONS = 4; 
    
    private static final int NONE = -1;


    private GreenfootImage greenfootImage;
    private World myWorld;
    private int   x_width;
    private int   y_width;

    private int   move_count;



    /**
     * 
     */
    public void addedToWorld(World world) // this is the defacto contructor
    {
        myWorld = getWorld();
	greenfootImage = getImage();

        x_width = myWorld.getWidth();
        y_width = myWorld.getHeight();

	randomDirection();

	// either run this sequence here, or
	// comment this below and uncomment the
	// code in act()
	/*
	Point start = new Point();
	Point goal = new Point();

	ArrayList path = A_Star(start, goal);

	ProcessPath(path);
	*/
    }   




    /**
     * Act - do whatever the ai_Agent wants to do. This method is
     * called whenever the 'Act' or 'Run' button gets pressed in the
     * environment.
     */
    public void act() 
    {

        // Add your action code here.



	if (foundWall() == NONE)
	    move();
	else
	    randomDirection();

	// System.out.println("move #: " + move_count);
    }




    /**
     * identify the author/creator of the agent
     *
     * @return a string that contains the name of the agent author
     */
    public String id()
    {
	return "Esmail Bonakdarian";
    }




    /**
     * detects a wall if present, and its direction
     * 
     * @return the direction where a wall was found. If
     *    none was found, NONE is returned. Direction is defined
     *    the constants EAST, WEST, NORTH, SOUTH
     */
    public int foundWall()
    {
	int result    = NONE;
	int direction = getDirection();

	int x_offset  = 0;
	int y_offset  = 0;

	switch(direction)
	    {
	    case EAST: x_offset++;
		break;

	    case WEST: x_offset--;
		break;

	    case NORTH: y_offset--;
		break;

	    case SOUTH: y_offset++;
		break;
	    }
	
	Actor wall = getOneObjectAtOffset(x_offset, y_offset, Wall.class);

	if (wall != null)
	    result = direction;
	
	return result;
    }



    /**
     * only allow 4 cardinal directions as defined by
     * constants EAST, WEST, NORTH, SOUTH
     *
     * @return direction as defined by constants EAST, WEST, NORTH, SOUTH
     */

    public int getDirection()
    {
	int direction = NORTH;

        int rot = getRotation();

	if (rot == 0) // east
	    direction = EAST;
	else if (rot == 180) // west
	    direction = WEST;
	else if (rot == 90)  // south
	    direction = SOUTH;
	
	return direction;
    }




    /**
     * set the agents direction
     *
     * @param direction as defined by constants EAST, WEST, NORTH, SOUTH
     */
    public void setDirection(int direction)
    {
	int rot = 270;

	if (direction == EAST)
	    rot = 0;
	else if (direction == WEST)
	    rot = 180;
	else if (direction == SOUTH)
	    rot = 90;

	setRotation(rot);
    }



    /**
     * change direction randomly
     */    
    public void randomDirection()
    {
	// one of the directions
	int direction = Greenfoot.getRandomNumber(DIRECTIONS); 
	setDirection(direction);
    }




    /** 
     * move the agent
     */
    public void move()
    {
        int x = getX();
        int y = getY();

        int direction = getDirection();
	
	if (direction == EAST) 
	    {
		x = (x+1) % x_width;
	    }
	else if (direction == WEST) 
	    {
		x--;

		if (x < 0)
		    x = x_width;
	    }
	else if (direction == SOUTH) 
	    {
		y = (y+1) % y_width;
	    }
	else if (direction == NORTH)
	    {
		y--;

		if (y < 0)
		    y = y_width;
	    }

	move_count++;
	setLocation(x, y);
    } 




    /**
     * make the agent turn left
     */
    public void turnLeft()
    {
	int rot = getRotation();
	setRotation ( (rot+90) % 361 );
    }


    /**
     * reads the directions and has the agent move accordingly
     *
     * @param direction for the path from start to goal
     */
    public void ProcessPath(ArrayList path)
    {
	int path_len = path.size();
	
	for(int i = 0; i < path_len; i++)
	    {
		int dir = (Integer) path.get(i);
		setDirection(dir);

		int x = getX();
		int y = getY();
		BreadCrumb bc = new BreadCrumb();
		myWorld.addObject(bc, x, y);

		move();
	    }
	
    }



    // ---------- SEARCH CODE HERE -----------------

    /**
     * A* search
     *
     * @param start - contains x/y for starting position
     * @param goal  - contains x/y for goal position
     *
     * @return an array list which contains the directions from
     *         start to goal.
     */
    public ArrayList A_Star(Point start, Point goal)
    {
	ArrayList path = new ArrayList();

	path.add(EAST);
	path.add(EAST);
	path.add(EAST);
	path.add(EAST);
	path.add(NORTH);
	path.add(NORTH);
	path.add(NORTH);
	path.add(WEST);
	path.add(SOUTH);


	path.trimToSize();

	return path;
    }




    // **** ADD ONE MORE SEARCH HERE

    /**
     * FS (replace with BFS or DFS)
     *
     * @param start - contains x/y for starting position
     * @param goal  - contains x/y for goal position
     *
     * @return an array list which contains the directions from
     *         start to goal.
     */
    public ArrayList FS(Point start, Point goal)
    {
	ArrayList path = new ArrayList();

	return path;
    }
}
