import java.util.ArrayList;

public class Player {

	private int x;
	private int y;
	private int tileSize;
	
	private String direction;
	private int speedCounter;
	private ArrayList<PlayerBody> body = new ArrayList<PlayerBody>();
	private boolean alive;
	
	private String name;
	private int skin;
	
	private boolean moved;
	
	public Player(int x, int y, int tileSize, String name, int skin){
		this.x = x;
		this.y = y;
		this.tileSize = tileSize;
		this.name = name;
		this.skin = skin;
		alive = true;
	}
	
	public void update(){
		moved = false;
		speedCounter++;
		if(speedCounter == 7){
			
			// Move all body parts in snake like fashion
			for(int i = body.size() - 1; i >= 0; i--){
				if(i == 0){
					body.get(0).move(x, y);
				}
				else{
					body.get(i).move(body.get(i - 1).getX(), body.get(i - 1).getY());
				}
			}
			
			// Move
			if(direction.equals("up")){
				y--;
			}
			else if(direction.equals("down")){
				y++;
			}
			else if(direction.equals("left")){
				x--;
			}
			else if(direction.equals("right")){
				x++;
			}
			moved = true;
			speedCounter = 0;
			
			
		}
	}
	
	public void updateBody(int index, int newX, int newY){
		if(index + 1 > body.size()){
			body.add(new PlayerBody(newX, newY));
		}
		else{
			body.get(index).move(newX, newY);
		}
	}
	
	public void eatSnack(){
		if(body.size() > 0){
			body.add(new PlayerBody(body.get(body.size() - 1).getX(), body.get(body.size() - 1).getY()));
		}
		else{
			body.add(new PlayerBody(x, y));
		}
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public int getTileSize(){
		return tileSize;
	}
	
	public void setDirection(String dir){
		direction = dir;
	}
	
	public ArrayList<PlayerBody> getBody(){
		return body;
	}
	
	public boolean isAlive(){
		return alive;
	}
	
	public void setAlive(boolean b){
		alive = b;
	}
	
	public String getName(){
		return name;
	}
	
	public int getSkin(){
		return skin;
	}
	
	public boolean hasMoved(){
		return moved;
	}
}
