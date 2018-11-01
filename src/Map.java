import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

public class Map {

	private int width; // Number of tiles wide
	private int height; // Number of tiles tall
	private int tileSize; // Size of each tile
	
	private ArrayList<Snack> snacks = new ArrayList<Snack>();
	
	public Map(int width, int height, int tileSize){
		this.width = width;
		this.height = height;
		this.tileSize = tileSize;
	}
	
	public ArrayList<Snack> getSnacks(){
		return snacks;
	}
	
	public void addSnack(Snack s){
		snacks.add(s);
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
