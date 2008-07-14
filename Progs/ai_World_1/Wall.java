import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)

/**
 * Write a description of class Wall here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Wall extends Actor
{

    private World myWorld;
    private int   x_width;
    private int   y_width;
    private static int   w_count; // wall count
    
    
    /**
     * Act - do whatever the Wall wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act() 
    {
        // Add your action code here.
    }  
    
    public void addedToWorld(World world)
    {
	/*
        myWorld = getWorld();
        
        x_width = myWorld.getWidth();
        y_width = myWorld.getHeight();

	*/

	/*        
        String type = this.getClass().getName();
        
        System.out.printf("%s wall_%d = new %s();\n", 
                          type, ++w_count, type);

        System.out.printf("addObject(wall_%d, %d, %d);\n\n",
                           w_count, getX(), getY());
	*/
    }
}
