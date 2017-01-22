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
	
	public void remove( int iUserId, int iPostId ){
		Post post = m_mPosts.get( new Integer( iPostId ) );
		if( post == null ) throw new NoSuchMessageIdException();
		if( iUserId != post.m_iUserId ) throw new PermissionDeniedException();
		m_mPosts.remove( new Integer( iPostId ) );
	}
	
	public boolean isLegalContentType( String sPostType ){
		for( int i=0; i<m_vRegisteredPostType.size(); i++){
			if( m_vRegisteredPostType.get(i).equals( sPostType ) ) return true;
		}
		if( isWidgetType( sPostType ) ) return true;
		return false;
	}
	
	public void changeWidgetProperty( int iWidgetId, String sProperty ){
		Post post = m_mPosts.get( new Integer( iWidgetId ) );
		if( !isWidgetType( post.m_sContentType ) ) throw new NotWidgetException(); 
		String sTokens[] = post.m_sContent.split("\\s", 3);
		if( sTokens.length == 3 ) post.m_sContent = sTokens[0] + " " + sTokens[1] + " " + sProperty;
	}
	
	public void setWidgetXY( int iWidgetId, int x, int y )
	{
		Post post = m_mPosts.get( new Integer( iWidgetId ) );
		if( !isWidgetType( post.m_sContentType ) ) throw new NotWidgetException(); 
		String sTokens[] = post.m_sContent.split("\\s", 3);
		if( sTokens.length == 3 ) post.m_sContent = x + " " + y + " " + sTokens[2];
		else post.m_sContent = x + " " + y;
	}
	
	public boolean isWidgetType( String sWidgetType )
	{
		return sWidgetType.endsWith("Widget");
	}
	
	public boolean isWidgetFormatRight( String sWidgetProperty )
	{
		String sTokens[] = sWidgetProperty.split("\\s+", 3);
		try{
			int x = Integer.valueOf(sTokens[0]);
			int y = Integer.valueOf(sTokens[1]);
			if( x < 0 || y < 0 ) return false;
		} catch( Exception e ){
			return false;
		}
		return true;
	}
	
	public int findOwnerId( int iPostId )
	{
		Post post = m_mPosts.get( new Integer(iPostId) );
		if( post == null ) throw new NoSuchMessageIdException();
		return post.m_iUserId;
	}
	
	
	public Map<Integer, Post> getPosts()
	{
		return m_mPosts;
	}
	
	public void registerPostType( String sPostType )
	{
			m_vRegisteredPostType.add( sPostType );
	}
	
	public boolean unRegisterPostType( String sPostType )
	{
		return m_vRegisteredPostType.remove( sPostType );
	}
	
	
	int m_iSerialNumber;
 	private Vector<String> m_vRegisteredPostType;
	Map<Integer, Post> m_mPosts;
}
