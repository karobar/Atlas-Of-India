package drawing;

import event.EventProcessable;
import event.EventProcessor;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import objects.GameMap;
import objects.GameObject;
import utils.Translator;

public class MainFrame extends JFrame implements EventProcessable, KeyListener , ComponentListener{
//	static Graphics contentGraphics;
	static EventProcessor eventProcessor;
	static Container myPane;    
        Insets insets;
	final static int DELAY_TIME = 40; //should achieve an fps of 25
	public static int WIDTH_IN_SLOTS    = 80;
	public static int HEIGHT_IN_SLOTS   = 25;
	final static int CHAR_PIXEL_WIDTH  = 8;
	final static int CHAR_PIXEL_HEIGHT = 12;
	final static int FRAME_WIDTH  = CHAR_PIXEL_WIDTH * WIDTH_IN_SLOTS;
	final static int FRAME_HEIGHT = CHAR_PIXEL_HEIGHT * HEIGHT_IN_SLOTS;
        final static int IMAGE_GRID_WIDTH = 16;
        final static int VOLATILE_IMAGE_TRANSPARENCY = 0;
	static BufferedImage[][] charsheet;
        Graphics contentGraphics;
	static Screen currentScreen;
	static Screen previousScreen;
	static GameMap testMap;
        static GraphicsEnvironment   dasEnv;
        static GraphicsConfiguration dasConfig; 
        static Translator rosetta; 
        //long-term version plans
        //
        final static String VERSION_NUMBER = "Alpha v0.1.0";
        
        
	MainFrame() {
		//initialize the main game window
		super("Beings From Beyond Alpha");
		setResizable(false);
		eventProcessor = new EventProcessor(this);
                setDefaultCloseOperation(EXIT_ON_CLOSE);	
		setIgnoreRepaint(true);
		pack();
		insets = getInsets();
		
                setSize(CHAR_PIXEL_WIDTH  * WIDTH_IN_SLOTS  + insets.left + insets.right, 
                        CHAR_PIXEL_HEIGHT * HEIGHT_IN_SLOTS + insets.top  + insets.bottom); 
                setMinimumSize(new Dimension(CHAR_PIXEL_WIDTH  * WIDTH_IN_SLOTS + insets.left + insets.right, 
                                             CHAR_PIXEL_HEIGHT * HEIGHT_IN_SLOTS+ insets.top  + insets.bottom));
		setVisible(true);
                
                addKeyListener(this);
                addComponentListener(this);
		
                //create graphics on the main pane
		myPane = this.getContentPane();
		myPane.setLayout(null);
//		contentGraphics = myPane.getGraphics();
                
                //works fine, just waiting on a decent icon
//                Image icon = null;
//                try {
//			icon = (BufferedImage)ImageIO.read(new File("src/AppIcon.png"));
//		}  catch (IOException e) {
//                    System.out.println("Failed loading image!");
//                    System.exit(0);
//                }
//                setIconImage(icon);
                
                //do some fancy system optimizations
                dasEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
                dasConfig = dasEnv.getDefaultScreenDevice().getDefaultConfiguration();
                
                this.createBufferStrategy(2);
	}
	
	public static void main(String[] args) {
                //read in the character sheet for drawing stuff
		BufferedImage rawCharSheet = null;
                try {
			rawCharSheet = (BufferedImage)ImageIO.read(new File("src/drawing/charsheet.bmp"));
		}  catch (IOException e) {
                    System.out.println("Failed loading image!");
                    System.exit(0);
                }
                //separates the character sheet into 256 individual tiles
                charsheet = separateSheet(rawCharSheet);
		
                BufferedImage titleScreen = null;
                try {
                    titleScreen = (BufferedImage)ImageIO.read(new File("src/drawing/title01.bmp"));
                }  catch (IOException e) {
                    System.out.println("Failed loading title screen!");
                    System.exit(0);
                }
                
                ImageRepresentation[][] translatedTitles = ImageRepresentation.bmpToImRep(titleScreen);
                
		//create the frame
		MainFrame mainFrame = new MainFrame();
		currentScreen = previousScreen = new TitleScreen(translatedTitles);
		
		rosetta = new Translator();
		
		//create game objects
		Random dice = new Random();
                int x = dice.nextInt(200);
                int y = dice.nextInt(60);
                testMap = new GameMap(x , y);
		testMap.populate();
		testMap.updateObjects();
		
		//run the main loop
		while(true) {
			eventProcessor.processEventList();
			testMap.updateObjects();
                        Graphics contentGraphics = myPane.getGraphics();
			currentScreen.render(contentGraphics);
                        contentGraphics.dispose();
		}
	}
	

	//add key events to the general list of events
        @Override
	public void keyPressed(KeyEvent e) { 
		eventProcessor.addEvent(e); 
	}
        @Override
	public void keyReleased(KeyEvent e){ 
		eventProcessor.addEvent(e); 
	}
        @Override
	public void keyTyped(KeyEvent e) {} 
	
        //add focus events to the general list of events 
	public void focusGained(FocusEvent e) { 
		eventProcessor.addEvent(e); 
	}
	public void focusLost(FocusEvent e) { 
		eventProcessor.addEvent(e); 
	}
	
	//redirect events to their specific screen for handling
        @Override
	public void handleEvent(AWTEvent e) {
		currentScreen.handleEvents(e);
	}

        @Override
        public void componentResized(ComponentEvent e) {

            eventProcessor.addEvent(e);
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            eventProcessor.addEvent(e);
        }

        @Override
        public void componentShown(ComponentEvent e) {
            eventProcessor.addEvent(e);
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            eventProcessor.addEvent(e);
        }
        
        static int getDrawAreaWidth() {
            return myPane.getWidth();
        }
        
        static int getDrawAreaHeight() {
            return myPane.getHeight();
        }
        
        Translator getTranslator() {
            return rosetta;
        }
        
        //Precondition: srcScheet is a BufferedImage of 16x16 tiles
        //Postcondition: 256 VolatileImages in a 2-dimensional array
        //Ha, ha! Fancy, eh?
        //I'm hoping this will reduce some of the work-load during drawing if I can avoid calling getSubImage per every tile.
        static BufferedImage[][] separateSheet(BufferedImage srcSheet) {
            BufferedImage[][] imageArray = new BufferedImage[IMAGE_GRID_WIDTH][IMAGE_GRID_WIDTH];
            
            for(int i = 0; i < IMAGE_GRID_WIDTH; i++) {
                for(int j = 0; j < IMAGE_GRID_WIDTH; j++) {
                    imageArray[i][j] = srcSheet.getSubimage(i*CHAR_PIXEL_WIDTH,j*CHAR_PIXEL_HEIGHT,CHAR_PIXEL_WIDTH,CHAR_PIXEL_HEIGHT);   
                }
            }
            
            return imageArray;
        }
        
        static VolatileImage createVolatileImage() {
            VolatileImage currImg = dasConfig.createCompatibleVolatileImage(CHAR_PIXEL_WIDTH,CHAR_PIXEL_HEIGHT, VOLATILE_IMAGE_TRANSPARENCY);
            
            //if the image had an error, recreate the image 
            int validity = currImg.validate(dasConfig);
            if(validity == VolatileImage.IMAGE_INCOMPATIBLE) {
                //woah, recursion? Gettin' fancy...  
                currImg = createVolatileImage();
                return currImg;
            }           
            
            return currImg;
        }
}


