import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Enemy {

	private String name;
	private int skin;
	private int x;
	private int y;
	
	private int offlineCounter;
	
	private List<PlayerBody> body = new ArrayList<PlayerBody>();
	
	public Enemy(String name, int skin){
		this.name = name;
		this.skin = skin;
	}
	
	public void update(int x, int y){
		this.x = x;
		this.y = y;
		offlineCounter = 0;
	}
	
	public void updateBody(List<PlayerBody> newBody){
		body = new ArrayList<PlayerBody>();
		body = Collections.synchronizedList(body);
		for(int i = 0; i < newBody.size(); i++){
			body.add(new PlayerBody(newBody.get(i).getX() - 1, newBody.get(i).getY()));
		}
	}
	
	public int getOfflineCounter(){
		return offlineCounter;
	}
	
	public void setOfflineCounter(int olc){
		offlineCounter = olc;
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
	
	public List<PlayerBody> getBody(){
		return body;
	}
}
