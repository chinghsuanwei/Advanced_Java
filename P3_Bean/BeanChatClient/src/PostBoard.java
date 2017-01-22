import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import widgets.*;

public class PostBoard{
	PostBoard( MainFrame mainFrame ){
		m_vPosts = new Vector<Post>();
		m_mWidgets = new HashMap< Integer, Widget >();
		this.mainFrame = mainFrame;
		this.canvasPanel = mainFrame.getCanvasPanel();
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
	
	public void add( Post post ){
		
		if( post.m_sContentType.endsWith("Widget") ){
			Class widgetClass = null;
			Constructor constructor;
			Widget widget = null;
			try {
				widgetClass = Class.forName("widgets." + post.m_sContentType );
				constructor = widgetClass.getConstructor();
				widget = (Widget) constructor.newInstance();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				cmdDispatcher.printlnOnConsole("Error: " + post.m_sContentType + " is not found for ‘/post " +
												post.m_sContentType + " " + post.m_sContent);
				
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			JPanel widgetPanel = mainFrame.getWidgetPanel();
			Component[] components = widgetPanel.getComponents();
			boolean bHasButton = false;
			
			for( Component cp : components ){
				JButton button = (JButton)cp;
				if ( button.getText().equals( post.m_sContentType ) ){
					bHasButton = true;
					break;
				}
			}
			
			if( !bHasButton ){
				GridLayout gridLayout = mainFrame.getWidgetPanelGridLayout();
				gridLayout.setRows( gridLayout.getRows() + 1 );
				
				JButton widgetButton = new JButton( post.m_sContentType );
				widgetButton.addActionListener( mainFrame );
				widgetPanel.add( widgetButton );
				
				Dimension dimension = widgetPanel.getSize();
				dimension.height += 100;
				widgetPanel.setPreferredSize( dimension );
				
				widgetPanel.revalidate();
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
			
			expandCanvasPanel( widget );
			
			// Widget的訊息也要加入Post中
			m_vPosts.add( post );
			
			// 用 PostId 做Mapping
			m_mWidgets.put( new Integer( post.m_iPostId ), widget );
			
			//如果是原Po 則加上 MouseListener
			if( messenger.getName().equals( post.m_sUserName ) ){
				WidgetMouseAdapter widgetMouseAdapter = new WidgetMouseAdapter( widget, post.m_iPostId, canvasPanel, cmdDispatcher );
				widget.addMouseListener( widgetMouseAdapter );
				widget.addMouseMotionListener( widgetMouseAdapter );			
			}
			
			widget.paint( widget.getGraphics() );
			
			canvasPanel.revalidate();
		} else {
			m_vPosts.add( post );
		}
		
	}
	
	public void setWidgetXY( int iWidgetId, int x, int y){
		Post post = search( iWidgetId );
		String sTokens[] = post.m_sContent.split("\\s+", 3);
		String sNewContent;
		if( sTokens.length == 3 ) sNewContent = x + " " + y + " " + sTokens[2];
		else sNewContent = x + " " + y;
		post.setContent( sNewContent );
		
		Widget widget = m_mWidgets.get( new Integer( iWidgetId ) );
		//setLocation 要在 expandCanvasPanel之上，因為計算是否超出邊界
		//需要最新的x, y資訊
		widget.setLocation(x, y);
		
		//判斷是否要放大canvasPanel
		expandCanvasPanel( widget );
		
		canvasPanel.revalidate();
	}
	
	public void changeWidgetProperty( int iWidgetId, String sProperty ){
		Post post = search( iWidgetId );
		String sTokens[] = post.m_sContent.split("\\s+", 3);
		String sNewContent = sTokens[0] + " " + sTokens[1] + " " + sProperty;
		post.setContent( sNewContent );
		
		Widget widget = m_mWidgets.get( new Integer( iWidgetId ) );
		widget.parseCommand( sProperty );
		
		//判斷是否要放大canvasPanel
		expandCanvasPanel( widget );
		
		canvasPanel.revalidate();		
	}
	
	public void expandCanvasPanel( Widget widget )
	{
		Dimension size = canvasPanel.getPreferredSize();
		if( widget.getX() + widget.getWidth() > canvasPanel.getWidth() ) size.width = widget.getX() + widget.getWidth();
		if( widget.getY() + widget.getHeight() > canvasPanel.getHeight() ) size.height = widget.getY() + widget.getHeight();
		canvasPanel.setPreferredSize( size );
	}
	
	public void remove( Post post ){
		if( isWidgetType( post.m_sContentType ) ){
			Widget widget  = m_mWidgets.remove( new Integer( post.m_iPostId ) );
			//destory 可將 widget內的thread中止
			widget.destroy();
			canvasPanel.remove( widget );
			canvasPanel.repaint();
		}
		m_vPosts.remove(post);
	}
	
	private boolean isWidgetType( String sWidgetType ){
		return sWidgetType.endsWith("Widget");
	}
	
	public void clear(){
		m_vPosts.clear();
		m_mWidgets.clear();
		//執行destory才能將Widget的Thread停止
		for( Component cp : canvasPanel.getComponents() ){
			Widget widget = (Widget)cp;
			widget.destroy();
		}
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
	private MainFrame mainFrame;
	private JPanel canvasPanel;
}
