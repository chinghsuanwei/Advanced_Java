import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;


public class Console implements Runnable{
	Thread consoleThread;
	
	Console(PostBoard postBoard, String[] args){
		this.postBoard = postBoard;
		this.args = args;
		m_stdin = new BufferedReader(new InputStreamReader(System.in));	
	}
	
	public void start(){
		consoleThread = new Thread( this, "Console Thread");
		consoleThread.start();
	}
	
	@Override
	public void run(){
		
		
		String sIp = args[0];
		int iPort = Integer.valueOf( args[1] );
		String sUserName = inputUserName();
		if( m_bOnline = initialSocket( sIp, iPort) ){
			initialInputOutput();
			sendUserName( sUserName );
	        messenger = new Messenger(this, postBoard, m_clientSocket, m_out, m_in);
	        messenger.start();
		}
		
	    while( true ){
		    String sInput;
		    String sCmd;
		    String sTokens[];
		    try {
				sInput = m_stdin.readLine();
				sTokens = sInput.split("\\s");
				sCmd = sTokens[0];
				//離線狀態
				if( !m_bOnline ){
					if( sCmd.equals("/connect") ) connect( sTokens ); 
					else if( sCmd.equals("/exit") ) System.exit(0);
				} else {
					if( sCmd.equals("/leave") ){
						// leave() 之後, 會將 m_bOnline 設成 false
						leave();
					} else if( sCmd.equals("/connect") ) connect( sTokens ); 
					else if( sCmd.equals("/showPost") ) showPost();
					else if( sCmd.equals("/exit") ){
						leave();
						System.exit(0);
					}
					else send( sInput );					
				}


			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			
			System.out.println("**** The server does not exist.  Please type different domain and/or port.");
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
	
	private String inputUserName(){
		System.out.print("Username: ");
		try {
			return m_stdin.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private void sendUserName( String sUserName ){
        m_out.println( sUserName );
        m_out.flush();		
	}
	
	private Socket tryConnect( String sIp, int iPort ) {
		InetSocketAddress isa = new InetSocketAddress( sIp, iPort );
		Socket newSocket = new Socket();
		try {
			newSocket.connect(isa, 10000);
			return newSocket;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("**** The server does not exist.  Please type different domain and/or port.");
			return null;
		}
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
			System.out.println( post.m_sUserName + " posted message '" + post.m_iPostId + "' in " 
								+ post.m_sContentType + ": " + post.m_sContent);
		}
	}
	
	private void leave(){
		send("/leave");
		m_bOnline = false;
		messenger.stop();
		messenger.join();
		messenger = null;
	}
	
	private void connect( String sTokens[] ){
		/*
		 * "/connect": The client can type "/connect anotherserver.nctu.edu.tw 7000" to connect to another server.
		 */
		
		String sIp = sTokens[1];
		int iPort = Integer.valueOf(sTokens[2]);
		
		Socket newSocket;
		//如果tryConnect失敗了, 表示連線沒有成功
		if( (newSocket = tryConnect( sIp, iPort )) == null ) return;
		
		//如果目前正在連線的話, 先結束先前的連線
		if( m_clientSocket.isClosed() == false ) leave();
		
		m_clientSocket = newSocket;
		initialInputOutput();
		String sUserName = inputUserName();
		sendUserName( sUserName );
		m_bOnline = true;
		messenger = new Messenger(this, postBoard, m_clientSocket, m_out, m_in);
		messenger.start();
	}
	
	private void send( String sMsg ){
		m_out.println( sMsg );
		m_out.flush();
		messenger.writeOutputLog( sMsg );

	}
	
	public void setOffline(){
		m_bOnline = false;
	}
	
	Boolean m_bOnline;
	Socket m_clientSocket;
	String args[];
	PostBoard postBoard;
	BufferedReader m_in;
	PrintWriter m_out;
	BufferedReader m_stdin;
	Messenger messenger;
}
