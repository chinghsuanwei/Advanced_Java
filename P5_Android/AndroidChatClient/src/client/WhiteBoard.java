package client;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import widget.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class WhiteBoard extends View implements View.OnTouchListener{
	Paint mPaint;
	PostBoard postBoard;
	CommandDispatcher cmdDispatcher;
	ScrollView whiteBoardScrollView;
	Map<Integer, Widget> m_mWidgets;
	boolean m_bMenuItemSelected;
	String m_sMenuItem;
	int height;
	int width;
	
	public WhiteBoard(Context context) {
		super(context);
		initialize();
	}
	
    public WhiteBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public WhiteBoard(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		initialize();
	}
	
	public void initialize()
	{
		m_mWidgets = new HashMap<Integer, Widget>();
		m_bMenuItemSelected = false;
		setOnTouchListener(this);
	}
	
	public void setPostBoard( PostBoard postBoard )
	{
		this.postBoard = postBoard;
	}
	
	public void setCommandDispatcher( CommandDispatcher cmdDispatcher ){
		this.cmdDispatcher = cmdDispatcher;
	}
	
	public void setWhiteBoardScrollView( ScrollView whiteBoardScrollView ){
		this.whiteBoardScrollView = whiteBoardScrollView;
	}
	
	public void setMenuItemSelected( String sMenuItem )
	{
		m_bMenuItemSelected = true;
		m_sMenuItem = sMenuItem;
	}
	
	boolean bFirstMeasure = true;
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.i("BP", "OnMeasure" + whiteBoardScrollView.getHeight() + " " + whiteBoardScrollView.getWidth() );
		Log.i("BP", "OnMeasure spec " + widthMeasureSpec + " " + heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if( bFirstMeasure ){
        	if( whiteBoardScrollView.getHeight() > 0 && whiteBoardScrollView.getWidth() > 0 ){
        		height = whiteBoardScrollView.getHeight();
        		width = whiteBoardScrollView.getWidth();
        		bFirstMeasure = false;
        	}
        } else {
        	
        }
		setMeasuredDimension(width, height);
    }
	
	public void addWidget( int iWidgetId, Widget widget ){
		m_mWidgets.put( new Integer(iWidgetId), widget );
		postInvalidate();
	}
	
	public void removeWidget( int iWidgetId ){
		m_mWidgets.remove( new Integer(iWidgetId) );
		postInvalidate();
	}
	
	public void moveWidget( int iWidgetId, int x, int y ){
		Widget widget = m_mWidgets.get( new Integer(iWidgetId) );
		String sProperties = widget.toCommand();
		String[] sTokens = sProperties.split("//s+", 3);
		widget.parseCommand( x + " " + y + " " + sTokens[2] );
		postInvalidate();
		
	}
	
	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i("BP", "OnDraw");
        
    	Object[] objArray = m_mWidgets.keySet().toArray();
    	int length = objArray == null ? 0 : objArray.length;
    	//reverse traverse
    	for( int i=0; i<length; i++ ){
    		Widget widget = m_mWidgets.get( objArray[i] );
    		widget.draw(canvas);
    	}
    }
	
	int downX;
	int downY;
	int moveX;
	int moveY;
	boolean bIsPicked = false;
	Widget targetWidget;
	int targetWidgetId; // move widget時會用到  [/move iWidgetId x y]
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		String sMsg;
        switch(event.getAction())
        {
	        case MotionEvent.ACTION_DOWN:
	        	Log.i("Down", (int)event.getX() + " " + (int)event.getY());
	        	downX = (int)event.getX();
	        	downY = (int)event.getY();
	        	
	        	if( m_bMenuItemSelected ){
	        		// 增加一個新的Widget
					Class<?> cls;
					try {
						cls = Class.forName("widget." + m_sMenuItem);
						Constructor<?> ct = cls.getConstructor();
						Widget widget = (Widget)ct.newInstance();
						String sProperties = widget.toCommand();
						String[] sTokens = sProperties.split("\\s+", 3);
						sMsg = "/post " + m_sMenuItem + " " + downX + " " + downY + " " + sTokens[2];
						cmdDispatcher.send( sMsg );
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
	        		
					m_bMenuItemSelected = false;
	        	} else {
	        		// check 是否滑鼠有Touch到某個Widget

		        	Object[] objArray = m_mWidgets.keySet().toArray();
		        	int length = objArray == null ? 0 : objArray.length;
		        	//reverse traverse
		        	for( int i=length-1; i>=0; i-- ){
		        		Widget widget = m_mWidgets.get( objArray[i] );
		        		if( postBoard.amIAuthor( (Integer) objArray[i] ) && widget.isPicked( downX, downY ) ){
		        			Log.i("BP", "isPicked");
		        			bIsPicked = true;
		        			targetWidget = widget;
		        			targetWidgetId = (Integer) objArray[i];
		        			break;
		        		}
		        	}
	        	}
	        	return true;
	        case MotionEvent.ACTION_MOVE: 
	        	Log.i("Move", (int)event.getX() + " " + (int)event.getY());
	        	if( bIsPicked ){
		        	moveX = (int)event.getX();
		        	moveY = (int)event.getY();
		        	int diffX = moveX - downX;
		        	int diffY = moveY - downY;
		        	String sProperties = targetWidget.toCommand();
		        	String[] sTokens = sProperties.split("\\s+", 3);
		        	
		        	int oldX = Integer.valueOf( sTokens[0] );
		        	int oldY = Integer.valueOf( sTokens[1] );
		        	
		        	targetWidget.parseCommand( (oldX + diffX) + " " + (oldY + diffY) + " " + sTokens[2] );
		        	
		        	downX = moveX;
		        	downY = moveY;
		        	invalidate();
	        	}
	        	return true;
	        case MotionEvent.ACTION_OUTSIDE: return true;
	        case MotionEvent.ACTION_CANCEL:
	        	int cancelX = (int)event.getX();
	        	int cancelY = (int)event.getY();
	        	Log.i("Cancel", cancelX + " " + cancelY);
	        	
	        	if( bIsPicked ){
					sMsg = "/move " + targetWidgetId + " " + cancelX + " " + cancelY;
					cmdDispatcher.send( sMsg );
					bIsPicked = false;
	        	}
	        	
	        	return true;
	        case MotionEvent.ACTION_UP:
	        	int upX = (int)event.getX();
	        	int upY = (int)event.getY();
	        	Log.i("Up", upX + " " + upY);
	        	
	        	if( bIsPicked ){
					sMsg = "/move " + targetWidgetId + " " + upX + " " + upY;
					cmdDispatcher.send( sMsg );
					bIsPicked = false;
	        	}

	        	
	        	return true;
        }
        return false;
    }
	
}
