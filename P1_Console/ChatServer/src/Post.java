
public class Post {
	
	Post( int iUserId, String sContentType, String sContent ){
		m_iUserId = iUserId;
		m_sContentType = sContentType;
		m_sContent = sContent;
	}
	
	public void setPostId( int iPostId){
		m_iPostId = iPostId;
	}
	
	public int getPostId(){
		return m_iPostId;
	}
	
	public String getContentType(){
		return m_sContentType;
	}
	
	int m_iUserId;
	private int m_iPostId;
	String m_sContentType;
	String m_sContent;
}
