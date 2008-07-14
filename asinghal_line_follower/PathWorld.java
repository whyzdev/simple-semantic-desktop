import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)

/**
 * Write a description of class PathWorld here.
 * 
 * @author Akshat Singhal
 * @version 0.1 3-April-2007
 */
public class PathWorld extends World
{

    /**
     * Constructor for objects of class PathWorld.
     * 
     */
    public PathWorld()
    {    
	/*create high-res world */
	/* uncomment these lines and comment the ones below for a 
	   high-res world */
    //    super(60, 40, 10);                
    //  setBackground("images/road_map.png"); 

	/*create low-res world */
    super(30, 20, 20);                
	setBackground("images/lowres_road_map.png");
    }
    
    public void populate()
    {
        PathFollower agent = new PathFollower();
        addObject(agent, 1, 1);
    }
}
