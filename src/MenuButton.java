import java.awt.Color;

public class MenuButton {

	private int x;
	private int y;
	private int width;
	private int height;
	
	private boolean selected;
	
	private Color color;
	
	public MenuButton(int x, int y, int width, int height, Color color){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
	}
	
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public int getWidth(){
		return width;
	}
	public int getHeight(){
		return height;
	}
	public boolean isSelected(){
		return selected;
	}
	public void setSelected(boolean b){
		selected = b;
	}
	public Color getColor(){
		return color;
	}
	public void setColor(Color c){
		color = c;
	}
}
