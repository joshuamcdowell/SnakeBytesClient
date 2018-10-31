import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.omg.CORBA.portable.InputStream;

public class Game extends JFrame implements MouseListener, KeyListener{

	String title = "Snake Bytes";
	private int WIDTH = 1280;
	private int HEIGHT = 720;
	private int titleBarOffset = 32;
	private Thread thread;
	private Thread receiveUpdater;
	
	private int gameState; // 0 = menu, 1 = in-game, 2 = death screen
	private Menu menu;
	
	private Map map;
	private Player player;
	private ArrayList<Enemy> enemies = new ArrayList<Enemy>();
	
	// Frequently used images
	private BufferedImage menuBG;
	
	// Stuff for server connection
	private boolean connecting;
	private boolean serverConnected;
	private boolean serverConnectError;
	private Socket socket;
	private OutputStream ostream;
	private PrintWriter pwrite;
	private BufferedReader receiveRead;
	
	public Game(){

		gameState = 0;
		menu = new Menu();
		map = new Map(80, 43, 16);
		addMouseListener(this);
		addKeyListener(this);
		loadImages();
		
		WindowListener exitListener = new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		        int confirm = JOptionPane.showOptionDialog(
		             null, "Are You Sure to Close Application?", 
		             "Exit Confirmation", JOptionPane.YES_NO_OPTION, 
		             JOptionPane.QUESTION_MESSAGE, null, null, null);
		        if (confirm == 0) {
		        	if(serverConnected){
		        		pwrite.print("QUIT");
			        	try{
			        		socket.close();
			        	}catch(Exception ex){
			        		ex.printStackTrace();
			        	}
		        	}
		        	
		           System.exit(0);
		        }
		    }
		};
		addWindowListener(exitListener);
		
		thread = new Thread() {
            public void run() {
            	// "Heart-beat" for the game
        		long lastTime = System.nanoTime();
        		final double ns = 1000000000.0 / 60.0;//60 times per second
        		double delta = 0;
        		requestFocus();
        		while(true) {
        			long now = System.nanoTime();
        			delta = delta + ((now-lastTime) / ns);
        			lastTime = now;
        			while (delta >= 1)//Make sure update is only happening 60 times a second
        			{
        				//handles all of the logic restricted time
        				update();
        				delta--;
        			}
        			render();//displays to the screen unrestricted time
        		}
            }
        };
        receiveUpdater = new Thread() {
            public void run() {
            	while(true){
            		try{
            			if(receiveRead.ready()){
            				String received = receiveRead.readLine();
                			if(received.contains("PMOVE:")){
                				String pname = received.substring(received.indexOf(":") + 1, received.indexOf("*"));
                				int skin = Integer.parseInt(received.substring(received.indexOf("*") + 1, received.indexOf("=")));
                				int x = Integer.parseInt(received.substring(received.indexOf("=") + 1, received.indexOf(";")));
                				int y = Integer.parseInt(received.substring(received.indexOf(";") + 1, received.indexOf(")")));
                				
                				int bodyLength = Integer.parseInt(received.substring(received.indexOf(")") + 1, received.indexOf("%")));
                				ArrayList<PlayerBody> newBody = new ArrayList<PlayerBody>();
                				if(bodyLength > 0){
                					// Now get body
                					System.out.println("ENEMY: " + pname + "   BODYSIZE: " + bodyLength);
                    				for(int i = 0; i < bodyLength; i++){
                    					// Get start and end strings
                    					String startString = "^";
                    					String endString = "$";
                    					for(int j = 0; j < i; j++){
                    						startString += "^";
                    						endString += "$";
                    					}
                    					
                    					int bx = 0;
                    					int by = 0;
                    					String bodyParts = received.substring(received.indexOf("^"));
                    					String coords = "";
                    					// Now start parsing through string
                    					coords = bodyParts.substring(bodyParts.indexOf(startString) + 1, bodyParts.indexOf(endString));
                    					coords = coords.replace("^", "");
                    					coords = coords.replace("$", "");
                    					System.out.println(bodyParts + ":" + coords);
                    					bx = Integer.parseInt(coords.substring(0, coords.indexOf("#"))) + 1;
                    					by = Integer.parseInt(coords.substring(coords.indexOf("#") + 1));
                    					newBody.add(new PlayerBody(bx, by));
                    				}
                				}
                				
                				
                				
                				// If enemy is not in list of enemies, add it, else update it
                				boolean inGame = false;
                				int index = 0;
                				for(int i = 0; i < enemies.size(); i++){
                					if(enemies.get(i).getName().equals(pname)){
                						inGame = true;
                						index = i;
                					}
                				}
                				if(inGame){
                					enemies.get(index).update(x, y);
                					enemies.get(index).updateBody(newBody);
                				}
                				else{
                					// Add it
                					enemies.add(new Enemy(pname, skin));
                				}
                			}
                		}
            		}catch(Exception e){
            			e.printStackTrace();
            		}
            	}
            }
        };
		
		setTitle(title);
		setSize(WIDTH, HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		
		setVisible(true);
		start();
	}
	
	public static void main(String[] args){
		Game g = new Game();
	}
	
	public void loadImages(){
		menuBG = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		try{
			menuBG = ImageIO.read(getClass().getResourceAsStream("res/menubg.png"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void start(){
		thread.start();
	}
	
	public void stop(){
		try{
			thread.join();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void update(){
		if(gameState == 0){
			menu.update();
			
			if(serverConnectError && menu.getMessageCounter() == 399){
				serverConnectError = false;
			}
		}
		else if(gameState == 1){
			
			if(player != null){
				if(serverConnected){
					sendToServer();
				}
				player.update();
				checkPlayerDeath();
				checkPlayerSnack();
			}
			map.update();
		}
	}
	
	public void connectToServer(){
		connecting = true;
		try {
			// Initialize server variables
			//socket = new Socket("138.47.129.40", 1978); // Use IP address for machine hosting the server!
			socket = new Socket(menu.getIP(), 3000); // Use IP address for machine hosting the server!
		
			// sending to client (pwrite object)
			ostream = socket.getOutputStream(); 
			pwrite = new PrintWriter(ostream, true);
		
		    // receiving from server ( receiveRead  object)
			java.io.InputStream istream = socket.getInputStream();
			receiveRead = new BufferedReader(new InputStreamReader(istream));
			receiveUpdater.start();
			
			// Try sending player information to server
			String playerInfo = "JOIN:" + menu.getName() + ";" + menu.getSkin();
			
			pwrite.println(playerInfo);
			pwrite.flush();
			
			serverConnected = true;
			serverConnectError = false;
		}
		catch(Exception e){
			e.printStackTrace();
			serverConnected = false;
			serverConnectError = true; // Used to display error message to user
			menu.setMessageCounter(0);
		}
		connecting = false;
	}
	
	public void sendToServer(){
		// Send player location info, collision with enemy, etc.
		// Calculate ping?
		if(player.hasMoved()){
			String body = "";
			String startString = "^";
			String endString = "$";
			for(int i = 0; i < player.getBody().size(); i++){
				for(int j = 0; j < i; j++){
					startString += "^";
					endString += "$";
				}
				body += startString;
				body += player.getBody().get(i).getX() + "#" + player.getBody().get(i).getY();
				body += endString;
			}
			String playerInfo = "UPDATE:" + player.getName() + "=" + player.getX() + "," + player.getY() + "*" + player.getBody().size() + "%" + body;
			pwrite.println(playerInfo);
			pwrite.flush();
		}
	}
	
	public void joinGame(){
		// Try to connect to server
		if(!serverConnected){
			connectToServer();
		}
		// If connection is successful, start game
		if(serverConnected){
			gameState = 1;
			spawnPlayer();
		}
	}
	
	public void spawnPlayer(){
		Random rand = new Random();
		int randX = rand.nextInt(map.getWidth() - 4) + 2;
		int randY = rand.nextInt(map.getHeight() - 4) + 2;
		int randDir = rand.nextInt(4);
		player = new Player(randX, randY, 16, menu.getName(), menu.getSkin());
		
		// Give player a random starting direction
		if(randDir == 0){
			player.setDirection("up");
		}
		else if(randDir == 1){
			player.setDirection("down");
		}
		else if(randDir == 2){
			player.setDirection("left");
		}
		else if(randDir == 3){
			player.setDirection("right");
		}
	}
	
	public void checkPlayerDeath(){
		// Check border collision
		if(player.getX() == 0 || player.getX() == map.getWidth() - 1 || player.getY() == 0 || player.getY() == map.getHeight() - 1){
			player.setAlive(false);
		}
		
		if(!player.isAlive()){
			gameState = 2;
		}
	}
	
	public void checkPlayerSnack(){
		for(int i = 0; i < map.getSnacks().size(); i++){
			if(player.getX() == map.getSnacks().get(i).getX() && player.getY() == map.getSnacks().get(i).getY()){
				player.eatSnack();
				map.getSnacks().remove(i);
			}
		}
	}
	
	public void render(){
		// Make sure game is ready to draw stuff
		BufferStrategy bs = getBufferStrategy();
		if(bs == null){
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		
		// Draw stuff here
		
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		if(gameState == 0){
			// Draw menu stuff
			g.drawImage(menuBG, 0, 30, WIDTH, HEIGHT, null);
			int borderWidth = 5;
			if(menu.getSkin() != -1){
				g.setColor(Color.YELLOW);
				g.fillRect(menu.getMenuButtons().get(menu.getSkin() + 1).getX() - borderWidth, menu.getMenuButtons().get(menu.getSkin() + 1).getY() - borderWidth, menu.getMenuButtons().get(menu.getSkin() + 1).getWidth() + borderWidth * 2, menu.getMenuButtons().get(menu.getSkin() + 1).getHeight() + borderWidth * 2);
			}
			for(int i = 0; i < menu.getMenuButtons().size(); i++){
				if(menu.getMenuButtons().get(i).isSelected()){ // Draw border around selected button
					g.setColor(Color.YELLOW);
					g.fillRect(menu.getMenuButtons().get(i).getX() - borderWidth, menu.getMenuButtons().get(i).getY() - borderWidth, menu.getMenuButtons().get(i).getWidth() + borderWidth * 2, menu.getMenuButtons().get(i).getHeight() + borderWidth * 2);
				}
				g.setColor(menu.getMenuButtons().get(i).getColor());
				g.fillRect(menu.getMenuButtons().get(i).getX(), menu.getMenuButtons().get(i).getY(), menu.getMenuButtons().get(i).getWidth(), menu.getMenuButtons().get(i).getHeight());
			}

			// Draw some texts
			g.setColor(Color.BLACK);
			Font buttonFont = new Font("Monospaced", Font.BOLD, 20);
			g.setFont(buttonFont);
			if(menu.getName() == ""){
				g.drawString("Enter a name", menu.getMenuButtons().get(0).getX() + 50, menu.getMenuButtons().get(0).getY() + 35);
			}
			else{
				g.drawString(menu.getName(), menu.getMenuButtons().get(0).getX() + 7, menu.getMenuButtons().get(0).getY() + 35);
			}
			if(menu.getIP() == ""){
				g.drawString("Enter Server IP", menu.getMenuButtons().get(1).getX() + 36, menu.getMenuButtons().get(1).getY() + 35);
			}
			else{
				g.drawString(menu.getIP(), menu.getMenuButtons().get(1).getX() + 7, menu.getMenuButtons().get(1).getY() + 35);
			}
			if(connecting){
				String conString = "Connecting";
				// Used to determine number of periods after the word for a little animation
				for(int i = 0; i < menu.getMessageCounter()/10; i++){
					conString += ".";
				}
				if(menu.getMessageCounter() >= 40){
					menu.setMessageCounter(0);
				}
				g.drawString(conString, menu.getMenuButtons().get(menu.getMenuButtons().size() - 1).getX() + 50, menu.getMenuButtons().get(menu.getMenuButtons().size() - 1).getY() + 35);
			}
			else if(serverConnectError){
				g.drawString("Could not connect.", menu.getMenuButtons().get(menu.getMenuButtons().size() - 1).getX() + 20, menu.getMenuButtons().get(menu.getMenuButtons().size() - 1).getY() + 35);
			}
			else{
				g.drawString("Play!", menu.getMenuButtons().get(menu.getMenuButtons().size() - 1).getX() + 93, menu.getMenuButtons().get(menu.getMenuButtons().size() - 1).getY() + 35);
			}
			
			Font instructionFont = new Font("Monospaced", Font.BOLD, 30);
			g.setFont(instructionFont);
			g.setColor(Color.WHITE);
			g.drawString("Pick a color", 520, 430);
		}
		else if(gameState == 1){
			// Draw game stuff
			// Draw map
			g.setColor(Color.BLACK);
			for(int i = 0; i < map.getWidth(); i++){
				for(int j = 0; j < map.getHeight(); j++){
					if(i == 0 || i == map.getWidth() - 1 || j == 0 || j == map.getHeight() - 1){
						g.fillRect(i * map.getTileSize(), j * map.getTileSize() + titleBarOffset, map.getTileSize(), map.getTileSize());
					}
					else{
						g.drawRect(i * map.getTileSize(), j * map.getTileSize() + titleBarOffset, map.getTileSize(), map.getTileSize());
					}
				}
			}
			// Draw player
			if(player != null){
				g.setColor(getPlayerColor(player.getSkin()));
				g.fillRect(player.getX() * player.getTileSize(), player.getY() * player.getTileSize() + titleBarOffset, player.getTileSize(), player.getTileSize());
				g.drawString(player.getName(), player.getX() * player.getTileSize() + 20, player.getY() * player.getTileSize() + titleBarOffset - 2);
				for(int i = 0; i < player.getBody().size(); i++){
					g.fillRect(player.getBody().get(i).getX() * player.getTileSize(), player.getBody().get(i).getY() * player.getTileSize() + titleBarOffset, player.getTileSize(), player.getTileSize());
				}
			}
			// Draw enemies
			for(int i = 0; i < enemies.size(); i++){
				g.setColor(getPlayerColor(enemies.get(i).getSkin()));
				g.fillRect(enemies.get(i).getX() * player.getTileSize(), enemies.get(i).getY() * player.getTileSize() + titleBarOffset, player.getTileSize(), player.getTileSize());
				g.drawString(enemies.get(i).getName(), enemies.get(i).getX() * player.getTileSize() + 20, enemies.get(i).getY() * player.getTileSize() + titleBarOffset - 2);
				for(int j = 0; j < enemies.get(i).getBody().size(); j++){
					//System.out.println("DRAWING ENEMY BODY PART:" + enemies.get(i).getBody().get(j).getX() + ":" + enemies.get(i).getBody().get(j).getY());
					//System.out.println("ENEMIE BODY SIZE: " + enemies.get(i).getBody().size() + ":" + j);
					//System.out.println("ENEMY: " + enemies.get(i).getName() + "  BODYSIZE: " + enemies.get(i).getBody().size());
					for(int z = 0; z < enemies.get(i).getBody().size(); z++){
						//System.out.println(enemies.get(i).getName() + ":" + enemies.get(i).getBody().get(z).getX() + ":" + enemies.get(i).getBody().get(z).getY());
					}
					g.fillRect(enemies.get(i).getBody().get(j).getX() * player.getTileSize(), enemies.get(i).getBody().get(j).getY() * player.getTileSize() + titleBarOffset, player.getTileSize(), player.getTileSize());	
					//g.fillRect(enemies.get(i).getX() * player.getTileSize() - j * player.getTileSize(), enemies.get(i).getBody().get(j).getY() * player.getTileSize() + titleBarOffset, player.getTileSize(), player.getTileSize());	
				}
			}
			
			// Draw snacks
			g.setColor(Color.WHITE);
			for(int i = 0; i < map.getSnacks().size(); i++){
				g.fillOval(map.getSnacks().get(i).getX() * map.getTileSize(), map.getSnacks().get(i).getY() * map.getTileSize() + titleBarOffset, map.getTileSize(), map.getTileSize());
			}
		}
		else if(gameState == 2){
			g.setColor(Color.RED);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			
			Font deathFont = new Font("Monospaced", Font.BOLD, 40);
			g.setFont(deathFont);
			g.setColor(Color.BLACK);
			g.drawString("You have died!", 450, 400);
			g.drawString("Press space bar to go to menu", 300, 500);
			g.drawString("Press enter to respawn", 370, 600);
		}
		
		bs.show();
	}
	
	public Color getPlayerColor(int c){
		Color color = Color.WHITE;
		if(player == null){
			return color;
		}
		
		
		if(c == 1){
			color = Color.BLUE;
		}
		else if(c == 2){
			color = Color.GREEN;
		}
		else if(c == 3){
			color = Color.MAGENTA;
		}
		else if(c == 4){
			color = Color.CYAN;
		}
		else if(c == 5){
			color = Color.ORANGE;
		}
		else if(c == 6){
			color = Color.PINK;
		}
		else if(c == 7){
			color = Color.YELLOW;
		}
		else if(c == 8){
			color = Color.WHITE;
		}
		else if(c == 9){
			color = Color.RED;
		}
		
		return color;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if(gameState == 0){
			// Check for button click
			for(int i = 0; i < menu.getMenuButtons().size(); i++){
				menu.getMenuButtons().get(i).setSelected(false); // Used to make sure all other buttons are unselected
				if(x > menu.getMenuButtons().get(i).getX() && x < menu.getMenuButtons().get(i).getX() + menu.getMenuButtons().get(i).getWidth()){
					if(y > menu.getMenuButtons().get(i).getY() && y < menu.getMenuButtons().get(i).getY() + menu.getMenuButtons().get(i).getHeight()){
						// CLICK
						menu.getMenuButtons().get(i).setSelected(true);
						if(i == menu.getMenuButtons().size() - 1 && menu.canPlay()){ // If the play button is clicked and all requirements are met to join, start game
							joinGame();
						}
					}
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println(e.getKeyCode());
		
		if(gameState == 0){
			if(menu.getMenuButtons().get(0).isSelected()){
				if(e.getKeyCode() == 8){
					// Backspace
					if(menu.getName().length() > 0){
						menu.setName(menu.getName().substring(0, menu.getName().length() - 1));
						if(menu.getName().length() == 0){
							menu.resetName();
						}
					}
				}
				else if(e.getKeyCode() != 16 && e.getKeyCode() != 17 && e.getKeyCode() != 18 && e.getKeyCode() != 20 && e.getKeyCode() != 38 && e.getKeyCode() != 40 && e.getKeyCode() != 37 && e.getKeyCode() != 39 && e.getKeyCode() != 59){
					// Add a character to the end of the name
					if(menu.getName().length() < menu.getNameMaxLength()){
						menu.setName(menu.getName().concat(e.getKeyChar() + ""));
					}
				}
			}
			else if(menu.getMenuButtons().get(1).isSelected()){
				if(e.getKeyCode() == 8){
					// Backspace
					if(menu.getIP().length() > 0){
						menu.setIP(menu.getIP().substring(0, menu.getIP().length() - 1));
						if(menu.getIP().length() == 0){
							menu.resetIP();
						}
					}
				}
				// Make sure key is an integer or period
				else if(e.getKeyCode() == 49 || e.getKeyCode() == 50 || e.getKeyCode() == 51 || e.getKeyCode() == 52 || e.getKeyCode() == 53 || e.getKeyCode() == 54 || e.getKeyCode() == 55 || e.getKeyCode() == 56 || e.getKeyCode() == 57 || e.getKeyCode() == 48 || e.getKeyCode() == 46){
					// Add a character to the end of the IP
					if(menu.getIP().length() < menu.getIPMaxLength()){
						menu.setIP(menu.getIP().concat(e.getKeyChar() + ""));
					}
				}
			}
		}
		else if(gameState == 1){
			if(e.getKeyCode() == 38){
				player.setDirection("up");
			}
			else if(e.getKeyCode() == 40){
				player.setDirection("down");
			}
			else if(e.getKeyCode() == 37){
				player.setDirection("left");
			}
			else if(e.getKeyCode() == 39){
				player.setDirection("right");
			}
		}
		else if(gameState == 2){
			if(e.getKeyCode() == 32){
				gameState = 0;
			}
			else if(e.getKeyCode() == 10){
				if(serverConnected){
					gameState = 1;
					spawnPlayer();
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
