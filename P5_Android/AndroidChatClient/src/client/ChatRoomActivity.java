package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Vector;

import client.CommandDispatcher.CONNECTIONSTATE;

import android.app.Activity;
import android.chat.client.R;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class ChatRoomActivity extends Activity {
	
	EditText inputEditText;
	TextView consoleTextView;
	WhiteBoard whiteBoard;
	ScrollView consoleScrollView;
	ScrollView whiteBoardScrollView;
	CommandDispatcher cmdDispatcher;
	
	
	Vector<String> m_vMenuItems;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("BP", "onCreate ChatRoomActivity");
        setContentView(R.layout.chat_room);
        
        m_vMenuItems = new Vector<String>();;
        inputEditText = (EditText) findViewById(R.id.inputEditText);
        whiteBoardScrollView = (ScrollView) findViewById(R.id.whiteBoardScrollView);
        consoleScrollView = (ScrollView) findViewById(R.id.consoleScrollView);
        consoleTextView = (TextView) findViewById(R.id.consoleTextView);
        whiteBoard = (WhiteBoard) findViewById(R.id.whiteBoard);
        PostBoard postBoard = new PostBoard( this, whiteBoard );
        
        inputEditText.setOnKeyListener( new View.OnKeyListener() { 
        	@Override
        	public boolean onKey(View v, int keyCode, KeyEvent event) {
	            /*
	             * 設定按下換行鍵就送出
	             */
	            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                	String sInput = inputEditText.getText().toString();
                	cmdDispatcher.dispatch( sInput );
                	inputEditText.getText().clear();
                	return true;
	            }
	            return false;
        	}
        }
        );
        cmdDispatcher = new CommandDispatcher(this, postBoard);
        // 是第一次Create
        if( savedInstanceState == null ) printOnConsole("Username: ");
        
        whiteBoard.setPostBoard( postBoard );
        whiteBoard.setCommandDispatcher( cmdDispatcher ); // for sending /post message
        whiteBoard.setWhiteBoardScrollView( whiteBoardScrollView );
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	Bundle map = new Bundle();
    	Log.i("BP", "onSaveInstanceState in ChatRoomActivity");
    	map.putString("consoleText", consoleTextView.getText().toString() );
    	map.putInt("connectionState", cmdDispatcher.m_eConnectionState.ordinal() );
    	
    	if( cmdDispatcher.m_eConnectionState == CONNECTIONSTATE.ONLINE ){
	    	map.putString("ip", ClientActivity.sIp );
	    	map.putInt("port", ClientActivity.iPort );
	    	map.putString("name", cmdDispatcher.messenger.getUsername() );
    	}
    	
        outState.putBundle("BUNDLE_CHATROOM", map);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) { 
    	super.onRestoreInstanceState(savedInstanceState);
    	Log.i("BP", "onRestoreInstanceState in ChatRoomActivity");
    	bFirstCreated = false;
    	Bundle map = savedInstanceState.getBundle("BUNDLE_CHATROOM");
    	consoleTextView.setText( map.getString("consoleText") );
    	CONNECTIONSTATE connectionState = CONNECTIONSTATE.values()[map.getInt("connectionState")];
    	if( connectionState == CONNECTIONSTATE.ONLINE ){
    		bConnected = true;
	    	ClientActivity.sIp = map.getString("ip");
	    	ClientActivity.iPort = map.getInt("port");
	    	sLastUsername = map.getString("name");
    	} else {
    		bConnected = false;
    		if( connectionState == CONNECTIONSTATE.OFFLINE ) cmdDispatcher.setOffline();
    		else if( connectionState == CONNECTIONSTATE.NONAME ) printOnConsole("\nUsername: ");
    	}
    }
    
    String sLastUsername;
    @Override
    protected void onPause() {
    	//close socket
    	super.onPause();
    	Log.i("BP", "onPause ChatRoomActivity");
    	if( ClientActivity.m_clientSocket.isConnected() ) {
    		//必須紀錄名子，下次Resume時才能夠連回來
    		sLastUsername = cmdDispatcher.messenger.getUsername();
    		//cmdDispatcher.leave() 也會終止 messenger的Thread
    		cmdDispatcher.leave();
    		bConnected = true;
    	} else bConnected = false;
    }
    
    boolean bFirstCreated = true;
    boolean bConnected = false;
    @Override
    protected void onResume() {
    	super.onResume();
    	if( !bFirstCreated && bConnected ) {
	    	Log.i("BP", "onResume ChatRoomActivity");
	    	//cmdDispatcher.reconnect() 會開啟一個新的 messenger的Thread
			cmdDispatcher.reconnect( sLastUsername );
    	}
    	
    	bFirstCreated = false;
    }
    
    public void printOnConsole( String sOutput )
    {
    	runOnUiThread( new PrintOnConsoleThread( sOutput ) );
    }
    
    public void addMenuItem( String sMenuItem )
    {
    	//check 是否MenuItem 有重覆, 重覆則不新增
    	for(int i=0; i<m_vMenuItems.size(); i++){
    		if( sMenuItem.equals( m_vMenuItems.get(i) ) ) return;
    	}
    	
    	m_vMenuItems.add(sMenuItem);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	
    	for(int i=0; i<m_vMenuItems.size(); i++)
    		menu.add( Menu.NONE, i, Menu.NONE, m_vMenuItems.get(i));
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
    	CharSequence csMenuItem = item.getTitle();
    	whiteBoard.setMenuItemSelected( csMenuItem.toString() );
    	return true;
    }
    
    class PrintOnConsoleThread implements Runnable{
    	
    	String sOutput;
    	PrintOnConsoleThread( String sOutput ){
    		this.sOutput = sOutput;
    	}
    	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			consoleTextView.append( sOutput );
		    consoleScrollView.fullScroll(View.FOCUS_DOWN);
		    consoleTextView.invalidate();
		    inputEditText.requestFocus();
		}
    	
    }
    
}
