import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


public class Messenger extends Thread
{
	Messenger(String sThreadName, Socket clientSocket, UserInfo userInfo, PostBoard postBoard){
		super(sThreadName);
		this.clientSocket = clientSocket;
		this.userInfo = userInfo;
		this.postBoard = postBoard;
	}
	
	public void run() {
		
		// 假如登入失敗直接return
		if( !login() ) return; 
		
		String sInstruction;
		try {
			while( (sInstruction = m_in.readLine()) != null ){
				
				I.writeInputLog( sInstruction );
				String[] sTokens = parse( sInstruction );
				String sCmd = sTokens[0];
				if( sCmd.equals("/tell") ) tell( sTokens );
				else if( sCmd.equals("/yell") ) yell( sTokens );
				else if( sCmd.equals("/post") ) post( sTokens );
				else if( sCmd.equals("/kick") ) kick( sTokens );
				else if( sCmd.equals("/remove") ) remove( sTokens );
				else if( sCmd.equals("/who") ) who();
				else if( sCmd.equals("/leave") ){
					leave();
					return;
				} else {
					/*
					  * If the user types a wrong command, echo an error message:
						    "**** Your message command '......' is incorrect". 
					 */
					send("/msg **** Your message command '" + sCmd + "' is incorrect.");

				}
			}
		} catch (SocketException e) {
			//使用者斷線
			leave();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean login(){
		String sUserName = null;
		/*
		 * 加入unknown User 是為了讓 其他使用者也能看到 unKnownUser的資訊
		 */
		userInfo.addUnknownUser( clientSocket ); 
		
		try {
			m_out = new PrintWriter(clientSocket.getOutputStream(), true);
			m_in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
			
			//使用者會先輸入他的帳號過來
			int iDuplicate;
			while( ( sUserName = m_in.readLine()) != null ){
				//若為空字串
				if ( sUserName.equals("") ){
					m_out.println( "/msg Error: No username is input." );
					m_out.println( "/msg UserName: ");
					m_out.flush();
					continue;
				}
				
				// 尋找舊記錄中，是否有User的資料
				if ( (iDuplicate = userInfo.find( sUserName )) >= 0){
					/*
					 * 查詢此User是否在線上
					 * if( true ) 則回傳Error;
					 * else 則繼續使用舊記錄的資料
					 */
					if( userInfo.isUserOnline( iDuplicate ) ){
						m_out.println( "/msg Error: The user '" + 
									sUserName +
									"' is already online. Please change a name.");
						m_out.println( "/msg UserName: ");
						m_out.flush();
	
					} else {
						I = userInfo.getUser( iDuplicate );
						// socket要重新指定
						userInfo.online( I, clientSocket );
						break;
					}
				} else {
					//新創造一個User，clientSocket要先指定，addUser的時候會寫入ConnectionLog檔
					I = new User( sUserName, clientSocket);
					userInfo.addUser( I );
					userInfo.online( I );
					break;
				}
				
				
			}
			
			// welcome message
			/*
			  * For each new created connection, broadcast to everyone
			    a message of "someone is connecting to the chat server", this message shows before greeting messages.
			 */
			
			userInfo.removeUnknownUser( clientSocket );
			// login 為我自己定義的Protocol，用來告知登入成功
			m_out.println("/login " + I.m_sUserName );
			m_out.flush();
			
			broadcast( "/msg " + I.m_sUserName + " is connecting to the chat server.");
			
			send("/msg *******************************************");
			send("/msg ** <" + sUserName + ">, welcome to the chat system.");
			send("/msg *******************************************");
			
			Map<Integer, Post> mPosts = postBoard.getPosts();
			Set<Map.Entry<Integer, Post>> entries = mPosts.entrySet();
			
			for(Map.Entry<Integer, Post> entry : entries ){
				Integer integer = entry.getKey();
				Post post = entry.getValue();
				//"/post user msgid String message" for each message.
				send("/post " + userInfo.getUserName(post.m_iUserId) + " "+ integer + " " + post.m_sContentType + " " + post.m_sContent);
			}
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//使用者自己斷線
			userInfo.removeUnknownUser( clientSocket );
			e.printStackTrace();
			return false;
		}
	}
	
	private String[] parse( String sInstruction ){
		String sCmd;
		String sTokens[];
		
		sTokens = sInstruction.split(" ");
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
		if( sCmd.equals("/yell") ) limit = 2;
		else if( sCmd.equals("/post") || sCmd.equals("/tell") ) limit = 3;
		else limit = -1;
		
		sTokens = sInstruction.split( "\\s+", limit );
		return sTokens;
	}
	
	private void tell( String sTokens[] ){
		/*
	    * "/tell someone msgs": send "msgs" to the person "someone" only. 
	      
	      Examples:     
	        /tell Student2 Hello, Student2.	<-- input chat message here.
	        /msg Student1 told Student2: Hello, Student2.	<-- Server sends to Student2.
	        Student1 told Student2: Hello, Student2	<-- Student2 shows on console.
	    		
	    		/tell Student2 Hello, Student2.	<-- input chat message here, but if Student2 does not exist.
	        /msg Error: 'Student2' is not online.	<-- Server sends back to the client.
	        Error: 'Student2' is not online.	<-- The client shows on console.
	    
	    		/tell	<-- input chat message here, but if empty. 
	        /msg Error: No target was given.	<-- Server sends back to the client.
	        Error: No target was given.	<-- The client shows on console.
        */

		String sUserName = null;
		String sSaid;
		
		try{
			sUserName = sTokens[1]; //ArrayIndexOutOfBoundsException
			try {
				sSaid = sTokens[2];
			} catch ( ArrayIndexOutOfBoundsException e ) {
				sSaid = "";
			}
			
			User user;
			user = userInfo.searchOnlineUser( sUserName );
			PrintWriter out = new PrintWriter( user.getSocket().getOutputStream() );
			send(out, "/msg " + I.m_sUserName + " told " + sUserName + ": " + sSaid);
		} catch ( ArrayIndexOutOfBoundsException e ){
			
			send( "/msg Error: No target was given." );
		} catch (NullPointerException e) {
			
			send("/msg Error: '" + sUserName + "' is not online." );
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	private void yell( String sTokens[] ){
		
	    /*
	    * "/yell msgs": send "msgs" to all people. 
	      
	      Examples:     
	        /yell Blah, Blah, ...     <--input chat message here.
	        /msg Student1 yelled: Blah, Blah, ...		<--Server sends to all clients.
	        Student1 yelled: Blah, Blah, ...<-- Clients show on console.
	        
	        /yell	<--input chat message here.
	        /msg Student1 yelled:	<--Server sends to all clients.
	        Student1 yelled:	<-- Clients show on console.
         */
		String sYell;
		try{
			sYell = sTokens[1];
		} catch ( ArrayIndexOutOfBoundsException e ){
			sYell = "";
		}
		String sMsg = "/msg " + I.m_sUserName + " yelled: " + sYell;
		broadcast( sMsg );
	}
	
	private void post( String sTokens[] ){
		/*
	    * "/post String msg": Client sends this message to server. In server, assign a series id "msgid" to the message, 
	      put (user, msgid, type, msg) into a global buffer,e.g.(Student1, 1, String, This is a test.), 
	      and then broadcast to others the message in the format: "/post user msgid String message"
	      
	      Examples: 
	        /post String This is a test.	<--input chat message here.
	        /post Student1 101 String This is a test.	<--Server broadcast to all client.
	        Student1 posted message '101' in String: This is a test.	<-- clients show on console.
	    		
	        /post String <--input chat message here.
	        /post Student1 101 String	<--Server broadcast to every client.
	        Student1 posted message '101' in String:	<-- Clients show on console.
	   			
	        /post Obj This is a test.	<--input chat message here.
	        /msg Error: No such post type.	<--Server send back to the client, and no broadcast.
	        Error: No such post type.	<-- The client shows on console.
	        
	        /post	<--input chat message here.
	        /msg Error: No post type.	<--Server send back to the client, and no broadcast.
	        Error: No post type.	<-- The client shows on console.
		 */
		
		String sContentType;
		String sContent;
		try{
			sContentType = sTokens[1]; //ArrayIndexOutOfBoundsException
			try{
				sContent = sTokens[2];
			} catch ( ArrayIndexOutOfBoundsException e ){
				sContent = "";
			}
			
			if( !postBoard.isLegalPostType(sContentType) ) throw new NoSuchPostType();
			Post post = new Post( I.getUserId(), sContentType, sContent );
			postBoard.add(post);
			
			String sMsg = "/post " + I.m_sUserName + " " + post.getPostId() + " " + sContentType + " " + sContent;
			broadcast( sMsg );
		} catch ( ArrayIndexOutOfBoundsException e ){
			
			send( "/msg Error: No post type." );
		} catch ( NoSuchPostType e ){
			
			send( "/msg Error: No such post type." );
		}
	}
	
	private void remove( String sTokens[] ){
		/*
        /remove 101	<--input chat message here.
        /remove Student1 101	<--Server broadcast to every client
        Student1 remove message '101': This is a test.	<-- Clients show on console
        
        /remove 101 abababab asdfasdf	<--input chat message here.
        /remove Student1 101	<--Server broadcast to every client
        Student1 remove message '101': This is a test.	<-- Clients show on console
        
        Assume that no 101 in the buffer.
        /msg Error: No such msg id.	<--Server send back to the client, and no broadcast.
        Error: No such msg id.	<-- The client show on console.
        
        /remove 101asdf	<--input chat message here.
        /msg Error: No such msg id.	<--Server send back to the client, and no broadcast.
        Error: No such msg id.	<-- The client shows on console.

        /remove	<--input chat message here.
        /msg Error: No msg id.	<--Server send back to the client, and no broadcast.
        Error: No msg id.	<-- The client shows on console.
		 */
		
		String sPostId;
		try {
			sPostId = sTokens[1]; //ArrayIndexOutOfBoundsException
			int iPostId = Integer.valueOf( sPostId ); //NumberFormatException
			postBoard.remove( iPostId ); //NoSuchMessageIdException
			String sMsg = "/remove " + I.m_sUserName + " " + iPostId;
			broadcast( sMsg );
		} catch ( ArrayIndexOutOfBoundsException e ){
			
			send( "/msg Error: No msg id." );
		} catch ( NumberFormatException e ) {
			
			send( "/msg Error: No such msg id." );			
		} catch ( NoSuchMessageIdException e ){
			
			send( "/msg Error: No such msg id." );			
		}
		
	}
	
	private void who( ){
		/*
		 * For "/who", display all the on-line users in the following format.
		 * Format:
		 * 			Name\tIP/port\n
		 * 			%s\t%s[\t <-- myself]
		 * Example:
		 * 			Name	IP/port
		 * 		Student0:	140.113.210.62/3145 
		 * 		Student1: 140.113.210.63/1456 <-- myself
		 * 		Student2: 140.113.210.64/4561 
		 */


		Vector<User> vOnlineUsers = userInfo.getOnlineUsers();
		ArrayList<Socket> lOnlineUnknownUsers = userInfo.getOnlineUnknownUsers();
		String sMsg = "/msg Name\tIP/port";
		send( sMsg );
		
		for( int i=0; i<lOnlineUnknownUsers.size(); i++){
			Socket userSocket = lOnlineUnknownUsers.get(i);
			sMsg = "/msg (Unknown):\t" + userSocket.getInetAddress() + "/" + userSocket.getPort();			
			send(sMsg);
		}
		
		for( int i=0; i<vOnlineUsers.size(); i++){
			User user = vOnlineUsers.get(i);
			sMsg = "/msg " + user.m_sUserName + 
					":\t" + user.getSocket().getInetAddress() + "/" + user.getSocket().getPort();
			if( I == user ) sMsg += "\t <-- myself";
			send(sMsg);
		}
		
	}
	
	private void kick( String sTokens[] ){
		/*
		  * "/kick": Allow to kick "someone" by typing "/kick someone".
		    Assume that 'Student1' types "/kick Student2". 
		    Actually, the server broadcasts "/kick Student2" first, 
		    marks "kicked" in the thread of 'Student2',
		    and then the client 'Student2' leaves by sending "/leave". 
		    Note: Do not need to worry about the privilege problem.
	            Before the client sends "/leave" to server, the client can still recieve the message.
		 */
		try{
			String sUserName = sTokens[1];
			broadcast("/kick " + sUserName);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			
			send( "/msg Error: No name of kicked person." );
		}
	}
	
	private void leave(){
		/*
		 * When a client leave, the client can't see the broadcast message.
		 * 這裡一定要先 offline，不然會收到離開的廣播
		 * boardcast message "someone is leaving the chat server". 
		 */

		userInfo.offline( I );
		String sMsg = "/msg " + I.m_sUserName + " is leaving the chat server.";
		broadcast( sMsg );
	}
	
	private void send( String sMsg ){
		m_out.println( sMsg );
		m_out.flush();
		I.writeOutputLog( sMsg + "\n");
	}
	
	private void send( PrintWriter out, String sMsg ){
		out.println( sMsg );
		out.flush();
		I.writeOutputLog( sMsg + "\n");
	}
	
	private void broadcast( String sMsg ){
		Vector<User> vOnlineUsers = userInfo.getOnlineUsers();	
		
		for( int i=0; i<vOnlineUsers.size(); i++ ){
			try {
				User user = vOnlineUsers.get(i);
				PrintWriter out = new PrintWriter( user.getSocket().getOutputStream() );
				out.println( sMsg );
				out.flush();
				user.writeOutputLog( sMsg );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private PrintWriter m_out;
	private BufferedReader m_in;
	private Socket clientSocket;
	private User I;
	private UserInfo userInfo;
	private PostBoard postBoard;
}
