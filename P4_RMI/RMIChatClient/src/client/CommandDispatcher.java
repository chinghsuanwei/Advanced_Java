package client;
import java.awt.Rectangle;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import javax.swing.JTextArea;

import widgets.Widget;
import task.*;

public class CommandDispatcher{
	enum CONNECTIONSTATE{
		START,
		NONAME,
		ONLINE,
		OFFLINE
	}
	
	JTextArea consoleTextArea;
	
	CommandDispatcher(PostBoard postBoard, JTextArea consoleTextArea, String[] args ){
		this.postBoard = postBoard;
		this.args = args;
		this.consoleTextArea = consoleTextArea;
		this.m_eConnectionState = CONNECTIONSTATE.START;
		//It's for TA
		DisplayResultForTA.cmdDispatcher = this;
		
		printOnConsole("Username: ");
	}
	
	public void buildConnection( String sUsername ){
		String sIp = null;
		int iPort = -1;
		
		try{
			sIp = args[0];
			iPort = Integer.valueOf( args[1] );
		} catch( Exception e ){
			printlnOnConsole( "Error: args is wrong. Client [IP] [port]" );
			m_eConnectionState = CONNECTIONSTATE.OFFLINE;
		}
		
		if( initialSocket( sIp, iPort) ){
			m_eConnectionState = CONNECTIONSTATE.NONAME;
			initialInputOutput();
			sendUsername( sUsername );
	        messenger = new Messenger(this, postBoard, m_clientSocket, m_out, m_in);
	        messenger.start();
		} else m_eConnectionState = CONNECTIONSTATE.OFFLINE;
	}
	
	public void initialRMI()
	{
        jobPool = new JobPool( );
        try {
			TaskExecutor taskExecutor = new TaskExecutor( );
			Compute stub = (Compute) UnicastRemoteObject.exportObject(taskExecutor, 0);
			registry = LocateRegistry.getRegistry( 2001 );
			registry.rebind( messenger.getUsername(), stub );
        } catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void dispatch( String sInput ) {
		
    	String sCmd;
    	String sTokens[];
		sTokens = parse( sInput );
		sCmd = sTokens[0];
		if( m_eConnectionState == CONNECTIONSTATE.START ){
			buildConnection( sInput );
		} else if( m_eConnectionState == CONNECTIONSTATE.NONAME ){
			sendUsername( sInput );
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
			else if( sCmd.equals("/showtask") ) showTask();
			else if( sCmd.equals("/task") ) task( sTokens );
			else if( sCmd.equals("/rexe") ) rexe( sTokens ); 
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
		
		if( sCmd.equals("/task") ) limit = 4;
		else limit = -1;
		
		sTokens = sInput.split( "\\s+", limit );
		return sTokens;
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
	
	private void showPost()
	{
		Map<Integer,Post> mPosts = postBoard.getPosts();
		Set<Entry<Integer,Post>> entries = mPosts.entrySet();
		/*
		 * "/showPost": List all post messages that stored in client.
        /showPost	<--input chat message here.
        Student1 posted message '101' in String: This is a test.	<--Get these messages from client, and show on console
				(Don't write into logfile.)
		 */
		
		for( Entry<Integer,Post> entry : entries ){
			Post post = entry.getValue();
			printlnOnConsole( post.m_sUsername + " posted message '" + post.m_iPostId + "' in " 
								+ post.m_sContentType + ": " + post.m_sContent);
		}
	}


	private void task( String sTokens[] )
	{
		/*
		 *  /task   tid    TaskType  Args
		 */
		
		try{
			
			String sJobId = sTokens[1];
			String sJobName = sTokens[2]; // ArrayIndexOutOfBoundsException
			String sArgs = sTokens[3]; // ArrayIndexOutOfBoundsException
			
			Class taskClass;
			Task task = null;
			try {
				taskClass = Class.forName("task." + sJobName );
				Constructor constructor = taskClass.getConstructor();
				Object obj = constructor.newInstance();
				task = (Task)obj;
				
			} catch (ClassCastException e) {
				printlnOnConsole( "Error: ClassCastException." );
				return;
			} catch (ClassNotFoundException e) {
				printlnOnConsole( "Error: ClassNotFoundException[ " + sJobName + " ]." );
				return;
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

			
			Job job = new Job( sJobId, sJobName, sArgs, task );
			
			if( jobPool.add( job, false ) ) printlnOnConsole("Task ID: " + job.m_sJobId + ", Task Type: " + job.m_sJobName );
			else printlnOnConsole("Error: Task Id is duplicate.");
			
		} catch ( ArrayIndexOutOfBoundsException e ){
			printlnOnConsole("Error: ArrayIndexOutOfBoundsException.");
		}
	}
	
	private void rexe( String sTokens[] )
	{
		/*
		 * /rexe   tid    [user]
		 */
		try{
			String sJobId = sTokens[1]; // ArrayIndexOutOfBoundsException
			
			String sUsername = null;
			try{
				sUsername = sTokens[2];
			} catch ( ArrayIndexOutOfBoundsException e ) { 
				//沒有執行的人是誰
			}
		
			Job job = jobPool.get( sJobId);
			if( job == null ){
				this.printlnOnConsole("Error: have no this job Id.");
				return;
			}
			

			job.m_task.init( job.m_sArgs );
			
			RexeThread rexeThread = new RexeThread( job.m_task, sUsername );
			rexeThread.start();
			
		} catch ( ArrayIndexOutOfBoundsException e ){
			printlnOnConsole("Error: ArrayIndexOutOfBoundsException.");
		}
	}
	
	private void showTask()
	{	
		/*
		E.g. 
		Type /task t1 Pi 20 
			Display on the “Chat Region” panel:
			Task ID: t1, Task Type: Pi 
		Type /task t2 GridifyStrings a b cc hello
			Display on the “Chat Region” panel:
			Task ID: t2, Task Type: GridifyStrings 
		Type /showtask 
			Display on the “Chat Region” panel:
			Task ID: t1, Task Type: Pi 
		*/
		Map<String, Job> mJobs = jobPool.getJobs();
		Set< Map.Entry<String, Job> > entries = mJobs.entrySet();
		
        for( Map.Entry<String, Job> entry : entries ) {
            String key = entry.getKey();
            Job job = entry.getValue();
            printlnOnConsole( "Task ID: " + key + ", Task Type: " + job.m_sJobName );
        }
        
	}
	
	
	private void leave(){
		send("/leave");
		try {
			registry.unbind( messenger.getUsername() );
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		registry = null;
		jobPool = null;
		
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
	
	private void sendUsername( String sUsername )
	{
		m_out.println( sUsername );
		m_out.flush();		
	}
	
	private void send( String sMsg )
	{
		m_out.println( sMsg );
		m_out.flush();
		messenger.writeOutputLog( sMsg );
	}
	
	public void sendByTA( String sMsg )
	{
		send( sMsg );
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
	String[] args;
	PostBoard postBoard;
	JobPool jobPool;
	Registry registry;
	BufferedReader m_in;
	PrintWriter m_out;
	BufferedReader m_stdin;
	Messenger messenger;

	class RexeThread extends Thread{
		Task task;
		String sUsername;
		
		RexeThread( Task task, String sUsername )
		{
			this.task = task;
			this.sUsername = sUsername;
		}
		
		public void run()
		{
			Compute compute;
			try {
				//先用RMI 將Task傳送到Server端
				compute = (Compute)(registry.lookup("@SERVER"));
				Object result = compute.executeTask( task, sUsername );
				printlnOnConsole( result.toString() );
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}