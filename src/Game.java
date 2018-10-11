import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.omg.CORBA.portable.InputStream;

public class Game extends JFrame implements Runnable, MouseListener, KeyListener{

	String title = "Snake Bytes";
	private int WIDTH = 1280;
	private int HEIGHT = 720;
	private int titleBarOffset = 32;
	private Thread thread;
	
	private int gameState; // 0 = menu, 1 = in-game, 2 = death screen
	private Menu menu;
	
	private Map map;
	private Player player;
	
	// Frequently used images
	private BufferedImage menuBG;
	
	// Stuff for server connection
	private boolean serverConnected;
	private Socket socket;
	private OutputStream ostream;
	private PrintWriter pwrite;
	private BufferedReader receiveRead;
	
	public Game(){
		thread = new Thread(this);
		gameState = 0;
		menu = new Menu();
		map = new Map(80, 43, 16);
		addMouseListener(this);
		addKeyListener(this);
		loadImages();
		
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
	
	public void connectToServer(){
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
			
			System.out.println("Connected to server");
			// Try sending player information to server
			String playerInfo = "JOIN:" + menu.getName() + ":" + menu.getSkin();
			System.out.println(playerInfo);
			serverConnected = true;
			pwrite.println(playerInfo);
			pwrite.flush();
		}
		catch(Exception e){
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

	@Override
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
	
	public void update(){
		if(gameState == 0){
			menu.update();
		}
		else if(gameState == 1){
			// Send player updates to server
			// Receive enemy updates from server
			sendToServer();
			receiveFromServer();
			player.update();
			checkPlayerDeath();
			checkPlayerSnack();
						
			map.update();
		}
	}
	
	public void sendToServer(){
		// Send player location info, collision with enemy, etc.
		// Calculate ping?
	}
	
	public void receiveFromServer(){
		String received = "";
		try {
			if((received = receiveRead.readLine()) != null){ //receive from server
				System.out.println(received); // displaying at DOS prompt
				// Parse information, update enemies, leader-board status, etc.
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void joinGame(){
		gameState = 1;
		// try to connect to server
		connectToServer();
		spawnPlayer();
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
		if(player.getX() == 0 || player.getX() == map.getWidth() || player.getY() == 0 || player.getY() == map.getHeight()){
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
			g.drawString("Play!", menu.getMenuButtons().get(menu.getMenuButtons().size() - 1).getX() + 93, menu.getMenuButtons().get(menu.getMenuButtons().size() - 1).getY() + 35);
			
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
				g.setColor(getPlayerColor());
				g.fillRect(player.getX() * player.getTileSize(), player.getY() * player.getTileSize() + titleBarOffset, player.getTileSize(), player.getTileSize());
				g.drawString(player.getName(), player.getX() * player.getTileSize() + 20, player.getY() * player.getTileSize() + titleBarOffset - 2);
				for(int i = 0; i < player.getBody().size(); i++){
					g.fillRect(player.getBody().get(i).getX() * player.getTileSize(), player.getBody().get(i).getY() * player.getTileSize() + titleBarOffset, player.getTileSize(), player.getTileSize());
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
	
	public Color getPlayerColor(){
		Color color = Color.WHITE;
		if(player == null){
			return color;
		}
		
		
		if(player.getSkin() == 1){
			color = Color.BLUE;
		}
		else if(player.getSkin() == 2){
			color = Color.GREEN;
		}
		else if(player.getSkin() == 3){
			color = Color.MAGENTA;
		}
		else if(player.getSkin() == 4){
			color = Color.CYAN;
		}
		else if(player.getSkin() == 5){
			color = Color.ORANGE;
		}
		else if(player.getSkin() == 6){
			color = Color.PINK;
		}
		else if(player.getSkin() == 7){
			color = Color.YELLOW;
		}
		else if(player.getSkin() == 8){
			color = Color.WHITE;
		}
		else if(player.getSkin() == 9){
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
				joinGame();
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
