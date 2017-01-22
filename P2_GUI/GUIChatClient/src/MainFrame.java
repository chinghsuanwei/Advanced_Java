import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class MainFrame extends JFrame implements ActionListener{
	 public enum WIDGETTYPE { 
	    NONE, RECTANGLE, CIRCLE, JUGGLER, TIMER
	 }
	 
	JButton rectangleButton;
	JButton circleButton;
	JButton jugglerButton;
	JButton timerButton;
	WIDGETTYPE eWidgetType;
	JScrollPane canvasScrollPane;
	CommandDispatcher cmdDispatcher;
	JTextField inputCommandTextField;
	JPanel canvasPanel;
	String args[];
	JTextArea consoleTextArea;
	
	MainFrame( String sTitle, String args[] ){
		super( sTitle );
		this.eWidgetType = WIDGETTYPE.NONE;
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
	    		  Widget widget = null;
	    		  if ( eWidgetType == WIDGETTYPE.RECTANGLE ) {
						System.out.println("==MouseClicked RectangleWidget==");
						widget = new RectangleWidget();
	    		  } else if( eWidgetType == WIDGETTYPE.CIRCLE ) {
						System.out.println("==MouseClicked CircleWidget==");
						widget= new CircleWidget();
	    		  } else if( eWidgetType == WIDGETTYPE.JUGGLER ) {
						System.out.println("==MouseClicked JugglerWidget==");
						widget = new JugglerWidget();
	    		  } else if( eWidgetType == WIDGETTYPE.TIMER ){
						System.out.println("==MouseClicked TimerWidget==");
						widget = new TimerWidget();	    			  
	    		  } else return;
	    		  
	    		  widget.setBounds( e.getX(), e.getY(), widget.getWidth(), widget.getHeight() );
	    		  cmdDispatcher.postWidget( widget, e.getX(), e.getY() );
	    		  
	    		  //release widget
	    	  }
	    });
	    
	    JPanel widgetPanel = new JPanel();
	    widgetPanel.setPreferredSize( new Dimension(200, 300) );
	    widgetPanel.setLayout( new GridLayout(4 , 0) );
	    widgetPanel.setBounds(canvasScrollPaneSize.width + graphicPanelInsets.left, graphicPanelInsets.top, 200, 300);
	    
	    rectangleButton = new JButton("Rectangle");
	    circleButton = new JButton("Circle");
	    jugglerButton = new JButton("Juggler");
	    timerButton = new JButton("Timer");
	    rectangleButton.addActionListener(this);
	    circleButton.addActionListener(this);
	    jugglerButton.addActionListener(this);
	    timerButton.addActionListener(this);
	    widgetPanel.add(rectangleButton);
	    widgetPanel.add(circleButton);
	    widgetPanel.add(jugglerButton);
	    widgetPanel.add(timerButton);
	    
	    graphicPanel.setLayout( null );
	    graphicPanel.add(canvasScrollPane);
	    graphicPanel.add(widgetPanel);
	    
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
        
		PostBoard postBoard = new PostBoard( canvasPanel );
		cmdDispatcher = new CommandDispatcher(postBoard, consoleTextArea, args);
		postBoard.setCommandDispatcher( cmdDispatcher );
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if( !cmdDispatcher.isOnline() ) return;
		if ( e.getSource() == rectangleButton ){
			System.out.println("==ActionPerformed RectangleWidget==");
			eWidgetType = WIDGETTYPE.RECTANGLE;
		} else if( e.getSource() == circleButton ){
			System.out.println("==ActionPerformed  CircleWidget==");
			eWidgetType = WIDGETTYPE.CIRCLE;
		} else if( e.getSource() == jugglerButton ){
			System.out.println("==ActionPerformed JugglerWidget==");
			eWidgetType = WIDGETTYPE.JUGGLER;
		} else if( e.getSource() == timerButton ) {
			System.out.println("==ActionPerformed TimerWidget==");
			eWidgetType = WIDGETTYPE.TIMER;
		}
	} // actionPerformed
}

class WidgetMouseAdapter implements MouseMotionListener, MouseListener{
	Widget widget;
	Point lastPoint;
	JPanel canvasPanel;
	CommandDispatcher cmdDispatcher;
	public int iWidgetId;
	
	WidgetMouseAdapter(Widget widget, int iPostId, JPanel canvasPanel, CommandDispatcher cmdDispatcher){
		this.widget = widget;
		this.iWidgetId = iPostId;
		this.canvasPanel = canvasPanel;
		this.cmdDispatcher = cmdDispatcher;
	}
	
	@Override
	public void mouseDragged(MouseEvent e){
		Point offsetPoint = new Point();
		offsetPoint.x = e.getX() - lastPoint.x;
		offsetPoint.y = e.getY() - lastPoint.y;
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
		
		/* lastPoint 為最後滑鼠座標的位置(相對於 左上角Corner的位置) */
		lastPoint.x = e.getX() - offsetPoint.x;
		lastPoint.y = e.getY() - offsetPoint.y;
	}

	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		lastPoint = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		cmdDispatcher.sendWidgetLocation( iWidgetId, widget.getX(), widget.getY());
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}