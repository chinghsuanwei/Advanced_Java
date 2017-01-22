package client;

public class Post {
	Post( String sUsername, int iPostId, String sContentType, String sContent ){
		m_sUsername = sUsername;
		m_iPostId = iPostId;
		m_sContentType = sContentType;
		m_sContent = sContent;
	}
	
	public void setContent( String sContent ){
		m_sContent = sContent;
	}
	
	String m_sUsername;
	int m_iPostId;
	String m_sContentType;
	String m_sContent;
}
