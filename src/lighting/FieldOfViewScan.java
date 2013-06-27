/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lighting;

import objects.GameMap;
import objects.GameObject;
import objects.Tile;

/**
 *
 * @author Travis
 */
public class FieldOfViewScan {  
    static final int START_DEPTH = 1;
    static final int START_STARTSLOPE = 1;
    static final int DEFAULT_ENDSLOPE = 0;
    
    static final int NUMBER_OF_OCTANTS = 8;
    
    GameObject origin;
    int visRange;
    int octant;
    GameMap handlingMap;  
    
//    int numberOfScans;
//    int numberOfVisibleTiles;
    
    static final int[][] INCREMENTS = {
        {1,-1,0, 0,-1,1, 0,0},
        {0, 0,1,-1, 0,0,-1,1}
    };
    
    static final double[][] BONUSES = {
        {-0.5, 0.5,   1,   1, 0.5,-0.5,  -1,  -1},
        {  -1,  -1,-0.5, 0.5,   1,   1, 0.5,-0.5}
    };

    //the first number is a boolean which controls whether the slope or inverse slope is used
    //-1 refers to inverse slope...1 refers to slope
    static final double[][] NEWENDSLOPE_CORNER = {
        {-0.5, 0.5,-0.5,-0.5, 0.5,-0.5, 0.5, 0.5},
        { 0.5, 0.5,-0.5, 0.5,-0.5,-0.5, 0.5,-0.5}
    };
    
    static final double[][] NEWSTARTSLOPE_CORNER = {
        {-0.5, 0.5, 0.5, 0.5, 0.5,-0.5,-0.5,-0.5},
        {-0.5,-0.5,-0.5, 0.5, 0.5, 0.5, 0.5,-0.5}
    };
    
    static final int[] INVERSE_SLOPE_HANDLER  = {1, 1,-1,-1, 1, 1,-1,-1};
    static final int[] NEGATIVE_SLOPE_HANDLER = {1,-1,-1, 1, 1,-1,-1, 1};
        
    //a driver for the long-form, recursive scan
    public FieldOfViewScan(GameObject inOrigin, int inVisRange) {
//        numberOfScans = 0; 
//        numberOfVisibleTiles = 0;
        
        origin = inOrigin;
        visRange = inVisRange;
        handlingMap = origin.handlingMap;  
        
//        System.out.println("new scan...");
        //NUMBER_OF_OCTANTS
        for(octant = 0; octant < NUMBER_OF_OCTANTS; octant++) {
            scanLine(START_DEPTH,START_STARTSLOPE,DEFAULT_ENDSLOPE);
        }
        
        handlingMap.getTile(origin.getX(), origin.getY()).doFOVaction(origin, true);
//        System.out.println("Scan Complete!");
//        System.out.println("Number of scans = " + numberOfScans);
//        System.out.println("Number of Visible Items = " + numberOfVisibleTiles);
    }

    
    void scanLine(int depth, double startSlope, double endSlope) {      
        double bonusX = (Math.abs(BONUSES[0][octant]) == 0.5)? Math.round(startSlope*depth*2*BONUSES[0][octant]) : depth*BONUSES[0][octant];
        double bonusY = (Math.abs(BONUSES[1][octant]) == 0.5)? Math.round(startSlope*depth*2*BONUSES[1][octant]) : depth*BONUSES[1][octant];    
        double newStartSlope = startSlope;
        
        PreciseCoordinate coords = new PreciseCoordinate(origin.getX() + bonusX  + 0.5, origin.getY() + bonusY  + 0.5);
 
        
//        System.out.println("new while: currentSlope = " + getCurrentSlope(coords) + ", endSlope = " + endSlope);
        while(getCurrentSlope(new PreciseCoordinate(coords.getX() + NEWENDSLOPE_CORNER[0][octant], coords.getY() + NEWENDSLOPE_CORNER[1][octant])) > endSlope 
                && depth < visRange) {
//            System.out.println(new PreciseCoordinate(coords.getX() - NEWENDSLOPE_CORNER[0][octant], coords.getY() - NEWENDSLOPE_CORNER[1][octant]) + "," + endSlope);
            if(handlingMap.isValidTile(coords) && getDistance(coords,origin.getX(),origin.getY()) < visRange) {
                newStartSlope = visibleAreaChecks(coords, depth, newStartSlope, endSlope);                   
            }
            //go to the next tile
            coords.addToX((double)INCREMENTS[0][octant]);
            coords.addToY((double)INCREMENTS[1][octant]); 
//            System.out.println(new PreciseCoordinate(coords.getX() - NEWENDSLOPE_CORNER[0][octant], coords.getY() - NEWENDSLOPE_CORNER[1][octant]) + "," + endSlope);
//            System.out.println(getCurrentSlope(new PreciseCoordinate(coords.getX() - NEWENDSLOPE_CORNER[0][octant], coords.getY() - NEWENDSLOPE_CORNER[1][octant])));
        }  
    }
    
    private double visibleAreaChecks (PreciseCoordinate coords, Integer depth, 
            double startSlope, double endSlope) {
        handlingMap.getTile(coords).doFOVaction(origin, depth == 1); 
        
        boolean thisTileIsBlocking  = handlingMap.getTile(coords).hasBlockingObject(); 
        boolean priorTileIsBlocking = getPriorTile(coords).hasBlockingObject(); 
        
        double newStartSlope = startSlope;
        
        PreciseCoordinate endingCorner  = new PreciseCoordinate(coords.getX() - NEWENDSLOPE_CORNER[0][octant], coords.getY() - NEWENDSLOPE_CORNER[1][octant]);
        PreciseCoordinate leadingCorner = new PreciseCoordinate(coords.getX() + NEWENDSLOPE_CORNER[0][octant], coords.getY() + NEWENDSLOPE_CORNER[1][octant]);
        PreciseCoordinate bottomRight = new PreciseCoordinate(coords.getX() - NEWSTARTSLOPE_CORNER[0][octant], coords.getY() - NEWSTARTSLOPE_CORNER[1][octant]);
        
//        System.out.println("endingCorner:" + endingCorner);
//        System.out.println("coords:" + coords + "################################################");
//        System.out.println("endslope: " + endSlope);
//        System.out.println("bottomRight: " + bottomRight + ", slopeTo = " + getCurrentSlope(bottomRight));
        
        if (!thisTileIsBlocking && priorTileIsBlocking){
//            System.out.println("1");
            newStartSlope = slopeWRTQuadrant(coords.getX() + NEWSTARTSLOPE_CORNER[0][octant],coords.getY() + NEWSTARTSLOPE_CORNER[1][octant], origin.getX(),origin.getY());
        }
        if(thisTileIsBlocking && !priorTileIsBlocking) {              
//            System.out.println("2");
            double newEndSlope = getCurrentSlope(leadingCorner);
            if (newEndSlope <= START_STARTSLOPE) {
                scanLine(depth + 1, startSlope, newEndSlope);
            }     
        }
        else if (getCurrentSlope(bottomRight) <= endSlope && !thisTileIsBlocking) {
//            System.out.println("3");
            //Math.max(getCurrentSlope(endingCorner), 0)
            scanLine(depth + 1, newStartSlope, endSlope);
        }
//        System.out.println("!");
        
        return newStartSlope;
    }
    
    double getCurrentSlope(PreciseCoordinate coords) {
        double currentSlope;    
        //if the current octant is in Quadrants I or III, use the standard slope
        currentSlope = slopeWRTQuadrant(coords.getX(), coords.getY(),(double)origin.getX()+0.5,(double)origin.getY()+0.5);
//        System.out.println("slope = " + currentSlope + "," + coords);   
        return currentSlope;
    }
    
    static double getDistance(PreciseCoordinate coords, int originX, int originY) {
        double srcX = (double)originX;
        double srcY = (double)originY;
        return Math.sqrt(Math.pow((coords.getX()-originX),2) + Math.pow((coords.getY()-originY),2));
    }
        
    //always returns a non-infinite number
    //if called by getCurrentSlope->slopeWRTQuadrant, x1 and y1 will refer to the target coordinates, and x2 and y2 will
    //refer to the origin coordinates
    double getSlope(double x1, double y1, double x2, double y2, int neg) {  
        if(x1-x2 != 0){
            //System.out.println(neg  + "*(" + y1 + "-" + y2 + ")/(" + x1 + "-" + x2 +")");
            return neg * (y1-y2)/(x1-x2);
        }
        else {
            return 0;
        }
    }

    //always returns a positive, non-infinite number
    double getInverseSlope(double x1, double y1, double x2, double y2, int neg) {
        if (getSlope(x1,y1,x2,y2, neg) != 0) {
            return (1.0 / getSlope(x1,y1,x2,y2, neg));
        }
        else {
            return 0;
        }
    }
    
    double slopeWRTQuadrant(double x1, double y1, double x2, double y2) {
        if(INVERSE_SLOPE_HANDLER[octant] < 0){
            return getSlope(x1,y1,x2,y2,NEGATIVE_SLOPE_HANDLER[octant]);
        }
        else {
            return getInverseSlope(x1,y1,x2,y2,NEGATIVE_SLOPE_HANDLER[octant]);
        }
    }

    public Tile getPriorTile(PreciseCoordinate coords) {
        if(handlingMap.isValidTile(coords.getX()-(double)INCREMENTS[0][octant], coords.getY()-(double)INCREMENTS[1][octant])) {                
            double x = Math.floor(coords.getX()-INCREMENTS[0][octant]);
            double y = Math.floor(coords.getY()-INCREMENTS[1][octant]);
            
            Tile tileToReturn = handlingMap.getTile(x, y);
            return tileToReturn;
        }
        else {
            return new Tile();
        }
    } 
}
