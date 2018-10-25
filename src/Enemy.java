import java.util.ArrayList;

public class Enemy {

	private String name;
	private int skin;
	private int x;
	private int y;
	
	private ArrayList<PlayerBody> body = new ArrayList<PlayerBody>();
	
	public Enemy(String name, int skin){
		this.name = name;
		this.skin = skin;
	}
	
	public void update(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void updateBody(ArrayList<PlayerBody> newBody){
		body = new ArrayList<PlayerBody>();
		for(int i = 0; i < newBody.size(); i++){
			body.add(new PlayerBody(newBody.get(i).getX() - 1, newBody.get(i).getY()));
		}
	}
	
	public String getName(){
		return name;
	}
	
	public int getSkin(){
		return skin;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public ArrayList<PlayerBody> getBody(){
		return body;
	}
}
