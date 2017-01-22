import java.net.*;
import java.io.*;


public class ChatServer 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UserInfo userInfo = new UserInfo();
		PostBoard postBoard = new PostBoard();
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket( Integer.valueOf(args[0]) );
			//serverSocket = new ServerSocket( 5566 );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while ( true ){
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
				//Thread Name, User(¦Û¤v), UserInfo, PostBoard
				Messenger messenger = new Messenger("Messenger", clientSocket, userInfo, postBoard);
				messenger.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

}
