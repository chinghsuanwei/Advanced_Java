package client;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import android.util.Log;


public class Messenger implements Runnable{
	private Thread messengerThread;
	
	Messenger(CommandDispatcher cmdDispatcher, PostBoard postBoard, PrintWriter out, BufferedReader in){
		this.postBoard = postBoard;
		this.out = out;
		this.in = in;
		this.cmdDispatcher = cmdDispatcher;
		
		postBoard.setMessenger( this );
		m_bLogin = false;
	}
	
	public void start(){
		messengerThread = new Thread( this, "Messenger Thread");
		messengerThread.start();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		boolean bKicked = false;
		try {	
			String sInstruction;
			
			//一開始在輸入姓名時, 只吃/login 跟 /msg 的訊息
			while( (sInstruction = in.readLine()) != null ){
				String sTokens[] = parse( sInstruction );
				String sCmd = sTokens[0];
				if( sCmd.equals("/login") ){
					login(sTokens);
					cmdDispatcher.setOnline();
					break;
				} else if( sCmd.equals("/msg") ) msg( sTokens );
			}
			
			while( (sInstruction = in.readLine()) != null ){
				String sTokens[] = parse( sInstruction );
				String sCmd = sTokens[0];
				if( sCmd.equals("/msg") ) msg( sTokens );
				else if( sCmd.equals("/remove") ) remove( sTokens );
				else if( sCmd.equals("/move") ) move( sTokens );
				else if( sCmd.equals("/post") ) post( sTokens );
				else if( sCmd.equals("/kick") ){
					bKicked = kick( sTokens );
					if( bKicked ) {
						send( "/leave" );
						stop();
						cmdDispatcher.setOffline();
						break;
					}
				}
			}
		} catch (SocketException e) {
			//Server斷線
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	private String[] parse( String sInstruction ){
		String sCmd;
		String sTokens[];
		
		sTokens = sInstruction.split("\\s");
		sCmd = sTokens[0];
		
		/*
		 * yell msg
		 * post String msg
		 * tell someone msg
		 * kick someone
		 * leave
		 * who
		 * remove
		 * 要特別注意 msg中的空白都需要留著
		 */
		int limit = -1;
		if( sCmd.equals("/msg") || sCmd.equals("/login") ) limit = 2;
		else if( sCmd.equals("/post") ) limit = 5;
		else limit = -1;
		
		sTokens = sInstruction.split( "\\s", limit );
		return sTokens;
	}
	
	private void login( String sTokens[] ) {
		
		// 設定姓名
		m_sUsername = sTokens[1];
		m_bLogin = true; // bLogin會用來表示Login進去後，有確定的Username產生output.txt後才能寫檔
	}
	
	private void msg( String sTokens[] ){
		String sMsg;
		sMsg = sTokens[1];
		//Log.i( "msg", sMsg );
		cmdDispatcher.printlnOnConsole(sMsg);
	}
	
	private void post( String sTokens[] ){
		/*
		 * "/post user msgid String message" for each message. Then, client shows: 
		 	Student1 posted message '101' in String: This is a test.
		 */
		String sUsername = sTokens[1];
		int iPostId = Integer.valueOf( sTokens[2] );
		String sContentType = sTokens[3];
		String sContent = sTokens[4];
		Log.i("PostContent", sContent );
		Post post = new Post( sUsername, iPostId, sContentType, sContent );
		postBoard.add( post );
		cmdDispatcher.printlnOnConsole( sUsername + " posted message '" + iPostId 
	    					+ "' in " + sContentType + ": " + sContent );
	}
	
	private void move( String sTokens[] ){
		int iWidgetId = Integer.valueOf( sTokens[1] );
		int x = Integer.valueOf( sTokens[2] );
		int y = Integer.valueOf( sTokens[3] );
		
		postBoard.moveWidget(iWidgetId, x, y);
		cmdDispatcher.printlnOnConsole("/move " + iWidgetId + " " + x + " " + y);
	}
	
	private void remove( String sTokens[] ){
		/*
		 * /remove 101	<--input chat message here.
        /remove Student1 101	<--Server broadcast to every client
        Student1 remove message '101': This is a test.	<-- Clients show on console
		 */
		String sUsername = sTokens[1];
		int iPostId = Integer.valueOf( sTokens[2] );
		Post post = postBoard.get( iPostId );
		postBoard.remove( post );	
		cmdDispatcher.printlnOnConsole( sUsername + " remove message '" + iPostId + "': " + post.m_sContent );
	}
	
	private boolean kick( String sTokens[] ){
		String sUsername = sTokens[1];
		if( m_sUsername.equals(sUsername) )  return true;
		else return false;
	}
	
	private void send( String sMsg ){
		out.println( sMsg );
		out.flush();
	}
	
	public void stop() {
		try {
			ClientActivity.m_clientSocket.close();
			postBoard.clear();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void join() {
		try {
			messengerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Thread getThread() {
		return messengerThread;
	}
	
	public String getUsername(){
		return m_sUsername;
	}
	
	private String m_sUsername;
	private CommandDispatcher cmdDispatcher;
	boolean m_bLogin;
	
	PostBoard postBoard;
	PrintWriter out;
	BufferedReader in;
	
}
