import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPanel;

public class PostBoard{
	PostBoard( JPanel canvasPanel ){
		m_vPosts = new Vector<Post>();
		m_mWidgets = new HashMap< Integer, Widget >();
		this.canvasPanel = canvasPanel;
	}
	
	public void setMessenger( Messenger messenger ){
		this.messenger = messenger;
	}
	
	public Vector<Post> getPosts(){
		return m_vPosts;
	}
	
	public void setCommandDispatcher( CommandDispatcher cmdDispatcher ){
		this.cmdDispatcher = cmdDispatcher;
	}
	
	public void add( Post post ) {
		Widget widget;
		if( post.m_sContentType.equals("RectangleWidget") ){
			widget = new RectangleWidget();
		} else if( post.m_sContentType.equals("CircleWidget") ){
			widget = new CircleWidget();
		} else if( post.m_sContentType.equals("JugglerWidget") ){
			widget = new JugglerWidget();		
		} else if ( post.m_sContentType.equals("TimerWidget") ){
			widget = new TimerWidget();	
		} else {
			m_vPosts.add( post );
			return;
		}
		
		String sTokens[] = post.m_sContent.split("\\s+", 3);
		int x;
		int y;
		x = Integer.valueOf( sTokens[0] );
		y = Integer.valueOf( sTokens[1] );
		try{
			widget.parseCommand( sTokens[2] );
		} catch( ArrayIndexOutOfBoundsException e ){
			//Do nothing, just use default widget attribute
		}

		canvasPanel.add( widget );
		widget.setBounds( x, y, widget.getWidth(), widget.getHeight() );
		
		Dimension size = canvasPanel.getPreferredSize();
		if( x + widget.getWidth() > canvasPanel.getWidth() ) size.width = x + widget.getWidth();
		if( y + widget.getHeight() > canvasPanel.getHeight() ) size.height = y + widget.getHeight();
		canvasPanel.setPreferredSize( size );
		canvasPanel.getParent().revalidate();
		
		m_vPosts.add( post );
		m_mWidgets.put( new Integer( post.m_iPostId ), widget );
		if( messenger.getName().equals( post.m_sUserName ) ){
			WidgetMouseAdapter widgetMouseAdapter = new WidgetMouseAdapter( widget, post.m_iPostId, canvasPanel, cmdDispatcher );
			widget.addMouseListener( widgetMouseAdapter );
			widget.addMouseMotionListener( widgetMouseAdapter );			
		}

		
		if( post.m_sContentType.equals("JugglerWidget") ) ((JugglerWidget)widget).start();
		else widget.paint( widget.getGraphics() );
	}
	
	public void setWidgetXY( int iWidgetId, int x, int y){
		Post post = search( iWidgetId );
		String sTokens[] = post.m_sContent.split("\\s+", 3);
		String sNewContent;
		if( sTokens.length == 3 ) sNewContent = x + " " + y + " " + sTokens[2];
		else sNewContent = x + " " + y;
		post.setContent( sNewContent );
		
		Widget widget = m_mWidgets.get( new Integer( iWidgetId ) );
		
		Dimension size = canvasPanel.getPreferredSize();
		if( x + widget.getWidth() > canvasPanel.getWidth() ) size.width = x + widget.getWidth();
		if( y + widget.getHeight() > canvasPanel.getHeight() ) size.height = y + widget.getHeight();
		canvasPanel.setPreferredSize( size );
		
		widget.setLocation(x, y);
		canvasPanel.repaint();
		canvasPanel.getParent().revalidate();
	}
	
	public void remove( Post post ){
		if( isWidgetType( post.m_sContentType ) ){
			Widget widget  = m_mWidgets.remove( new Integer( post.m_iPostId ) );
			widget.destroy();
			canvasPanel.remove( widget );
			canvasPanel.repaint();
		}
		m_vPosts.remove(post);
	}
	
	private boolean isWidgetType( String sWidgetType ){
		if( sWidgetType.equals("RectangleWidget") ||
			sWidgetType.equals("CircleWidget") ||
			sWidgetType.equals("JugglerWidget") || 
			sWidgetType.equals("TimerWidget") ) return true;
		else return false;
	}
	
	public void clear(){
		m_vPosts.clear();
		m_mWidgets.clear();
		canvasPanel.removeAll();
		canvasPanel.repaint();
	}
	
	
	public Post search( int iPostId ){
		for( int i=0; i<m_vPosts.size(); i++ ){
			if( m_vPosts.get(i).m_iPostId == iPostId ) return m_vPosts.get(i);
		}
		return null;
	}
	
	private Messenger messenger;
	private Map< Integer, Widget > m_mWidgets;
	private Vector<Post> m_vPosts;
	private CommandDispatcher cmdDispatcher;
	JPanel canvasPanel;
	
}
