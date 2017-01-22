import java.util.Vector;

public class PostBoard {
	PostBoard(){
		m_vPosts = new Vector<Post>();
	}
	
	public Vector<Post> getPosts(){
		return m_vPosts;
	}
	
	public void add( Post post ){
		m_vPosts.add( post );
	}
	
	public void remove( Post post ){
		m_vPosts.remove(post);
	}
	
	public void clear(){
		m_vPosts.clear();
	}
	
	public Post search( int iPostId ){
		for( int i=0; i<m_vPosts.size(); i++ ){
			if( m_vPosts.get(i).m_iPostId == iPostId ) return m_vPosts.get(i);
		}
		return null;
	}
	
	private Vector<Post> m_vPosts;
}
