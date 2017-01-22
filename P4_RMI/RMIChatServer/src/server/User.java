package server;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;


public class User {
	User( String sUsername, Socket socket){
		m_sUsername = sUsername;
		m_socket = socket;
		
		try {
			m_fwOutput = new FileWriter("output_" + sUsername + ".txt");
			m_fwInput = new FileWriter("input_" + sUsername + ".txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setUserId( int iUserId ){
		m_iUserId = iUserId;
	}
	
	public int getUserId(){
		return m_iUserId;
	}
	
	public Socket getSocket(){
		return m_socket;
	}
	
	public void setSocket( Socket socket ){
		m_socket = socket;
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
	
	public void writeOutputLog( String sLog ){
		try {
			m_fwOutput.append( sLog + "\r\n" );
			m_fwOutput.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String m_sUsername;
	private int m_iUserId;
	private Socket m_socket;
	private FileWriter m_fwOutput;
	private FileWriter m_fwInput;
}
