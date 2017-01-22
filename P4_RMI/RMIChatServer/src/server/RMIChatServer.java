package server;

import java.net.*;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;

import task.TaskAllocator;

public class RMIChatServer
{
	
	RMIChatServer( String[] args ) throws RemoteException
	{
		System.setProperty("java.security.policy" ,"permission.policy");
        System.setProperty("java.rmi.server.codebase", this.getClass().getProtectionDomain().getCodeSource().getLocation().toString() );
        
        if( System.getSecurityManager() == null ){
			System.setSecurityManager( new RMISecurityManager() );
		}
		
		// TODO Auto-generated method stub
		UserInfo userInfo = new UserInfo();
		
	    // create the registry
		//Registry registry;    // rmi registry for lookup the remote objects.
	    //registry = LocateRegistry.createRegistry( 3344 );
		//registry.rebind("@SERVER", taskAllocator);
		TaskAllocator taskAllocator = new TaskAllocator( userInfo );
		
		PostBoard postBoard = new PostBoard();
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket( Integer.valueOf(args[0]) );
			//serverSocket = new ServerSocket( 5566 );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println( "Error: Server [Port]");
			System.exit(0);
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
	
	public static void main(String[] args) 
	{
		try{
			RMIChatServer s = new RMIChatServer( args );
		} catch( Exception e ){
	    	e.printStackTrace();
	    	System.exit(1);
		}
	}

}
