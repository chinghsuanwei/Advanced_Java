package widgets;

import java.awt.Color;
import java.awt.Graphics;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class DoubleRectangleWidget extends Widget
{
	private static final long serialVersionUID = 1L;
	private PropertyChangeSupport propertyChangeSupport;
	private Color innerBackground; // inner background color. 
	private Color outerBackground; // Outer background color.
	private int innerWidth; // Inner width. 
	private int innerHeight; // Inner height. 
	private int outerWidth; // Outer width. 
	private int outerHeight; // Outer height.
	
	public DoubleRectangleWidget()
	{
        propertyChangeSupport = new PropertyChangeSupport(this);
        outerBackground = Color.RED;
        innerBackground = Color.GREEN ;
    	innerWidth = 70; 
    	innerHeight = 70; 
    	outerWidth = 100;
    	outerHeight = 100;        
        setSize(outerWidth, outerHeight);
	}
	
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        g.setColor(outerBackground);
        g.fillRect(0, 0, outerWidth, outerHeight);
        g.setColor(innerBackground);
        g.fillRect( (outerWidth - innerWidth)/2, (outerHeight - innerHeight)/2, 
        			innerWidth, innerHeight);
        setSize(outerWidth, outerHeight);
    }
    
	@Override
	public void parseCommand(String cmd) 
	{
		//#ff00ff #00ff00 5 5 20 20
		String sTokens[] = cmd.split("( )+", 6) ;
		if ( sTokens.length != 6 ) return ;
		Color inColor, outColor = null ;
		int inWidth, inHeight;
		int outWidth, outHeight;
		try {
			inColor = Color.decode( sTokens[0] );
			outColor = Color.decode( sTokens[1] );
			inWidth = Integer.parseInt( sTokens[2] );
			inHeight = Integer.parseInt( sTokens[3] );
			outWidth = Integer.parseInt( sTokens[4] );
			outHeight = Integer.parseInt( sTokens[5] );
		} catch ( Exception e ) {
			return ;
		}
		
		setinnerBackground( inColor );
		setouterBackground( outColor );
		setinnerWidth( inWidth );
		setinnerHeight( inHeight );
		setouterWidth( outWidth );
		setouterHeight( outHeight );
	}

	@Override
	public String toCommand() 
	{
		// TODO Auto-generated method stub
		//#ff00ff #00ff00 5 5 20 20
		return String.format("%s %s %d %d %d %d", 
    			getHexColor(innerBackground), getHexColor(outerBackground), 
    			innerWidth, innerHeight, outerWidth, outerHeight) ;
	}

	@Override
	public void destroy() 
	{
		// TODO Auto-generated method stub
		
	}

	public Color getinnerBackground() 
	{
		return innerBackground;
	}

	public void setinnerBackground(Color innerBackground) 
	{
		Color oldInnerBackground = this.innerBackground;
		this.innerBackground = innerBackground;
		propertyChangeSupport.firePropertyChange("innerBackground", oldInnerBackground, innerBackground);
		repaint();
	}

	public Color getouterBackground() 
	{
		return outerBackground;
	}

	public void setouterBackground(Color outerBackground) 
	{
		Color oldOuterBackground = this.outerBackground;
		this.outerBackground = outerBackground;
		propertyChangeSupport.firePropertyChange("outerBackground", oldOuterBackground, outerBackground);
		repaint();
	}

	public int getinnerHeight() 
	{
		return innerHeight;
	}

	public void setinnerHeight(int innerHeight) 
	{
		int oldInnerHeight = this.innerHeight;
		this.innerHeight = innerHeight;
		propertyChangeSupport.firePropertyChange("innerHeight", oldInnerHeight, innerHeight);
		repaint();
	}
	
	public int getinnerWidth() 
	{
		return innerWidth;
	}

	public void setinnerWidth(int innerWidth) 
	{
		int oldInnerWidth = this.innerWidth;
		this.innerWidth = innerWidth;
		propertyChangeSupport.firePropertyChange("innerWidth", oldInnerWidth, innerWidth);
		repaint();
	}

	public int getouterWidth() 
	{
		return outerWidth;
	}

	public void setouterWidth(int outerWidth) 
	{
		int oldOuterWidth = this.outerWidth;
		this.outerWidth = outerWidth;
		propertyChangeSupport.firePropertyChange("outerWidth", oldOuterWidth, outerWidth);
		repaint();
	}

	public int getouterHeight() 
	{
		return outerHeight;
	}

	public void setouterHeight(int outerHeight) 
	{
		int oldOuterHeight = this.outerHeight;
		this.outerHeight = outerHeight;
		propertyChangeSupport.firePropertyChange("outerHeight", oldOuterHeight, outerHeight);
		repaint();
	}
	
    private String getHexColor(Color color)
	{                        
        return String.format("#%02x%02x%02x", 
        		color.getRed(), color.getGreen(), color.getBlue() ) ;
	}
	
}
