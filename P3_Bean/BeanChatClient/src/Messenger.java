import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;



public class Messenger implements Runnable{
	private Thread messengerThread;
	
	Messenger(CommandDispatcher cmdDispatcher, PostBoard postBoard, Socket clientSocket, PrintWriter out, BufferedReader in){
		this.postBoard = postBoard;
		this.clientSocket = clientSocket;
		this.out = out;
		this.in = in;
		this.cmdDispatcher = cmdDispatcher;
		postBoard.setMessenger(this);
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
				writeInputLog( sInstruction );
				String sTokens[] = parse( sInstruction );
				String sCmd = sTokens[0];
				if( sCmd.equals("/msg") ) msg( sTokens );
				else if( sCmd.equals("/remove") ) remove( sTokens );
				else if( sCmd.equals("/move") ) move( sTokens );
				else if( sCmd.equals("/change") ) change( sTokens );
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
		else if( sCmd.equals("/change") ) limit = 3;
		else if( sCmd.equals("/post") ) limit = 5;
		else limit = -1;
		
		sTokens = sInstruction.split( "\\s", limit );
		return sTokens;
	}
	
	private void login( String sTokens[] ) {
		
		// 設定姓名
		m_sUserName = sTokens[1];
		
		try {
			m_fwOutput = new FileWriter("output_" + m_sUserName + ".txt");
			m_fwInput = new FileWriter("input_" + m_sUserName + ".txt");
			m_bLogin = true; // bLogin會用來表示Login進去後，有確定的UserName產生output.txt後才能寫檔
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	private void msg( String sTokens[] ){
		String sMsg;
		sMsg = sTokens[1];
		cmdDispatcher.printlnOnConsole(sMsg);
	}
	
	private void post( String sTokens[] ){
		/*
		 * "/post user msgid String message" for each message. Then, client shows: 
		 	Student1 posted message '101' in String: This is a test.
		 */
		String sUserName = sTokens[1];
		int iPostId = Integer.valueOf( sTokens[2] );
		String sContentType = sTokens[3];
		String sContent = sTokens[4];
		
		Post post = new Post( sUserName, iPostId, sContentType, sContent );
		postBoard.add( post );
		cmdDispatcher.printlnOnConsole( sUserName + " posted message '" + iPostId 
	    					+ "' in " + sContentType + ": " + sContent );
	}
	
	private void remove( String sTokens[] ){
		/*
		 * /remove 101	<--input chat message here.
        /remove Student1 101	<--Server broadcast to every client
        Student1 remove message '101': This is a test.	<-- Clients show on console
		 */
		String sUserName = sTokens[1];
		int iPostId = Integer.valueOf( sTokens[2] );
		Post post = postBoard.search( iPostId );
		cmdDispatcher.printlnOnConsole( sUserName + " remove message '" + iPostId + "': " + post.m_sContent );
		postBoard.remove( post );			
	}
	
	private void move( String sTokens[] ){
		int iWidgetId = Integer.valueOf( sTokens[1] );
		int x = Integer.valueOf( sTokens[2] );
		int y = Integer.valueOf( sTokens[3] );
		postBoard.setWidgetXY(iWidgetId, x, y);
		cmdDispatcher.printlnOnConsole("/move " + iWidgetId + " " + x + " " + y);
	}
	
	private void change( String sTokens[] ){
		int iWidgetId = Integer.valueOf( sTokens[1] );
		String sProperty = sTokens[2];
		postBoard.changeWidgetProperty( iWidgetId, sProperty );
		cmdDispatcher.printlnOnConsole("/change " + iWidgetId + " " + sProperty);
	}
	
	private boolean kick( String sTokens[] ){
		String sUserName = sTokens[1];
		if( m_sUserName.equals(sUserName) )  return true;
		else return false;
	}
	
	private void send( String sMsg ){
		out.println( sMsg );
		out.flush();
		writeOutputLog( sMsg );
	}
	
	public void writeOutputLog( String sLog ){
		
		//需要做這個判斷只有一開始輸入姓名時候，因Output.txt還沒創建不能寫檔
		if( m_bLogin ){
			try {
				m_fwOutput.append( sLog + "\r\n" );
				m_fwOutput.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void writeInputLog( String sLog ){
		
		try {
			m_fwInput.append( sLog + "\r\n" );
			m_fwInput.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void stop() {
		try {
			clientSocket.close();
			m_fwOutput.close();
			m_fwInput.close();
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
	
	public String getName(){
		return m_sUserName;
	}
	
	public Thread getThread() {
		return messengerThread;
	}
	
	public String getUserName(){
		return m_sUserName;
	}
	
	public FileWriter getOutputFileWriter(){
		return m_fwOutput;
	}
	
	public FileWriter getInputFileWriter(){
		return m_fwInput;
	}
	
	private String m_sUserName;
	private FileWriter m_fwOutput;
	private FileWriter m_fwInput;
	private CommandDispatcher cmdDispatcher;
	boolean m_bLogin;
	
	PostBoard postBoard;
	Socket clientSocket;
	PrintWriter out;
	BufferedReader in;
	
}
