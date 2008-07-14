import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)
import java.util.*;
import java.io.*;


/**
 * PathFollower is a follower that does A* pathfinding to get 
 * to the road and then stays on it
 * 
 * @author Akshat Singhal
 * @version 0.1 3-April-2007
 */
public class PathFollower extends Actor
{
    private World myWorld;
    private int x_width;
    private int y_width;
    private int[][] H;//H: cost of going from a point (x,y) to the goal, calced by heuristic
    private int[][] G;//G: cost of going from current cell to neighbouring ones
    private LinkedList openList;
    private LinkedList closedList;
    private LinkedList pathList;
    private boolean DIAG1 = true;
    

    public void addedToWorld(World world)
    {
        myWorld = getWorld();           
        x_width = myWorld.getWidth();
        y_width = myWorld.getHeight();
    if (DIAG1)
        System.out.printf("x_width/y_width= %d / %d\n", x_width, y_width);
        
    //initialize vectors for Heuristic and G-distance
        H=new int[x_width][y_width];
        G=new int[x_width][y_width];
    
    //init the open,closed, and path lists
        openList=new LinkedList<int[]>();
        closedList=new LinkedList<int[]>();
        pathList=new LinkedList<int[]>();
        
    //calculate H-values for each grid
        for (int i=0;i<x_width;i++)
            for(int j=0;j<y_width;j++){
                H[i][j]=getHeuristicCost(i,j);
                G[i][j]=0;
            }

        
    }   
        
   
    
    /**
     * Act - moves pathFollower by one grid 
     *
     */
    public void act() 
    {
    
        int[] currentNode = new int[3];

        //set the current tile, add stuff to open list
        currentNode[0]=getX();
        currentNode[1]=getY();    
//        setGValues();
//        currentNode[2]=G[currentNode[0]][currentNode[1]] + H[currentNode[0]][currentNode[1]] ;        
        //---look around and add tiles to open list as appropriate
          setGValues();
           for (int i=getX()-1;i<=getX()+1;i++)
               for (int j=getY()-1;j<=getY()+1;j++) 
                if (canMove(i,j) && isNotInClosedList(i,j)){
                    int[] newNode=new int[3];
                    newNode[0]=i;
                    newNode[1]=j;
                    newNode[2]=G[i][j]+H[i][j];
                    openList.add(newNode);
                }
               

        if(!getWhiteValue(currentNode)){ 
            

        /* find a path to the road if you're not already on it */

        //--get the lowest cost tile from open list        
            int minCost=((int[])openList.get(0))[2], minNodeIndex=0;
            
            for (int i=0;i<openList.size();i++){
        if ((((int[])openList.get(i))[2] < minCost) 
            && (isNotInClosedList(((int[])openList.get(i))[0],
                      ((int[])openList.get(i))[1]))){
            minNodeIndex=i;
            minCost=((int[])openList.get(i))[2];
        }
        }
        
        //move to that best tile
            setLocation(((int[])openList.get(minNodeIndex))[0],
            ((int[])openList.get(minNodeIndex))[1]);
        if (DIAG1)
        System.out.println("Moving to " + 
                   ((int[])openList.get(minNodeIndex))[0] 
                   + "," + ((int[])openList.get(minNodeIndex))[1] );
        //
            currentNode=(int[])openList.get(minNodeIndex);
            //--if you're on a white patch, i.e. on the road
            //--if not on white patch
            //---maintain parent, add current tile to closed list
                            
            pathList.add(currentNode);
            closedList.add(currentNode);
            removeFromOpenList(currentNode);                   
        }
    else{
        //If agent is on the road already, make it stick to the road.


        //look around for tiles , add them to open list
        //move to one of the white tiles in neighbourhood        
    //  idea: Maybe it would help to randomize the order 
    //in which these rules are followed, or to encode directions 
    // for the agent such that moves are made in the same 
    //direction until the agent cannot see any more white patches.
    //Seems like this is a place where some more lookhead would 
    //help the algorithm follow a path better.
          if (myWorld.getColorAt(getX()+1,getY()).equals(java.awt.Color.white) 
          && isNotInClosedList(getX()+1,getY())) //go right?
              setLocationAndSave(getX()+1,getY());
          else if (myWorld.getColorAt(getX(),getY()+1).equals(java.awt.Color.white) 
           && isNotInClosedList(getX(),getY()+1))//go up?
              setLocationAndSave(getX()+1,getY()+1);
          else if (myWorld.getColorAt(getX(),getY()-1).equals(java.awt.Color.white) 
           && isNotInClosedList(getX(),getY()-1))//go down?
              setLocationAndSave(getX(),getY()-1);            
          else if (myWorld.getColorAt(getX()-1,getY()).equals(java.awt.Color.white) 
           && isNotInClosedList(getX()-1,getY()))//go left?
              setLocationAndSave(getX()-1,getY());
          else if (myWorld.getColorAt(getX()+1,getY()+1).equals(java.awt.Color.white) 
           && isNotInClosedList(getX()+1,getY()+1))//go right-up?
              setLocationAndSave(getX()+1,getY()+1);
          else if (myWorld.getColorAt(getX()-1,getY()-1).equals(java.awt.Color.white) 
           && isNotInClosedList(getX()-1,getY()-1))//go left-down?
              setLocationAndSave(getX()-1,getY()-1);                        
          else if (myWorld.getColorAt(getX()-1,getY()+1).equals(java.awt.Color.white) 
           && isNotInClosedList(getX()-1,getY()+1))//go left-up?
              setLocationAndSave(getX()-1,getY()+1);                          
          else //go right-down if nothing else
              setLocationAndSave(getX()+1,getY()-1);         


                
    }
           
    }
            

    /**
     * determines if a legal move is possible
     * @return true or false if possible to move
     */
    private boolean canMove(int x, int y)
    {

                
        if ( (x+1 >= x_width) || (x < 0) ||
             (y+1 >= y_width) || (y < 0) )
           return false;

        return true;
    }

    
    //this method calculates the heuristic value
    private int getHeuristicCost(int x, int y){
       int minYDistance=y_width+1, Hcost=y_width+1;


       //find distance to nearest road above this cell
       for (int j=y;j<y_width;j++){
           int[] nodeInTest=new int[3];
           nodeInTest[0]=x;
           nodeInTest[1]=j;
           nodeInTest[2]=0;
           if (getWhiteValue(nodeInTest) && minYDistance>j){
             minYDistance=j;
             break;
            }
        }



       //find distance to nearest road below this cell        
       for (int j=y;j>=0;j--){
           int[] nodeInTest=new int[3];
           nodeInTest[0]=x;
           nodeInTest[1]=j;
           nodeInTest[2]=0;
           if (getWhiteValue(nodeInTest) && minYDistance>j){
           minYDistance=j;
           break;
       }
       }
        
       
       
       Hcost=minYDistance;
       
       return Hcost;
    }
    

    //return true if the current cell is white
    private boolean getWhiteValue(int[] node){
        return (myWorld.getColorAt(node[0],node[1]).equals(java.awt.Color.white));
    }

    //calculate the G-distance to neighbouring nodes
    private void setGValues(){
            for (int i=getX()-1;i<=getX()+1;i++)
              for (int j=getY()-1;j<=getY()+1;j++) 
                if (canMove(i,j)){
                      if  (Math.random()<0.05)
                   if (j==getY()||i==getX())
                    G[i][j]=10;
                   else
                    G[i][j]=14;
                }        
    }

    // return true if the cell at passed coordinates is in the closed list
    private boolean isNotInClosedList(int x, int y){
        for (int i=0; i<closedList.size();i++){
            int[] node_i=(int[])closedList.get(i);
            if (node_i[0]==x && node_i[1]==y)
                return false;                       
        }
        if (canMove(x,y))
            return true;           
        else
            return false;
    }
    
    //sets the current location to a given coordinate, and puts that 
    //location in the closed list.
    private void setLocationAndSave(int x, int y){
        int[] newNode= new int[3];
        newNode[0]=x;
        newNode[1]=y;
        newNode[2]=0;
        closedList.add(newNode);        
        setLocation(x,y);
    }
    
    private void removeFromOpenList (int[] node){
        for (int i = 0;i<openList.size();i++){
            if ((((int[])openList.get(i))[0] == node[0]) &&  
            (((int[])openList.get(i))[1] == node[1]) ){
                openList.remove(i);
                return;
            }
            
        }
    }
    
}

