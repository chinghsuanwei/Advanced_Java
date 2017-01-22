package client;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import widget.Widget;

import android.app.Activity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CommandDispatcher{
	public enum CONNECTIONSTATE{
		NONAME,
		ONLINE,
		OFFLINE
	}
	
	CommandDispatcher(ChatRoomActivity chatRoomActivity, PostBoard postBoard){
		this.postBoard = postBoard;
		this.chatRoomActivity = chatRoomActivity;
		this.m_eConnectionState = CONNECTIONSTATE.NONAME;
		
		initialInputOutput();
		messenger = new Messenger(this, postBoard, m_out, m_in);
		messenger.start();
	}
	
	public void dispatch( String sInput ) {
		
    	String sCmd;
    	String sTokens[];
		sTokens = parse( sInput );
		sCmd = sTokens[0];
		if( m_eConnectionState == CONNECTIONSTATE.NONAME ){
			sendUsername( sInput );
		} else if( m_eConnectionState == CONNECTIONSTATE.OFFLINE ){
			if( sCmd.equals("/exit") ) System.exit(0);
			else if( sCmd.equals("/connect") ) connect( sTokens );
		} else if( m_eConnectionState == CONNECTIONSTATE.ONLINE ){
			if( sCmd.equals("/leave") ){
				// leave() 之後, 會將 m_bOnline 設成 false
				leave();
			} else if( sCmd.equals("/post") ) post( sInput, sTokens );
			else if( sCmd.equals("/showPost") ) showPost();
			else if( sCmd.equals("/connect") ) connect( sTokens );
			else if( sCmd.equals("/exit") ){
				leave();
				System.exit(0);
			}
			else send( sInput );					
		}
	}
	
	private String[] parse( String sInput )
	{
		String sCmd = "";
		String sTokens[];
		
		//先去掉前面的空白
		int found = sInput.trim().indexOf(" ");
		if( found > 0 ) sCmd = sInput.substring(0, found);
		/*
		 * post type msg
		 * task id type arg 
		 */
		
		int limit = -1;
		
		sTokens = sInput.split( "\\s+", limit );
		return sTokens;
	}
	
	/*
	 * @param sIp IP address, Ex: 127.0.0.1
	 * @param iPort 
	 * @param bExit if connection fail, then exit or not.
	 * @author miso
	 */
	
	private void initialInputOutput(){
        try {
			m_out = new PrintWriter(ClientActivity.m_clientSocket.getOutputStream());
			m_in = new BufferedReader( new InputStreamReader( ClientActivity.m_clientSocket.getInputStream() ) );
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
		String sPostType = sTokens[1];
		if( sPostType.endsWith("Widget") ){
			try {
				Class<?> cls = Class.forName( "widget." + sPostType );
			} catch (ClassNotFoundException e) {
				//找不到Class就彈跳Toast
				Log.i("BP", "Toast" );
				Toast.makeText(chatRoomActivity, 
						"Error: <WidgetName> is not found for '" + sInput, Toast.LENGTH_LONG ).show();
				
				return;
			}
		}
		send( sInput );
	}
	
	private void showPost()
	{
		/*
		 * "/showPost": List all post messages that stored in client.
        /showPost	<--input chat message here.
        Student1 posted message '101' in String: This is a test.	<--Get these messages from client, and show on console
				(Don't write into logfile.)
		 */
		Map<Integer, Post> mPosts = postBoard.getPosts();
		Set<Entry<Integer,Post>> entries = mPosts.entrySet();
		
        for(Map.Entry<Integer, Post> entry : entries) {
            Integer key = entry.getKey();
            Post post = entry.getValue();
            printlnOnConsole( post.m_sUsername + " posted message '" + post.m_iPostId + "' in " 
					+ post.m_sContentType + ": " + post.m_sContent);
        }
		
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
		if( (newSocket = tryConnect( sIp, iPort )) == null ){
			
			Toast.makeText( chatRoomActivity, 
					"Error: cannot connect to [" + sIp + ":" + iPort + "]", Toast.LENGTH_LONG ).show();
			return;
		}
		
		
		//如果目前正在連線的話, 先結束先前的連線
		if( ClientActivity.m_clientSocket.isClosed() == false && ClientActivity.m_clientSocket.isConnected() == true ) leave();
		
		ClientActivity.m_clientSocket = newSocket;
		ClientActivity.sIp = sIp;
		ClientActivity.iPort = iPort;
		
		m_eConnectionState = CONNECTIONSTATE.NONAME;
		initialInputOutput();
		messenger = new Messenger(this, postBoard, m_out, m_in);
		messenger.start();
		
		printOnConsole("Username: ");
	}
	
	public void leave(){
		send("/leave");
		
		m_eConnectionState = CONNECTIONSTATE.OFFLINE;
		
		// messenger.stop() 會執行clientSocket.closed() 迫使 readline()發生exception 結束Loop迴圈
		messenger.stop();
		messenger.join();
		messenger = null;
	}
	
	public void reconnect( String sUsername ){
		InetSocketAddress isa = new InetSocketAddress( ClientActivity.sIp, ClientActivity.iPort );
		ClientActivity.m_clientSocket = new Socket();
		try {
			ClientActivity.m_clientSocket.connect(isa, 10000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText( chatRoomActivity, 
					"Error: cannot connect to [" + ClientActivity.sIp + ":" + ClientActivity.iPort + "]", Toast.LENGTH_LONG ).show();
		}
		
		initialInputOutput();
		this.m_eConnectionState = CONNECTIONSTATE.NONAME;
		messenger = new Messenger(this, postBoard, m_out, m_in);
		messenger.start();
		printOnConsole("Username: ");
		sendUsername( sUsername );
	}
	
	public void printlnOnConsole( String sOutput ){
		printOnConsole( sOutput + "\n" );
	}
	
	public void printOnConsole( String sOutput ){
		chatRoomActivity.printOnConsole( sOutput );
		//consoleTextArea.append( sOutput );
		//consoleTextArea.scrollRectToVisible(new Rectangle(0, consoleTextArea.getHeight()-2, 1, 1));
	}
	
	private void sendUsername( String sUsername )
	{
		m_out.println( sUsername );
		m_out.flush();		
	}
	
	public void send( String sMsg )
	{
		m_out.println( sMsg );
		m_out.flush();
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
	PostBoard postBoard;
	BufferedReader m_in;
	PrintWriter m_out;
	BufferedReader m_stdin;
	ChatRoomActivity chatRoomActivity;
	Messenger messenger;
}