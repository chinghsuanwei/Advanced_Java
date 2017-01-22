package client;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import widget.Widget;
import android.util.Log;

public class PostBoard {
	PostBoard( ChatRoomActivity chatRoomActivity, WhiteBoard whiteBoard ){
		m_mPosts = new TreeMap<Integer, Post>();
		this.whiteBoard = whiteBoard;
		this.chatRoomActivity = chatRoomActivity;
	}
	
	public void setMessenger( Messenger messenger ){
		this.messenger = messenger;
	}
	
	public Map<Integer, Post> getPosts(){
		return m_mPosts;
	}
	
	public void add( Post post ){
		m_mPosts.put( new Integer(post.m_iPostId), post );
		if( post.m_sContentType.endsWith("Widget") ){
			try {
				
				Class<?> cls = Class.forName("widget." + post.m_sContentType);
				Constructor<?> ct = cls.getConstructor();
				Widget widget = (Widget)ct.newInstance();
				widget.parseCommand( post.m_sContent );
				whiteBoard.addWidget( post.m_iPostId, widget );
				
				chatRoomActivity.addMenuItem( post.m_sContentType );
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean amIAuthor( int iWidgetId ){
		Post post = m_mPosts.get( new Integer(iWidgetId) );
		if( post.m_sUsername.equals( messenger.getUsername() ) ) return true;
		else return false;
	}
	
	public void moveWidget( int iWidgetId, int x, int y){
		Post post = m_mPosts.get( iWidgetId );
		String sTokens[] = post.m_sContent.split("\\s+", 3);
		String sNewContent = x + " " + y + " " + sTokens[2];
		post.setContent( sNewContent );
		
		whiteBoard.moveWidget( iWidgetId, x, y );
	}
	
	public void remove( Post post ){
		m_mPosts.remove( new Integer(post.m_iPostId) );
		if( post.m_sContentType.endsWith("Widget") ){
			whiteBoard.removeWidget( post.m_iPostId );
		}
	}
	
	public void clear(){
		m_mPosts.clear();
	}
	
	public Post get( int iPostId ){
		return m_mPosts.get( new Integer(iPostId) );
	}
	
	private Map<Integer, Post> m_mPosts;
	private WhiteBoard whiteBoard;
	private ChatRoomActivity chatRoomActivity;
	private Messenger messenger;
}
