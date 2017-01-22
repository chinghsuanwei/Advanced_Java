import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;


public class ChatClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PostBoard postBoard = new PostBoard();
		
        Console console = new Console(postBoard, args);
        console.start();
	}

}
