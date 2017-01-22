package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.chat.client.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ClientActivity extends Activity {
    /** Called when the activity is first created. */
    
	static public Socket m_clientSocket;
	
    EditText ipEditText;
    EditText portEditText;
    public static String sIp;
    public static int iPort;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        Button okButton = (Button) findViewById(R.id.okButton);
        ipEditText = (EditText) findViewById(R.id.ipEditText);
        portEditText = (EditText) findViewById(R.id.portEditText);
        
        okButton.setOnClickListener( new View.OnClickListener() {
        	@Override
        	public void onClick(View view){
        		// socket connect
				sIp = ipEditText.getText().toString();
				iPort = Integer.valueOf( portEditText.getText().toString() );
        		InetSocketAddress isa = new InetSocketAddress( sIp, iPort );
        		m_clientSocket = new Socket();
        		try {
        			m_clientSocket.connect(isa, 10000);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Toast.makeText(ClientActivity.this, 
							"Error: cannot connect to [" + sIp + ":" + iPort + "]", Toast.LENGTH_LONG ).show();
					return;
				}
        		
        		Intent intent = new Intent();  
                intent.setClass(ClientActivity.this, ChatRoomActivity.class);  
                startActivity(intent);
        	}
        });
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.i("BP", "onPause ClientActivity");
    }
    
    @Override
    protected void onResume() {
    	super.onPause();
    	Log.i("BP", "onResume ClientActivity");
    }
}