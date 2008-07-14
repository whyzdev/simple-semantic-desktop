import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)
import java.awt.*;   // for use of Point
import java.util.*;  // for use of ArrayList

/**
 * Write a description of class ai_Agent here.
 * 
 * @author Akshat Singhal  
 * @version April 13, 2007
 */
public class ai_Agent extends Actor
{   
    private static final int NORTH_EAST = 7;
    private static final int NORTH = 6; 
    private static final int NORTH_WEST = 5;    
    private static final int WEST  = 4;
    private static final int SOUTH_WEST = 3;
    private static final int SOUTH = 2;  
    private static final int SOUTH_EAST = 1;   
    private static final int EAST  = 0;
    private static final int NONE = -1;
    
    // number of directions - update if change
    private static final int DIRECTIONS = 8; 
    



    private GreenfootImage greenfootImage;
    private GreenfootImage possiblepath1;
    private GreenfootImage possiblepath2;
    private GreenfootImage definitepath;
    private GreenfootImage closedpath;
    private static final int printingOffset=13;        
    private static final int cellFontSize=9;           
    private final static int straightG = 10;
    private final static int diagonalG = 14;            
    private final static int UNREACHABLE = 500;
    private final static boolean SLOWMODE= false;
    private final static int slowdelay= 100;
    private final static boolean EXTRAINFO= false;
    private boolean UseA_Star=true;
    
    private World myWorld;
    private int   x_width;
    private int   y_width;
    int[][] h ;
    int[][] g ;
    Point[][] parent;
    private int   move_count;
    private ArrayList currentpath;
    private ArrayList visitedNodes;

    /**
     * addedToWorld() - the constructor for ai_Agent
     */
    public void addedToWorld(World world) // this is the defacto contructor
    {
    myWorld = getWorld();
    greenfootImage = getImage();
    initializeBCImages();
        
    x_width = myWorld.getWidth();
    y_width = myWorld.getHeight();
        
    g = new int[x_width][y_width];
    h = new int[x_width][y_width];   
    parent = new Point[x_width][y_width];   
    for(int i=0;i<x_width;i++)
        for(int j=0;j<y_width;j++)
        g[i][j]=0;
        visitedNodes = new ArrayList();
                    
    randomDirection();

    // either run this sequence here, or
    // comment this below and uncomment the
    // code in act()
  	ArrayList path = A_StarPathToGoal();
	        System.out.println("path:"+path);       
        System.out.println("path length:"+path.size());       
	paintPath(path);
    	UseA_Star=true;
    }   



    public ArrayList A_StarPathToGoal(){
	Point goal = new Point (0,0) ;
	java.util.List goalobjects = myWorld.getObjects(Goal.class);
	if (goalobjects.size() >0 )
	    goal = new Point (((Goal)goalobjects.get(0)).getX(),((Goal)goalobjects.get(0)).getY());

	Point start = new Point(getX(),getY());
	if (EXTRAINFO)
	    System.out.printf("Making path from Agent(%d,%d) to Goal(%d,%d)", start.x,start.y,goal.x,goal.y);
	return A_Star(start, goal);        
    }


    public ArrayList DFSPathToGoal(){
	Point goal = new Point (0,0) ;
	java.util.List goalobjects = myWorld.getObjects(Goal.class);
	if (goalobjects.size() >0 )
	    goal = new Point (((Goal)goalobjects.get(0)).getX(),((Goal)goalobjects.get(0)).getY());

	Point start = new Point(getX(),getY());
	if (EXTRAINFO)
	    System.out.printf("Making path from Agent(%d,%d) to Goal(%d,%d)", start.x,start.y,goal.x,goal.y);
	return DFS(start, goal);        
    }
    
    
    /**
     * Act - do whatever the ai_Agent wants to do. This method is
     * called whenever the 'Act' or 'Run' button gets pressed in the
     * environment.
     */
    public void act() 
    {
    // Add your action code here.
    if (currentpath.size()>0){
       setDirection((Integer)currentpath.get(0));       
       currentpath.remove(0);
    }      
    if (foundWall() == NONE){
        if (hasBreadCrumbAtOffset(0,0,definitepath))
           removeBreadCrumbsAtOffset(0,0,definitepath);     
        move();
    }
    else
        randomDirection();
        
 
    if (UseA_Star){
        myWorld.removeObjects(myWorld.getObjects(BreadCrumb.class));
        paintPath(A_StarPathToGoal());
    }
    // System.out.println("move #: " + move_count);
    }

    public void toggleA_Star(){
        UseA_Star=!UseA_Star;
    }
    
    public void paintDFS(){
    myWorld.removeObjects(myWorld.getObjects(BreadCrumb.class));
  	ArrayList path = DFSPathToGoal();
	        System.out.println("path:"+path);       
        System.out.println("path length:"+path.size());       
	paintPath(path);       
	UseA_Star=false;
    }


    /**
     * identify the author/creator of the agent
     *
     * @return a string that contains the name of the agent author
     */
    public String id()
    {
    return "Akshat Singhal";
    }




    /**
     * detects a wall if present, and its direction
     * 
     * @return the direction where a wall was found. If
     *  none was found, NONE is returned. Direction is defined
     *  the constants EAST, WEST, NORTH, SOUTH
     */
    public int foundWall()
    {
    int result  = NONE;
    int direction = getDirection();

    int x_offset  = 0;
    int y_offset  = 0;

    x_offset=xOffset(direction);
    y_offset=yOffset(direction);

    Actor wall = getOneObjectAtOffset(x_offset, y_offset, Wall.class);

    if (wall != null)
        result = direction;
    
    return result;
    }



    /**
     * get the agent's current direction
     *
     * @return direction as defined by direction constants
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
    else if (rot == 45)
        direction = SOUTH_EAST;
    else if (rot == 135)
        direction = SOUTH_WEST;     
    else if (rot == -45)
        direction = NORTH_EAST;
    else if (rot == -135)
        direction = NORTH_WEST;     
    return direction;

    }




    /**
     * set the agents direction
     *
     * @param direction as defined by direction constants 
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
    else if (direction == NORTH_EAST)
        rot = -45;
    else if (direction == NORTH_WEST)
        rot = -135;
    else if (direction == SOUTH_EAST)
        rot = 45;       
    else if (direction == SOUTH_WEST)
        rot = 135;  
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

    x += xOffset(direction);
    y += yOffset(direction);
        
    if (x < 0)
        x =0;
    if (x > x_width)
        x = x_width;
    if (y < 0)
        y =0;
    if (y > y_width)
        y = y_width;        
        
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
     * reads the directions and has the agent paint breadcrumbs
     * on that path
     * 
     * @param direction for the path from start to goal
     */
    public void paintPath(ArrayList path)
    {
    int path_len = path.size();
    int initialx = getX();
    int initialy = getY();  
    int initialdir = getDirection();    

    int x=0,y=0,dir=0;
    for(int i = 0; i < path_len; i++)
        {
        dir = (Integer) path.get(i);
        setDirection(dir);

        x = getX();
        y = getY();

        placeBreadCrumb(x,y,definitepath);

        move();
        }
        setLocation(initialx,initialy);
        setDirection(initialdir);
        currentpath=path;
    
    }



                
    // ---------- SEARCH CODE HERE -----------------
    
    /**
     * A* search - A* pathfinding algorithm
     *
     * @param start - contains x/y for starting position
     * @param goal  - contains x/y for goal position
     *
     * @return an array list which contains the directions from
     *       start to goal.
     */
    public ArrayList A_Star(Point start, Point goal)
    {                       
        calculateHeuristic(start,goal);
        ArrayList openlist = new ArrayList();
        ArrayList closedlist = new ArrayList();
        ArrayList pathlist = new ArrayList();   
        ArrayList path = new ArrayList();   
        boolean goalreached=false;
                   
        g[start.x][start.y]=0;

        // add the starting node to the open list
        openlist.add(start);
        parent[start.x][start.y]=start;
        // while the open list is not empty
        while (!closedlist.contains(goal)) {
            if (SLOWMODE)
                try {Thread.currentThread().sleep(slowdelay);} catch (Exception e) {;}
            if (openlist.size()==0){
                System.out.println("Agent is trapped!");
                return path;
            }
            //     current node=node from open list with the lowest cost

            Point currentnode = (Point)openlist.get(findLowestIndex(openlist));
            //         move current node to the closed list
            closedlist.add(currentnode);
            openlist.remove(currentnode);
            
            if (EXTRAINFO)
            System.out.printf("current node: %d,%d\n",currentnode.x,currentnode.y);     
            //     if current node = goal node then    
            //         path complete
            if (currentnode.equals(goal)){
                if (EXTRAINFO)
                    System.out.println("goal found!");
                }
                //     else
            else{      
                //         examine each node adjacent to the current node
                for (int i=0;i<DIRECTIONS;i++) {
                    Point querynode = new Point(currentnode.x+xOffset(i),currentnode.y+yOffset(i));
                    //         for each adjacent node        
                    //           if it isn't on the open list
                    //              and isn't on the closed list
                    //                 and it isn't an obstacle then            
                    if (!openlist.contains(querynode) &&!closedlist.contains(querynode) &&
                        canMove(querynode.x,querynode.y)){
                            //                    move it to open list and calculate cost
                            openlist.add(querynode);
                            g[querynode.x][querynode.y]=10+(i%2)*4 + g[currentnode.x][currentnode.y];   
                            parent[querynode.x][querynode.y]=currentnode;           
                            if (EXTRAINFO)
                            System.out.printf("added node: %d,%d\n",querynode.x,querynode.y);
                        }//end if
                    else if (openlist.contains(querynode)){
                        if (g[querynode.x][querynode.y] > 10+(i%2)*4 + g[currentnode.x][currentnode.y]){
                            parent[querynode.x][querynode.y]=currentnode;           
                            g[querynode.x][querynode.y]=10+(i%2)*4 + g[currentnode.x][currentnode.y];
                        }//end if                      
                    }//end else if                    
                    placeGBreadCrumb(querynode.x,querynode.y);
                }//end for loop
                
            }//end else
                        if (EXTRAINFO){
            System.out.println("loopcondition: " +((!closedlist.contains(goal)) && openlist.size()>0));
            System.out.println("openlist:"+openlist);
            System.out.println("closedlist:"+closedlist);
        }
        }//end while
        
        Point currentnode = goal;
        while (currentnode != start){
            path.add(calculateDirection(parent[currentnode.x][currentnode.y].x,
                parent[currentnode.x][currentnode.y].y,currentnode.x,currentnode.y));
            currentnode=parent[currentnode.x][currentnode.y];
        }//end while
       
        ArrayList path2 = new ArrayList();
        for (int i=0;i<path.size();i++)
            path2.add(path.get(path.size()-1-i));

        return path2;
    }
    

    /**
     * findLowestIndex() - finds the index of the element in int array
     * with the lowest value.
     */
    private int findLowestIndex(ArrayList list){
    int minimumvalue=0;
    int minimumvalueindex=0;
    if (list.size() >0)
        minimumvalue = g[((Point)list.get(0)).x][((Point)list.get(0)).y] + h[((Point)list.get(0)).x][((Point)list.get(0)).y];
    else 
        return -1;
    for (int i=0;i<list.size();i++){
        int f=g[((Point)list.get(i)).x][((Point)list.get(i)).y]+h[((Point)list.get(i)).x][((Point)list.get(i)).y];
        if (f < minimumvalue){
            minimumvalue=f;
            minimumvalueindex=i;
        }
    }
    return minimumvalueindex;
    }
    

    // **** ADD ONE MORE SEARCH HERE

    /**
     * DFS 
     *
     * @param start - contains x/y for starting position
     * @param goal  - contains x/y for goal position
     *
     * @return an array list which contains the directions from
     *       start to goal.
     */
    public ArrayList DFS(Point start, Point goal)
    {
        ArrayList path = new ArrayList();
        visitedNodes.clear();
        path=DFS_helper(start,goal,path);           
        if (path != null)
        return path;
            else
            return new ArrayList();
    }
  
    private ArrayList DFS_helper(Point current, Point goal, ArrayList path){
        if (SLOWMODE)
            try {Thread.currentThread().sleep(slowdelay);} catch (Exception e) {;}
        ArrayList childpath;                
        //     process(v)
        if (current.x==goal.x && current.y==goal.y)
            return path;
            //     mark v as visited                
        placeBreadCrumb(current.x,current.y,possiblepath2);        
        visitedNodes.add(current);
//     for all vertices i adjacent to v not visited
        for (int i=0;i<DIRECTIONS;i++){
            //         dfs(i)            
            Point newNode=new Point(current.x+xOffset(i),current.y+yOffset(i));
            if ((!visitedNodes.contains(newNode))&& (canMove(newNode.x,newNode.y))){
                path.add(i);
                visitedNodes.add(newNode);
                if ((childpath=DFS_helper(new Point(current.x+xOffset(i),current.y+yOffset(i)),goal,path))!=null){
                    return childpath;
                }
                placeBreadCrumb(newNode.x,newNode.y,closedpath);        
                path.remove(path.size()-1);
            }
        }
        placeBreadCrumb(current.x,current.y,closedpath);        
        return null;
    }
 

    /**
     * seeBreadCrumbs()
     *
     *
     * @return the direction of the neighbouring breadcrumb
     */
    private int seeBreadCrumbs(){
    int x=getX();
    int y=getY();
        
    for (int i=-1;i<=1;i++)
        for (int j=-1;j<=1;j++){
        System.out.printf("looking for breadcrumbs on x:%d, y:%d\n", x+i, y+j);
        if (hasBreadCrumbAtOffset(i,j)){
            //System.out.printf("found crumb on x:%d, y:%d, setting direction to %d\n", x+i, y+j,calculateDirection(x,y,x+i,y+j));                    
            return calculateDirection(x,y,x+i,y+j);
        }
        }
    return NONE;
    }


    private static int calculateDirection(int sourcex,int sourcey,int destinationx,int destinationy){
    int vert = 0;
    int horz = 0;
    int direction=NONE;
    if (destinationx == sourcex){
        if (destinationy < sourcey)
        direction=NORTH;
        else if (destinationy > sourcey)
        direction=SOUTH;
        else 
        direction=NONE;
    }
    else if (destinationx > sourcex){
        if (destinationy < sourcey)
        direction=NORTH_WEST;
        else if (destinationy > sourcey)
        direction=SOUTH_WEST;
        else
        direction=WEST;         
    }
    else{ // if(destinationx < sourcex)
        if (destinationy < sourcey)
        direction=NORTH_EAST;
        else if (destinationy > sourcey)
        direction=SOUTH_EAST;
        else
        direction=EAST;
    }
    return direction;
    }
    
    
    /**
     *  xOffset() - returns the x-offset to move to, given a direction
     *  @param - direction is the direction in terms of the direction
     *  constants
     */
    private int xOffset(int direction){
    if (direction == NORTH_EAST || direction == SOUTH_EAST 
        || direction == EAST)
        return -1;
    if (direction == NORTH_WEST || direction == SOUTH_WEST 
        || direction == WEST)
        return 1;
    if (direction == NORTH || direction == SOUTH)
        return 0;
    return 0;
    }
    /**
     *  yOffset() - returns the y-offset to move to, given a direction
     *  @param - direction is the direction in terms of the direction
     *  constants
     */
    private int yOffset(int direction){
    if (direction == NORTH_EAST || direction == NORTH_WEST 
        || direction == NORTH)
        return -1;
    if (direction == SOUTH_WEST || direction == SOUTH_EAST 
        || direction == SOUTH)
        return 1;
    if (direction == EAST || direction == WEST)
        return 0;
    return 0;
    }
    
    private void initializeBCImages(){      
    int[] xs = {0,0,myWorld.getCellSize()-1,myWorld.getCellSize()-1};
    int[] ys = {0,myWorld.getCellSize()-1,myWorld.getCellSize()-1,0};
    possiblepath1=new GreenfootImage(myWorld.getCellSize(),myWorld.getCellSize());
    possiblepath1.setColor(Color.BLUE);     
    possiblepath1.drawPolygon(xs,ys,4);
    possiblepath2=new GreenfootImage(myWorld.getCellSize(),myWorld.getCellSize());
    possiblepath2.setColor(Color.RED);
    possiblepath2.drawPolygon(xs,ys,4);
    BreadCrumb bc = new BreadCrumb();
    definitepath=bc.getImage();
    int[] cross_xs = {0,0,myWorld.getCellSize()-1,myWorld.getCellSize()-1};
    int[] cross_ys = {0,myWorld.getCellSize()-1,0,myWorld.getCellSize()-1};
    closedpath=new GreenfootImage(myWorld.getCellSize(),myWorld.getCellSize());     
    closedpath.setColor(java.awt.Color.WHITE);
    closedpath.drawPolygon(cross_xs,cross_ys,4);
    }
    
    private void placeBreadCrumb(int x, int y, GreenfootImage img){
    BreadCrumb bc = new BreadCrumb();
    bc.setImage(img);
    myWorld.addObject(bc, x, y);
    }
    
    private boolean hasBreadCrumbAtOffset(int x_offset, int y_offset, GreenfootImage img){
    java.util.List breadcrumbs = myWorld.getObjectsAt(getX()+x_offset,getY()+y_offset, BreadCrumb.class);
    for (int i=0;i<breadcrumbs.size();i++){
        if ((((BreadCrumb)breadcrumbs.get(i)).getImage())==img)
        return true;
    }
    return false;
    }
    
    private boolean hasBreadCrumbAtOffset(int x_offset, int y_offset){
    return getOneObjectAtOffset(x_offset, y_offset, BreadCrumb.class)!=null;
    }
    
    private void removeBreadCrumbsAtOffset(int x_offset, int y_offset){
    while (hasBreadCrumbAtOffset(x_offset,y_offset))
        myWorld.removeObject(getOneObjectAtOffset(x_offset, y_offset, BreadCrumb.class));       
    }
    
    private void removeBreadCrumbsAtOffset(int x_offset, int y_offset, GreenfootImage img){
    java.util.List breadcrumbs = myWorld.getObjectsAt(getX()+x_offset,getY()+y_offset, BreadCrumb.class);
    for (int i=0;i<breadcrumbs.size();i++){
        if (((BreadCrumb)breadcrumbs.get(i)).getImage().equals(img))
        myWorld.removeObject((BreadCrumb)breadcrumbs.get(i));
    }
    }
    
    private boolean hasBreadCrumbAt(int x, int y, GreenfootImage img){
    java.util.List breadcrumbs = myWorld.getObjectsAt(x,y, BreadCrumb.class);
    for (int i=0;i<breadcrumbs.size();i++){
        if (((BreadCrumb)breadcrumbs.get(i)).getImage().equals(img))
        return true;
    }
    return false;
    }
    
    private boolean hasBreadCrumb(int x, int y){
    java.util.List breadcrumbs = myWorld.getObjectsAt(x,y, BreadCrumb.class);
    return breadcrumbs.size()>0;
    }
    
    private void removeBreadCrumbsAt(int x, int y){
    java.util.List breadcrumbs = myWorld.getObjectsAt(x,y, BreadCrumb.class);
    for (int i=0;i<breadcrumbs.size();i++){
        myWorld.removeObject((BreadCrumb)breadcrumbs.get(i));
    }
    }
    
    
     /**
     * 
     * @param x - x coordinate of current cell
     * @param y - y coordinate of current cell
     * @param img - The Image on breadcrumb we want to remove
     * 
     */    
    private void removeBreadCrumbsAt(int x, int y, GreenfootImage img){
    java.util.List breadcrumbs = myWorld.getObjectsAt(x,y, BreadCrumb.class);
    for (int i=0;i<breadcrumbs.size();i++){
        if (((BreadCrumb)breadcrumbs.get(i)).getImage().equals(img))
        myWorld.removeObject((BreadCrumb)breadcrumbs.get(i));
    }
    }
    
    /**
     * calculateHeuristic() calculates heuristic values for A* search
     * as the manhattan distance to the goal.
     *
     * @param start - contains x/y for starting position
     * @param goal  - contains x/y for goal position
     *
     */
    private void calculateHeuristic(Point start, Point goal){

    for (int i=0;i<x_width;i++)
        for (int j=0;j<y_width;j++){
        h[i][j]=(int)((double)Math.pow((double)Math.pow(goal.x-i,2) + (double)Math.pow(goal.y-j,2),(double)1/2)*(double)5);
        //              System.out.printf("H(%d,%d)=%d,x-distance^2:%d,y-distance^2:%d\n",i,j,h[i][j],(int)((double)Math.pow(goal.x-i,2)*8),(int)((double)Math.pow(goal.y-j,2)*8));
        if (EXTRAINFO) {
            GreenfootImage h_image = new GreenfootImage(myWorld.getCellSize(),myWorld.getCellSize());
            h_image.setFont(new Font("Monospaced",Font.PLAIN,cellFontSize));
            h_image.setColor(Color.YELLOW);
            h_image.drawString(Integer.toString(h[i][j]),myWorld.getCellSize()-printingOffset,myWorld.getCellSize()-printingOffset);
            placeBreadCrumb(i,j,h_image);
        }
        }
    }




     /**
     * 
     * canMove - determines if cell (x,y) is reachable
     * i.e. is within World bounds and not a Wall
     * 
     * @param x - x coordinate of current cell
     * @param y - y coordinate of current cell
     *
     */
    private boolean canMove(int x, int y){
    if (! (y>=0 && y <y_width && x >=0 && x <x_width))
        return false;
    java.util.List wall = myWorld.getObjectsAt(x, y, Wall.class);           
    if (wall.size() != 0)
        return false;
    else 
        return true;            
    }

    
     /**
     * Writes the cost of going to each cell from the current one
     * on all immediately reachable cells.
     * @param x - x coordinate of current cell
     * @param y - y coordinate of current cell
     *
     */
    private void placeGBreadCrumb(int x, int y) {
    if (EXTRAINFO && canMove(x,y)){
        GreenfootImage g_image = new GreenfootImage(myWorld.getCellSize(),myWorld.getCellSize());
        g_image.setFont(new Font("Monospaced",Font.PLAIN,cellFontSize));
        g_image.setColor(Color.WHITE);
        g_image.drawString(Integer.toString(g[x][y]),2,myWorld.getCellSize()-printingOffset);                             
        placeBreadCrumb(x,y,g_image);
        placeBreadCrumb(x,y,possiblepath1);                 
    }
    }

}

