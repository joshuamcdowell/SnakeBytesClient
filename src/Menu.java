import java.awt.Color;
import java.util.ArrayList;

public class Menu {

	private String name = "";
	private String ip = "";
	private int nameMaxLength;
	private int ipMaxLength;
	private int skin;
	private boolean canPlay;
	private ArrayList<MenuButton> buttons;
	private int messageCounter;
	
	
	public Menu(){
		skin = -1;
		nameMaxLength = 20;
		ipMaxLength = 15;
		// Initialize menu buttons
		buttons = new ArrayList<MenuButton>();
		buttons.add(new MenuButton(300, 300, 250, 50, Color.WHITE)); // text box for player name
		buttons.add(new MenuButton(700, 300, 250, 50, Color.WHITE)); // text box for ip address
		
		buttons.add(new MenuButton(200, 450, 50, 50, new Color(200, 200, 10)));
		buttons.add(new MenuButton(300, 450, 50, 50, new Color(245, 240, 115)));
		buttons.add(new MenuButton(400, 450, 50, 50, new Color(125, 245, 190)));
		buttons.add(new MenuButton(500, 450, 50, 50, new Color(0, 165, 190)));
		buttons.add(new MenuButton(600, 450, 50, 50, new Color(245, 15, 140)));
		buttons.add(new MenuButton(700, 450, 50, 50, new Color(210, 135, 135)));
		buttons.add(new MenuButton(800, 450, 50, 50, new Color(210, 205, 135)));
		buttons.add(new MenuButton(900, 450, 50, 50, new Color(190, 155, 220)));
		buttons.add(new MenuButton(1000, 450, 50, 50, new Color(110, 195, 250)));
		
		buttons.add(new MenuButton(500, 600, 250, 50, Color.RED));
	}
	
	public void update(){
		// Make sure name, ip, and skin is picked out before allowing play
		int numPeriods = 0;
		for(int i = 0; i < ip.length(); i++){
			if(ip.charAt(i) == '.'){
				numPeriods++;
			}
		}
		if(name != "" && numPeriods == 3 && skin != -1){
			canPlay = true;
		}
		else{
			canPlay = false;
		}
		
		if(canPlay){
			buttons.get(buttons.size() - 1).setColor(Color.GREEN);
		}
		else{
			buttons.get(buttons.size() - 1).setColor(Color.RED);
		}
		
		// Check which color is selected (excluding first two and last button)
		for(int i = 2; i < buttons.size() - 1; i++){
			if(buttons.get(i).isSelected()){
				skin = i - 1;
			}
		}
		
		messageCounter++;
		if(messageCounter == 400){
			messageCounter = 0;
		}
	}
	
	public void resetName(){
		name = "";
	}
	
	public void setName(String s){
		name = s;
	}
	
	public String getName(){
		return name;
	}
	
	public void resetIP(){
		ip = "";
	}
	
	public void setIP(String s){
		ip = s;
	}
	
	public String getIP(){
		return ip;
	}
	
	public int getNameMaxLength(){
		return nameMaxLength;
	}
	
	public int getIPMaxLength(){
		return ipMaxLength;
	}
	
	public void setSkin(int i){
		skin = i;
	}
	
	public int getSkin(){
		return skin;
	}
	
	public boolean canPlay(){
		return canPlay;
	}
	
	public ArrayList<MenuButton> getMenuButtons(){
		return buttons;
	}
	
	public void setMessageCounter(int mc){
		messageCounter = mc;
	}
	
	public int getMessageCounter(){
		return messageCounter;
	}
}
