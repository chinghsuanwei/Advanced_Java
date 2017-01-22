import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimerWidget extends Widget implements Runnable {

	private static final long serialVersionUID = 1L;
	private Thread thread = null ;
	private boolean wbRunning = true;
	private Color wbFore = Color.red;
    private Color wbBack = Color.cyan;
	
	public TimerWidget () {
		
		setwbBack(wbBack);
		setwbFore(wbFore);
		
		setVisible(true) ;
	}
	
    public synchronized void setEnabled(boolean x)
    {
        super.setEnabled(x);
        notify();
    }
	
	@Override
	public void run() {
		do {
			try {
				synchronized(this)
			    {
			        for(; !wbRunning || !isEnabled(); wait());
			    } 
				// paint ( getGraphics() ) ;
				Graphics g = getGraphics();
				
				paint(g);
				
				Thread.sleep (100) ;
				
			} catch ( InterruptedException e ) {
				return ;
			}
		} while ( true ) ;
	}

	public void paint ( Graphics g ) {
		String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		try {
			g.setFont(new Font("Monospaced", 1, 18));
			// g.clearRect(0, 0, this.getWidth(), this.getHeight());
			g.setColor(wbBack) ;
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			// g.fillR
			g.setColor(wbFore);
			g.drawString( currentTime, 0, 15);
			
			FontMetrics fm = g.getFontMetrics();
	        setSize(fm.stringWidth(currentTime), 20);
		} catch ( Exception e ) {
			e.printStackTrace();
			System.err.println("g="+g.toString());
			return;
		}
	}
	
	@Override
	public void parseCommand(String cmd) {
		String[] tokens = cmd.split("( )+", 3) ;
		
		boolean running = wbRunning ;
		Color bkcolor = wbBack ;
		Color frcolor = wbFore ;
		if ( cmd.length() == 3 ) { 
			try {
				running = Boolean.parseBoolean(tokens[0]);
				frcolor = Color.decode(tokens[1]);
				bkcolor = Color.decode(tokens[2]);
			} catch ( Exception e ) {
			}
		}
		
		setwbBack(bkcolor);
		setwbFore(frcolor);
		
		setwbRunning(running) ;
	}

	@Override
	public String toCommand() {
		return String.format("%b %s %s", 
				wbRunning, getHexColor(wbFore), getHexColor(wbBack));
	}

	@Override
	public void destroy() {
		if ( thread != null ) {
			thread.interrupt();
		}
	}
	
	private void startTimer () {
		if ( thread == null ) {
			thread = new Thread(this);
			thread.start();
		}
	}
	private void stopTimer () {
		paint( getGraphics() ) ;
	}
	
	public void setwbRunning ( boolean running ) {
		wbRunning = running ;
		if ( wbRunning ) startTimer () ;
		else stopTimer () ;
	}
	public boolean getwbRunning ( ) {
		return wbRunning ;
	}
	
	public void setwbBack ( Color c ) {
		wbBack = c;
		setBackground(wbBack) ;
	}
	
	public Color getwbBack ( Color c ) {
		return wbBack;
	}
	
	public void setwbFore ( Color c ) {
		wbFore = c;
	}
	
	public Color getwbFore ( Color c ) {
		return wbFore;
	}
	
    private String getHexColor(Color cColor)
	{                        
        return String.format("#%02x%02x%02x", 
        		cColor.getRed(), cColor.getGreen(), cColor.getBlue() ) ;
	}
	
}
