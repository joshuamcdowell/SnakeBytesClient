import java.util.ArrayList;
import java.util.Random;

public class Map {

	private int width; // Number of tiles wide
	private int height; // Number of tiles tall
	private int tileSize; // Size of each tile
	
	private ArrayList<Snack> snacks = new ArrayList<Snack>();
	private int maxSnackSize;
	
	public Map(int width, int height, int tileSize){
		this.width = width;
		this.height = height;
		this.tileSize = tileSize;
		
		maxSnackSize = 20;
	}
	
	public void update(){
		if(snacks.size() < maxSnackSize){
			Random rand = new Random();
			snacks.add(new Snack(rand.nextInt(width - 4) + 2, rand.nextInt(height - 4) + 2));
		}
	}
	
	public ArrayList<Snack> getSnacks(){
		return snacks;
	}
	
	public int getTileSize(){
		return tileSize;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
}
