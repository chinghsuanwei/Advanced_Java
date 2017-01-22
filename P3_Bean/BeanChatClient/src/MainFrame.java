import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import widgets.*;

public class MainFrame extends JFrame implements ActionListener{
	 
	JButton rectangleButton;
	JButton circleButton;
	JButton jugglerButton;
	
	String sWidgetType;
	JScrollPane canvasScrollPane;
	CommandDispatcher cmdDispatcher;
	JTextField inputCommandTextField;
	JPanel canvasPanel;
	JPanel widgetPanel;
	GridLayout widgetPanelGridLayout;
	
	String args[];
	JTextArea consoleTextArea;
	
	MainFrame( String sTitle, String args[] ){
		super( sTitle );
		this.sWidgetType = null;
		this.args = args;
		initialize();
	}
	
	private void initialize() {
	    JFrame mainFrame = new JFrame("ChatClient");
	    Container container = mainFrame.getContentPane();
	    mainFrame.setResizable(false);
	    mainFrame.addWindowListener(new WindowAdapter(){
	    	  public void windowClosing(WindowEvent event) {
	    		    System.exit(0);
	    	  }	    	
	    	});
	    
	    JPanel graphicPanel = new JPanel();
	    graphicPanel.setPreferredSize( new Dimension(700, 300) );
	    
	    
	    canvasPanel = new JPanel();
	    canvasScrollPane = new JScrollPane(canvasPanel);
	    canvasScrollPane.setPreferredSize( new Dimension(500, 300) );
	    Insets graphicPanelInsets = graphicPanel.getInsets();
	    canvasScrollPane.setBounds(graphicPanelInsets.left, graphicPanelInsets.top, 500, 300);
	    canvasScrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ); 
	    canvasScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ); 
	    canvasPanel.setLayout(null);
	    Dimension canvasScrollPaneSize = canvasScrollPane.getPreferredSize();
	    canvasPanel.setBackground( Color.WHITE );
	    canvasPanel.setPreferredSize( new Dimension(500, 300) );
	    canvasPanel.addMouseListener( new MouseAdapter(){
	    	public void mouseClicked(MouseEvent e) {
	    		
	    		if( sWidgetType == null ) return;
	    		
	    		Widget widget = null;
	    		Class widgetClass;
	    		Constructor constructor;
				try {
					widgetClass = Class.forName("widgets." + sWidgetType );
					constructor = widgetClass.getConstructor();
					widget = (Widget)constructor.newInstance();
				} catch (ClassNotFoundException evt) {
					// TODO Auto-generated catch block
					evt.printStackTrace();
				} catch (NoSuchMethodException evt) {
					// TODO Auto-generated catch block
					evt.printStackTrace();
				} catch (SecurityException evt) {
					// TODO Auto-generated catch block
					evt.printStackTrace();
				} catch (InstantiationException evt) {
					// TODO Auto-generated catch block
					evt.printStackTrace();
				} catch (IllegalAccessException evt) {
					// TODO Auto-generated catch block
					evt.printStackTrace();
				} catch (IllegalArgumentException evt) {
					// TODO Auto-generated catch block
					evt.printStackTrace();
				} catch (InvocationTargetException evt) {
					// TODO Auto-generated catch block
					evt.printStackTrace();
				}
	    		
	    		widget.setBounds( e.getX(), e.getY(), widget.getWidth(), widget.getHeight() );
	    		
	    		new PropertyHandlerFrame( cmdDispatcher, widget, -1 ); // -1 means no widgit id.
	    		
	    		sWidgetType = null;
	    	}
	    });
	    
	   
	    widgetPanel = new JPanel();
	    JScrollPane widgetScrollPane = new JScrollPane(widgetPanel);
	    
	    widgetScrollPane.setPreferredSize( new Dimension(200, 300) );
	    widgetScrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
	    widgetScrollPane.setBounds(canvasScrollPaneSize.width + graphicPanelInsets.left, graphicPanelInsets.top, 200, 300);
	    widgetScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
	    // -3為ScrollBar的大小
	    widgetPanelGridLayout = new GridLayout(3,1);
	    widgetPanel.setPreferredSize( new Dimension(200-3, 300) );
	    widgetPanel.setLayout( widgetPanelGridLayout );
	    
	    rectangleButton = new JButton("RectangleWidget");
	    circleButton = new JButton("CircleWidget");
	    jugglerButton = new JButton("JugglerWidget");
	    
	    widgetPanel.add(rectangleButton);
	    widgetPanel.add(circleButton);
	    widgetPanel.add(jugglerButton);
	    
	    rectangleButton.addActionListener( this );
	    circleButton.addActionListener( this );
	    jugglerButton.addActionListener( this );
	    
	    graphicPanel.setLayout( null );
	    graphicPanel.add(canvasScrollPane);
	    graphicPanel.add(widgetScrollPane);
	    
	    consoleTextArea = new JTextArea(10, 5);
	    JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);
	    consoleScrollPane.setPreferredSize( new Dimension(700, 170) );
	    consoleScrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ); 
	    consoleScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ); 
	    
	    JPanel commandPanel = new JPanel( new GridLayout(0, 2) );
	    commandPanel.setPreferredSize( new Dimension(700, 30) );
	    JLabel inputCommandLabel = new JLabel("Input Command:");
	    inputCommandTextField = new JTextField(50);
	    inputCommandTextField.addKeyListener( new KeyAdapter( ){
	    	public void keyPressed(KeyEvent e) {
	    		int key = e.getKeyCode();
	            if (key == KeyEvent.VK_ENTER) {
	            	String sInput = inputCommandTextField.getText();
	            	inputCommandTextField.setText("");
	            	cmdDispatcher.dispatch( sInput );
	            }
	    	}
	    });
	    commandPanel.add(inputCommandLabel);
	    commandPanel.add(inputCommandTextField);
	    
	    container.add(BorderLayout.NORTH, graphicPanel);
	    container.add(BorderLayout.CENTER, consoleScrollPane);
	    container.add(BorderLayout.SOUTH, commandPanel);
	    
	    mainFrame.setSize(700, 500);
	    mainFrame.setLocation(300,200);
	    mainFrame.pack();
	    mainFrame.setVisible(true);
        
		PostBoard postBoard = new PostBoard( this );
		cmdDispatcher = new CommandDispatcher(postBoard, consoleTextArea, args);
		postBoard.setCommandDispatcher( cmdDispatcher );
	}
	
	public JPanel getCanvasPanel()
	{
		return canvasPanel;
	}
	
	public JPanel getWidgetPanel()
	{
		return widgetPanel;
	}
	
	public GridLayout getWidgetPanelGridLayout()
	{
		return widgetPanelGridLayout;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		if( !cmdDispatcher.isOnline() ) return;
		JButton widgetButton = (JButton)e.getSource();
		sWidgetType = widgetButton.getText();
	}
	
}

class WidgetMouseAdapter implements MouseMotionListener, MouseListener
{
	Widget widget;
	Point clickPoint;
	JPanel canvasPanel;
	CommandDispatcher cmdDispatcher;
	public int iWidgetId;
	
	WidgetMouseAdapter(Widget widget, int iPostId, JPanel canvasPanel, CommandDispatcher cmdDispatcher)
	{
		this.widget = widget;
		this.iWidgetId = iPostId;
		this.canvasPanel = canvasPanel;
		this.cmdDispatcher = cmdDispatcher;
	}
	
	@Override
	public void mouseDragged(MouseEvent e)
	{
		Point offsetPoint = new Point();
		offsetPoint.x = e.getX() - clickPoint.x;
		offsetPoint.y = e.getY() - clickPoint.y;
		Point targetPoint = new Point();
		targetPoint.x = widget.getX() + offsetPoint.x;
		targetPoint.y = widget.getY() + offsetPoint.y; 
		if( targetPoint.x < 0 ) targetPoint.x = 0;
		if( targetPoint.y < 0 ) targetPoint.y = 0; 
		widget.setLocation( targetPoint );
		
		Dimension size = new Dimension( canvasPanel.getPreferredSize() );
		if( targetPoint.x + widget.getWidth() > canvasPanel.getWidth() ) size.width = targetPoint.x + widget.getWidth();
		if( targetPoint.y + widget.getHeight() > canvasPanel.getHeight() ) size.height = targetPoint.y + widget.getHeight();
		canvasPanel.setPreferredSize( size );
		canvasPanel.revalidate();
	}

	
	@Override
	public void mouseClicked(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		if (e.getClickCount() == 2) {
			new PropertyHandlerFrame( cmdDispatcher, widget, iWidgetId );
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent e) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		clickPoint = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		cmdDispatcher.moveWidget( iWidgetId, widget.getX(), widget.getY());
	}

	@Override
	public void mouseMoved(MouseEvent e) 
	{
		// TODO Auto-generated method stub
	}
}