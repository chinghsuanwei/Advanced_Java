package client;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import editor.BaseEditor;

import widgets.Widget;

public class PropertyHandlerFrame extends JFrame implements ActionListener {
	
	Widget widget;
	String sOldWidgetConfigure;
	
	
	CommandDispatcher cmdDispatcher;
	JButton okButton;
	JButton cancelButton;
	int iWidgetId;
	boolean bCreated;
	
	PropertyHandlerFrame( CommandDispatcher cmdDispatcher, Widget widget, int iWidgetId ){
		this.widget = widget;
		// 將Old Widget的Configure記起來，如果之後按下cancel鍵，就回復先前的設定
		sOldWidgetConfigure = widget.toCommand();
		this.cmdDispatcher = cmdDispatcher;
		this.iWidgetId = iWidgetId;
		
		if( iWidgetId < 0 ) this.bCreated = false;
		else this.bCreated = true;
		
		initialize();
	}
	
	void initialize(){
		setTitle("Editor");
		setPreferredSize( new Dimension(400, 400) );
		setLocation(200, 200);
		
		Container container = getContentPane();
		JPanel editorPanel = new JPanel();
		JScrollPane editorScrollPane = new JScrollPane( editorPanel );
		
		GridLayout gridLayout = new GridLayout();
		editorPanel.setLayout( gridLayout );
		container.add( editorScrollPane );
		
		BeanInfo beanInfo = null;
		
		try {
			beanInfo = Introspector.getBeanInfo( widget.getClass() );
			//下面這行可只抓到Interface中的function
			//beanInfo = Introspector.getBeanInfo( widget.getClass(), Widget.class);
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 1 for ok and cancel button
		int nRowCount = 1;
		Vector<Component> vComponents = new Vector<Component>();
		
		for( PropertyDescriptor pd : beanInfo.getPropertyDescriptors() ){
			String property_name = pd.getName();
			Method get_method = pd.getReadMethod();
			Method set_method = pd.getWriteMethod();
			
			try {
				JLabel propertyLabel = new JLabel( property_name );
				// get_method 可能為null?
				if( get_method == null || set_method == null ) continue;
				
				Object value = get_method.invoke( widget );
				Class returnType = get_method.getReturnType();
				
				String sValueType = returnType.getSimpleName();				
				//System.out.println( sValueType );
				//System.out.flush();
				
				if( sValueType.equals("int") || sValueType.equals("Integer" ) || 
					sValueType.equals("float") || sValueType.equals("Float") || 
					sValueType.equals("boolean") || sValueType.equals("Boolean") ||
					sValueType.equals("String") ) {
					
					JTextField inputTextField = new JTextField( );
					EditorTextFieldListener editorTextFieldListener =  new EditorTextFieldListener( inputTextField, widget, sValueType, set_method );
					
					// When textfield lose focus, then set the current value of it.
					// Or when type key of enter, then set the current value of it.
					inputTextField.addFocusListener( editorTextFieldListener  );
					inputTextField.addKeyListener( editorTextFieldListener );
					if( value != null ) inputTextField.setText( value.toString() );
					
					nRowCount++;
					vComponents.add( propertyLabel );
					vComponents.add( inputTextField );
				}
				else{
					JButton editorButton = new JButton();
					
					String sEditorClass = sValueType + "Editor";
					
					try{
						Class editorClass = Class.forName("editor." + sEditorClass );
						Constructor constructor = editorClass.getConstructor();
						Object editor = constructor.newInstance();
						EditorButtonActionListener editorButtonActionListener = new EditorButtonActionListener( (BaseEditor)editor, widget, value, pd.getWriteMethod() );
						editorButton.addActionListener(  editorButtonActionListener );
						editorButton.setText( sEditorClass );
						nRowCount++;
						vComponents.add( propertyLabel );
						vComponents.add( editorButton );
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		gridLayout.setRows( nRowCount );
		gridLayout.setColumns( 2 );
		
		for( int i=0; i<vComponents.size(); i++ ) editorPanel.add( vComponents.get(i) );
		
		okButton = new JButton("ok");
		okButton.addActionListener( this );
		cancelButton = new JButton("cancel");
		cancelButton.addActionListener( this );
		editorPanel.add( okButton );
		editorPanel.add( cancelButton );
		
		pack();
		setVisible( true );
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if( e.getSource() == okButton ){
			if( bCreated ) cmdDispatcher.changeWidget( iWidgetId, widget );
			else cmdDispatcher.postWidget( widget );
			dispose(); // close Frame
		} else if( e.getSource() == cancelButton ){
			
			//如果是已被created 出來的widget，則要回覆先前的設定 & 重繪
			if( bCreated ){
				widget.parseCommand( sOldWidgetConfigure );
				widget.getParent().revalidate();
			}
			dispose();
		}
	}
}

class EditorTextFieldListener implements FocusListener, KeyListener{
	
	String sValueType;
	String sOldValue;
	Method set_method;
	Widget widget;
	JTextField inputTextField;
	
	EditorTextFieldListener( JTextField inputTextField, Widget widget, String sValueType, Method set_method ){
		this.sValueType = sValueType;
		this.inputTextField = inputTextField;
		this.set_method = set_method;
		this.widget = widget;
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		sOldValue = inputTextField.getText();
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
		setValue();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
        	setValue();
        }
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * set value of current textfield.
	 */
	private void setValue(){
		Object[] paramObjs = new Object[1];
		try{
			if( sValueType.equals("int") || sValueType.equals("Integer") ){
				paramObjs[0] = Integer.valueOf( inputTextField.getText() );
			} else if( sValueType.equals("float") || sValueType.equals("Float") ){
				paramObjs[0] = Float.valueOf( inputTextField.getText() );
			} else if( sValueType.equals("boolean") || sValueType.equals("Boolean") ){
				paramObjs[0] = Boolean.valueOf( inputTextField.getText() );
			} else if( sValueType.equals("String") ){
				paramObjs[0] = inputTextField.getText();
			}
		} catch ( NumberFormatException e ) {
			// recover to old value, because of NumberFormatException.
			inputTextField.setText( sOldValue );
			return;
		}
		
		try {
			//if( set_method == null ) System.out.println("set_method is null.");
			set_method.invoke(widget, paramObjs);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}

class EditorButtonActionListener implements ActionListener
{
	BaseEditor baseEditor;
	Widget widget;
	Object curValue;
	Method set_method;
	
	EditorButtonActionListener( BaseEditor baseEditor, Widget widget, Object value, Method set_method ){
		this.baseEditor = baseEditor;
		this.widget = widget;
		this.curValue = value;
		this.set_method = set_method;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		Thread thread = new Thread() {
            public void run() {
            	curValue = baseEditor.returnValue(curValue);
            	Object[] paramObjs = {curValue};

                //呼叫方法
                try {
					set_method.invoke(widget, paramObjs);
					widget.repaint();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //呼叫setName方法
            }
		};
		thread.start();
	}
	
}