import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Enemy {

	private String name;
	private int skin;
	private int x;
	private int y;
	
	private List<PlayerBody> body = new ArrayList<PlayerBody>();
	
	public Enemy(String name, int skin){
		this.name = name;
		this.skin = skin;
	}
	
	public void update(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void updateBody(List<PlayerBody> newBody){
		body = new ArrayList<PlayerBody>();
		body = Collections.synchronizedList(body);
		//synchronized(body){
			for(int i = 0; i < newBody.size(); i++){
				body.add(new PlayerBody(newBody.get(i).getX() - 1, newBody.get(i).getY()));
			}
		//}
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
