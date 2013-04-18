package objects;

import AI.AI;
import AI.Compass;
import AI.MovementDesire;
import drawing.ImageRepresentation;
import java.util.Random;
import java.util.Stack;
import lighting.LightingElement;

public class GameObject {
	int x, y;
	int precedence;
	boolean blocking = false;
	ImageRepresentation ir;
	String name;
	GameMap handlingMap;
        LightingElement light;
	
	//current implementation fills with Tiles
	Stack desires = new Stack();
        
        //creates a completely bland GameObject for polymorphic purposes
        GameObject() {}
	
	GameObject(String name, ImageRepresentation ir, boolean blocking, int precedence, GameMap handlingMap) {
		
		
		this.blocking = blocking;
		this.ir = ir;
		this.precedence = precedence;
		this.name = name;
		this.handlingMap = handlingMap;
		
		int[] coords = validPositionRolls(handlingMap);
		//System.out.println("postpassed = " + coords[0] + " " + coords[1]);
		setX(coords[0]);
		setY(coords[1]);
		
		updateBackgroundColor(x,y);
		handlingMap.addActor(this);
	}
	
	GameObject(String name, ImageRepresentation ir, int x, int y, boolean blocking, int precedence, GameMap handlingMap) {
		setX(x);
		setY(y);
		this.blocking = blocking;
		this.ir = ir;
		this.precedence = precedence;
		this.name = name;
		this.handlingMap = handlingMap;
		
		updateBackgroundColor(x,y);
		handlingMap.addActor(this);
	}
        
        GameObject(String name, ImageRepresentation ir, LightingElement light,
                int x, int y, boolean blocking, int precedence, GameMap handlingMap) {
		
                setX(x);
		setY(y);
		this.ir = ir;
                this.light = light;
                this.blocking = blocking;
		this.precedence = precedence;
		this.name = name;
		this.handlingMap = handlingMap;
		
		updateBackgroundColor(x,y);
		handlingMap.addActor(this);
	}
	
	private int[] validPositionRolls(GameMap rollingArea) {
		int[] coords = new int[2];
		
		Random dice = new Random();
		Tile isItValid;
                
		do {
			coords[0] = dice.nextInt(rollingArea.width);
			coords[1] = dice.nextInt(rollingArea.height); 
			
                        isItValid = handlingMap.getTile(coords[0], coords[1]);
                        
		} while(isItValid.hasBlockingObject());
		
		return coords;
	}
	
	//Should generalize this so that one can order an object to move along a path(either Tile[] or Compass[])
	public void move(Compass direction) {
		desires.push(direction);
	}
	
	void resolveImmediateDesire() {
		//System.out.println("resolving: " + desires);
		//System.out.println();
		
		//curr can be either Compass directions or Tiles
		MovementDesire curr = (MovementDesire)desires.pop();
		int[] coords = curr.getCoords(getTile());
		
		//make sure that the desired target position is valid
		if(!collision(coords[0], coords[1])) {
			handlingMap.getTile(this.getX(), this.getY()).remove(this);
                        
                        //System.out.println("Setting x from " + getX() + " to " + curr.getX());
			this.setX(coords[0]);
			
			//System.out.println("Setting y from " + getY() + " to " + curr.getY());
			this.setY(coords[1]);
			
			updateBackgroundColor(x,y);
		}
	}
	
	public void timestepMove(Compass direction) {
            //System.out.println("timestepmovin...");
            move(direction);
            handlingMap.moveNPCs();
            handlingMap.resolveDesires();
	}
	
	public boolean collision(int x, int y) {
		if(x < 0 || y < 0 || x >= handlingMap.width || y >= handlingMap.height || handlingMap.getTile(x,y).hasBlockingObject())
			return true;
		return false;
	}
	
	public boolean isBlocking() {
		return blocking;
	}
	
	int getBackColor() { 
		return this.ir.getBackColor();
	}
	
	//moves 3 times in a random direction
	void idleMove() {
		Compass[] dirs = Compass.values();
		Random dice = new Random();
		
		final int MIN_PATH_LENGTH = 3;
                final int MAX_PATH_LENGTH = 10;
		int roll = dice.nextInt(dirs.length);
		int randomPathLength = dice.nextInt(MAX_PATH_LENGTH-MIN_PATH_LENGTH) + MIN_PATH_LENGTH;
		
		for(int i = 0; i < randomPathLength; i++) {
			move(dirs[roll]);
		}
	}
	
	Tile getTile() {
		return handlingMap.map[getX()][getY()];
	}
	
	//Move to a random position using A*
	void randomMove() {
		int[] coords = validPositionRolls(handlingMap);
		Tile start = handlingMap.getTile(getX(), getY());
		Tile goal = handlingMap.getTile(coords[0], coords[1]);
		
		desires = AI.AStar(start, goal, handlingMap);
                handlingMap.clearFGH();
        }
	
	void setBackground(int newBackColor) {
		this.ir.setBackColor(newBackColor);
	}
	
	void updateBackgroundColor(int x, int y) {
		if(getTile().size() > 0) {
			//System.out.println("updatin background colour...");
			setBackground(handlingMap.getUnderlyingColor(x,y));
		}
	}
	
	public ImageRepresentation getRepresentation() {
		return this.ir;
	}
	
        LightingElement getLightingElement() {
            return this.light;
        }
        
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	private void setX(int newX) {
		this.x = newX;
	}
	
	private void setY(int newY) {
		this.y = newY;
	}
	
	String getName() {
		return name;
	}
	
	int getPrecedence() {
		return precedence;
	}
	
    @Override
	public String toString() {
		return this.ir.toString();
	}
}