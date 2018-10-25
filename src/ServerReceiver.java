import java.io.BufferedReader;
import java.io.IOException;

public class ServerReceiver implements Runnable{

	private Thread thread;
	private boolean newInfo;
	private String info;
	
	private BufferedReader reader;
	
	public ServerReceiver(BufferedReader reader){
		this.reader = reader;
		thread = new Thread(this);
		thread.start();
	}
	
	public boolean hasNewInfo(){
		return newInfo;
	}
	
	public String getInfo(){
		newInfo = false;
		return info;
	}
	
	@Override
	public void run() {
		
		try {
			while ((info = reader.readLine()) != null)
			  {
				info = reader.readLine();
				newInfo = true;
			  }
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
