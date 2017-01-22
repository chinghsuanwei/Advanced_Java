package client;
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
	PostBoard( MainFrame mainFrame )
	{
		m_mPosts = new HashMap<Integer, Post>();
		m_mWidgets = new HashMap<Integer, Widget>();
		this.mainFrame = mainFrame;
		this.canvasPanel = mainFrame.getCanvasPanel();
	}
	
	public void setMessenger( Messenger messenger )
	{
		this.messenger = messenger;
	}
	
	public Map<Integer,Post> getPosts()
	{
		return m_mPosts;
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
				cmdDispatcher.printlnOnConsole("Error: " + post.m_sContentType + " is not found for ��/post " +
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
			
			//��Button�W��Text�A�h�M�䦹WidgetButton�O�_�w�s�b
			for( Component cp : components ){
				JButton button = (JButton)cp;
				if ( button.getText().equals( post.m_sContentType ) ){
					bHasButton = true;
					break;
				}
			}
			
			//�p�GButton���s�b�A�h�s�W�@��WidgetButton
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
			
			// Widget���T���]�n�[�JPost��
			m_mPosts.put( new Integer(post.m_iPostId), post );
			
			// �� PostId ��Mapping
			m_mWidgets.put( new Integer( post.m_iPostId ), widget );
			
			//�p�G�O��Po �h�[�W MouseListener
			if( messenger.getUsername().equals( post.m_sUsername ) ){
				WidgetMouseAdapter widgetMouseAdapter = new WidgetMouseAdapter( widget, post.m_iPostId, canvasPanel, cmdDispatcher );
				widget.addMouseListener( widgetMouseAdapter );
				widget.addMouseMotionListener( widgetMouseAdapter );			
			}
			
			widget.paint( widget.getGraphics() );
			
			canvasPanel.revalidate();
		} else {
			m_mPosts.put( new Integer(post.m_iPostId), post );
		}
		
	}
	
	public void moveWidget( int iWidgetId, int x, int y){
		Post post = get( iWidgetId );
		String sTokens[] = post.m_sContent.split("\\s+", 3);
		String sNewContent = x + " " + y + " " + sTokens[2];
		post.setContent( sNewContent );
		
		Widget widget = m_mWidgets.get( new Integer( iWidgetId ) );
		//setLocation �n�b expandCanvasPanel���W�A�]���p��O�_�W�X���
		//�ݭn�̷s��x, y��T
		widget.setLocation(x, y);
		
		//�P�_�O�_�n��jcanvasPanel
		expandCanvasPanel( widget );
		
		canvasPanel.revalidate();
	}
	
	public void changeWidget( int iWidgetId, String sProperty ){
		Post post = get( iWidgetId );
		String sTokens[] = post.m_sContent.split("\\s+", 3);
		String sNewContent = sTokens[0] + " " + sTokens[1] + " " + sProperty;
		post.setContent( sNewContent );
		
		Widget widget = m_mWidgets.get( new Integer( iWidgetId ) );
		widget.parseCommand( sProperty );
		
		//�P�_�O�_�n��jcanvasPanel
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
			//destory �i�N widget����thread����
			widget.destroy();
			canvasPanel.remove( widget );
			canvasPanel.repaint();
		}
		m_mPosts.remove( new Integer(post.m_iPostId) );
	}
	
	private boolean isWidgetType( String sWidgetType ){
		return sWidgetType.endsWith("Widget");
	}
	
	public void clear(){
		m_mPosts.clear();
		m_mWidgets.clear();
		//����destory�~��NWidget��Thread����
		for( Component cp : canvasPanel.getComponents() ){
			Widget widget = (Widget)cp;
			widget.destroy();
		}
		canvasPanel.removeAll();
		canvasPanel.repaint();
	}
	
	
	public Post get( int iPostId ){
		return m_mPosts.get( new Integer(iPostId) );
	}
	
	private Messenger messenger;
	private Map< Integer, Widget > m_mWidgets;
	private Map< Integer, Post> m_mPosts;
	private CommandDispatcher cmdDispatcher;
	private MainFrame mainFrame;
	private JPanel canvasPanel;
}
