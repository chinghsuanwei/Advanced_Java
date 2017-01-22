import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import widgets.Widget;

public class CommandDispatcher{
	enum CONNECTIONSTATE{
		START,
		NONAME,
		ONLINE,
		OFFLINE
	}
	
	JTextArea consoleTextArea;
	
	CommandDispatcher(PostBoard postBoard, JTextArea consoleTextArea, String[] args){
		this.postBoard = postBoard;
		this.args = args;
		this.consoleTextArea = consoleTextArea;
		this.m_eConnectionState = CONNECTIONSTATE.START;
		printOnConsole("Username: ");
	}
	
	public void buildConnection( String sUserName ){
		String sIp = "127.0.0.1";
		int iPort = Integer.valueOf( "5566" );
		if( initialSocket( sIp, iPort) ){
			m_eConnectionState = CONNECTIONSTATE.NONAME;
			initialInputOutput();
			sendUserName( sUserName );
	        messenger = new Messenger(this, postBoard, m_clientSocket, m_out, m_in);
	        messenger.start();
		} else m_eConnectionState = CONNECTIONSTATE.OFFLINE;
	}
	
	public void dispatch( String sInput ) {
		
    	String sCmd;
    	String sTokens[];
		sTokens = sInput.split("\\s");
		sCmd = sTokens[0];
		if( m_eConnectionState == CONNECTIONSTATE.START ){
			buildConnection( sInput );
		} else if( m_eConnectionState == CONNECTIONSTATE.NONAME ){
			sendUserName( sInput );
		} else if( m_eConnectionState == CONNECTIONSTATE.OFFLINE ){
			if( sCmd.equals("/connect") ) connect( sTokens ); 
			else if( sCmd.equals("/exit") ) System.exit(0);
		} else if( m_eConnectionState == CONNECTIONSTATE.ONLINE ){
			if( sCmd.equals("/leave") ){
				// leave() 之後, 會將 m_bOnline 設成 false
				leave();
			} else if( sCmd.equals("/post") ) post( sInput, sTokens );
			else if( sCmd.equals("/connect") ) connect( sTokens ); 
			else if( sCmd.equals("/showPost") ) showPost();
			else if( sCmd.equals("/exit") ){
				leave();
				System.exit(0);
			}
			else send( sInput );					
		}
	}
	
	/*
	 * @param sIp IP address, Ex: 127.0.0.1
	 * @param iPort 
	 * @param bExit if connection fail, then exit or not.
	 * @author miso
	 */

	private boolean initialSocket( String sIp, int iPort ){
		
		try{
			InetSocketAddress isa = new InetSocketAddress( sIp, iPort );
			m_clientSocket = new Socket();
			m_clientSocket.connect(isa, 10000);
			return true;
		} catch (SocketException e) {
			
			printlnOnConsole("**** The server does not exist.  Please type different domain and/or port.");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void initialInputOutput(){
        try {
			m_out = new PrintWriter(m_clientSocket.getOutputStream());
			m_in = new BufferedReader( new InputStreamReader( m_clientSocket.getInputStream() ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Socket tryConnect( String sIp, int iPort ) {
		InetSocketAddress isa = new InetSocketAddress( sIp, iPort );
		Socket newSocket = new Socket();
		try {
			newSocket.connect(isa, 10000);
			return newSocket;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			printlnOnConsole("**** The server does not exist.  Please type different domain and/or port.");
			return null;
		}
	}
	
	private void post( String sInput, String[] sTokens ) {
		// TODO Auto-generated method stub
		
		if( sTokens.length >= 2 && sTokens[1].endsWith("Widget") ){
			try{
				Class widgetClass = Class.forName( "widgets." + sTokens[1] );
				int x = Integer.valueOf( sTokens[2] );
				int y = Integer.valueOf( sTokens[3] );
				
				// 將預設值加到尾巴
				if( sTokens.length < 5 ){
					
					try {
						Constructor constructor = widgetClass.getConstructor();
						Widget widget = (Widget) constructor.newInstance();
						send( sInput + " " + widget.toCommand() );
						
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
					
				} else send( sInput );
				
			} catch ( ClassNotFoundException e ){
				printlnOnConsole("Error: " + sTokens[1] + " is not found for ‘" + sInput + "‘");
			} catch ( ArrayIndexOutOfBoundsException e ){
				printlnOnConsole("Error: ArrayIndexOutOfBoundsException.");
			} catch ( NumberFormatException e ){
				printlnOnConsole("Error: NumberFormatException.");
			}
					
		} else send( sInput );

	}
	
	private void showPost(){
		Vector<Post> vPosts = postBoard.getPosts();
		/*
		 * "/showPost": List all post messages that stored in client.
        /showPost	<--input chat message here.
        Student1 posted message '101' in String: This is a test.	<--Get these messages from client, and show on console
				(Don't write into logfile.)
		 */
		for( int i=0; i<vPosts.size(); i++){
			Post post = vPosts.get(i);
			printlnOnConsole( post.m_sUserName + " posted message '" + post.m_iPostId + "' in " 
								+ post.m_sContentType + ": " + post.m_sContent);
		}
	}
	
	private void leave(){
		send("/leave");
		m_eConnectionState = CONNECTIONSTATE.OFFLINE;
		// messenger.stop() 會執行clientSocket.closed() 迫使 readline()發生exception 結束Loop迴圈
		messenger.stop();
		messenger.join();
		messenger = null;
	}
	
	private void connect( String sTokens[] ){
		/*
		 * "/connect": The client can type "/connect anotherserver.nctu.edu.tw 7000" to connect to another server.
		 */
		
		String sIp;
		int iPort;
		try{
			sIp = sTokens[1];
			iPort = Integer.valueOf(sTokens[2]);
		} catch( ArrayIndexOutOfBoundsException e ){
			
			printlnOnConsole("Error: ArrayIndexOutOfBoundsException. The Format should be \" /connect <IP> <Port> \"");
			return;
		} catch( NumberFormatException e ){
			printlnOnConsole("Error: NumberFormatException. The Format should be \" /connect <IP> <Port> \"");
			return;
		}
		
		Socket newSocket;
		//如果tryConnect失敗了, 表示連線沒有成功
		if( (newSocket = tryConnect( sIp, iPort )) == null ) return;
		
		
		//如果目前正在連線的話, 先結束先前的連線
		if( m_clientSocket.isClosed() == false && m_clientSocket.isConnected() == true ) leave();
		
		m_clientSocket = newSocket;
		initialInputOutput();
		m_eConnectionState = CONNECTIONSTATE.NONAME;
		printOnConsole("Username: ");
		messenger = new Messenger(this, postBoard, m_clientSocket, m_out, m_in);
		messenger.start();
		
	}
	
	public void printlnOnConsole( String sOutput ){
		printOnConsole( sOutput + "\n" );
	}
	
	public void printOnConsole( String sOutput ){
		consoleTextArea.append( sOutput );
		consoleTextArea.scrollRectToVisible(new Rectangle(0, consoleTextArea.getHeight()-2, 1, 1));
	}
	
	private void sendUserName( String sUserName ){
		m_out.println( sUserName );
		m_out.flush();		
	}
	
	private void send( String sMsg ){
		m_out.println( sMsg );
		m_out.flush();
		messenger.writeOutputLog( sMsg );
	}
	
	public void postWidget( Widget widget ){
		send( "/post " + widget.getClass().getSimpleName() + " " + widget.getX() + " " + widget.getY() + " " + widget.toCommand() );
	}
	
	public void moveWidget( int iWidgetId, int x, int y ){
		send( "/move " + iWidgetId +  " " + x + " " + y);
	}
	
	public void changeWidget( int iWidgetId, Widget widget ){
		send( "/change " + iWidgetId + " " + widget.toCommand() );
	}
	
	public void setOnline(){
		m_eConnectionState = CONNECTIONSTATE.ONLINE;
	}
	
	public void setOffline(){
		m_eConnectionState = CONNECTIONSTATE.OFFLINE;
	}
	
	public boolean isOnline(){
		return m_eConnectionState == CONNECTIONSTATE.ONLINE;
	}
	
	CONNECTIONSTATE m_eConnectionState;
	Socket m_clientSocket;
	String args[];
	PostBoard postBoard;
	BufferedReader m_in;
	PrintWriter m_out;
	BufferedReader m_stdin;
	Messenger messenger;
}
