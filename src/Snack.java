
public class Snack {

	private int x;
	private int y;
	
	private String id;
	
	public Snack(String id, int x, int y){
		this.x = x;
		this.y = y;
		this.id = id;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public String getID(){
		return id;
	}
}
