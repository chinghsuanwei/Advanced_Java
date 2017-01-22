import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Vector;


public class UserInfo {
	
	UserInfo(){
		lOnlineUnknownUsers = new ArrayList<Socket>();
		vOnlineUsers = new Vector<User>();
		vUsers = new Vector<User>();
		// 讓第0個物件為空
		vUsers.add(null);
		try {
			m_fstream = new FileWriter("connect_log.txt");
			m_fstream.write("IP/port\tlogID\r\n");
			m_fstream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_iSerialNumber = 1;
	}
	
	public int find( String sUserName ){
		for( int i=1; i<vUsers.size(); i++){
			if( vUsers.get(i).m_sUserName.equals(sUserName) ) return i;
		}
		
		return -1;
	}
	
	public void addUser( User user ){
		user.setUserId( m_iSerialNumber++ );
		vUsers.add( user );
		try {
			m_fstream.append( user.getSocket().getInetAddress() + "/" + user.getSocket().getPort() + "\t" + user.getUserId() + "\r\n" );
			m_fstream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public User getUser( int iUserId ){
		return vUsers.get( iUserId );
	}
	
	public String getUserName( int iUserId ){
		return vUsers.get( iUserId ).m_sUserName;
	}
	
	public void addUnknownUser( Socket socket ){
		lOnlineUnknownUsers.add(socket);
	}
	
	public void removeUnknownUser( Socket socket ){
		lOnlineUnknownUsers.remove(socket);
	}
	
	public boolean isUserOnline( int iUserId ){
		for( int i=0; i<vOnlineUsers.size(); i++){
			if( vOnlineUsers.get(i).getUserId() == iUserId ) return true;
		}

		return false;
	}
	
	public void online( User user, Socket socket ){
		user.setSocket(socket);
		vOnlineUsers.add( user );
	}
	
	public void online( User user ){
		vOnlineUsers.add( user );
	}
	
	public void offline( User user ){
		try {
			user.getSocket().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		user.setSocket(null);
		vOnlineUsers.remove( user );
	}
	
	public User searchOnlineUser( String sUserName ){
		for( int i=0; i<vOnlineUsers.size(); i++){
			if( vOnlineUsers.get(i).m_sUserName.equals(sUserName) ) return vOnlineUsers.get(i);
		}
		
		throw new NullPointerException();
	}
	
	public Vector<User> getOnlineUsers(){
		return vOnlineUsers;
	}
	
	public ArrayList<Socket> getOnlineUnknownUsers(){
		return lOnlineUnknownUsers;
	}
	
	protected void finalize() throws Throwable
	{
	  //do finalization here
	  m_fstream.close();
	  super.finalize(); //not necessary if extending Object.
	} 
	
	/*
	 * lUnknownUsers 為還未完成輸入姓名的玩家
	 */
	private ArrayList<Socket> lOnlineUnknownUsers;
	/*
	 * vOnlineUsers 為目前線上玩家
	 */
	private Vector<User> vOnlineUsers;
	/*
	 * vUsers 記錄所有User資料，只要上線過一次就會記住
	 */
	private Vector<User> vUsers;
	
	public int m_iSerialNumber;
	/*
	 * ConnectionLog檔
	 */
	FileWriter m_fstream;
}
 
