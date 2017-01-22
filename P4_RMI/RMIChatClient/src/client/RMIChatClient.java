package client;

import java.rmi.RMISecurityManager;

public class RMIChatClient {

	/**
	 * @param args
	 */
	
	RMIChatClient(String[] args)
	{
		System.setProperty("java.security.policy" ,"permission.policy");
        System.setProperty("java.rmi.server.codebase", this.getClass().getProtectionDomain().getCodeSource().getLocation().toString() );
        
		if( System.getSecurityManager() == null ){
			System.setSecurityManager( new RMISecurityManager() );
		}
        /*System.setSecurityManager (new RMISecurityManager() {
			public void checkConnect (String host, int port) {}
			public void checkConnect (String host, int port, Object context) {}
			public void checkWrite(FileDescriptor fd) {}
			public void checkWrite(String sFile) {}
		});*/
		
		MainFrame mainFrame = new MainFrame("ChatClient", args);		
	}
	
	public static void main(String[] args) {
		
		new RMIChatClient( args );
	}
	

}
