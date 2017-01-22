import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;


public class PostBoard {
	public PostBoard(){
		m_iSerialNumber = 1;
		m_mPosts = new TreeMap<Integer, Post>();
		m_vRegisteredPostType = new Vector<String>();
		
		registerPostType( "String" );
	}
	
	public void add( Post post ){
		post.setPostId( m_iSerialNumber++ );
		m_mPosts.put(new Integer( post.getPostId() ), post );
	}
	
	public void remove( int iPostId ){
		if( m_mPosts.remove( new Integer( iPostId ) ) == null ) throw new NoSuchMessageIdException();
	}
	
	public boolean isLegalPostType( String sPostType ){
		for( int i=0; i<m_vRegisteredPostType.size(); i++){
			if( m_vRegisteredPostType.get(i).equals( sPostType ) ) return true;
		}
		return false;
	}
	
	public Map<Integer, Post> getPosts(){
		return m_mPosts;
	}
	
	public void registerPostType( String sPostType ){
			m_vRegisteredPostType.add( sPostType );
	}
	
	public boolean unRegisterPostType( String sPostType ){
		return m_vRegisteredPostType.remove( sPostType );
	}
	
	int m_iSerialNumber;
	private Vector<String> m_vRegisteredPostType;
	Map<Integer, Post> m_mPosts;
}
