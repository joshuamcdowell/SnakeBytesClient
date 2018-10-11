
public class PlayerBody {

	private int x;
	private int y;
	
	public PlayerBody(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void move(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
}
