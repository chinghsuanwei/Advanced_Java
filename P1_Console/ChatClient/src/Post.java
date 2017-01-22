
public class Post {
	Post( String sUserName, int iPostId, String sContentType, String sContent ){
		m_sUserName = sUserName;
		m_iPostId = iPostId;
		m_sContentType = sContentType;
		m_sContent = sContent;
	}
	
	String m_sUserName;
	int m_iPostId;
	String m_sContentType;
	String m_sContent;
}
