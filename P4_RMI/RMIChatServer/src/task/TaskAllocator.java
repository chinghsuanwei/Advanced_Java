package task;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Vector;

import server.UserInfo;
import server.User;

public class TaskAllocator implements Serializable, Compute{
	public TaskAllocator( UserInfo userInfo ) throws RemoteException {
        Compute stub = (Compute) UnicastRemoteObject.exportObject(this, 0);
        registry = LocateRegistry.createRegistry( 2001 );
        //registry = LocateRegistry.getRegistry( 2001 );
        registry.rebind("@SERVER", stub);
        this.userInfo = userInfo;
	}
	
	@Override
	public Object executeTask(Task t, String target) throws RemoteException {
		// TODO Auto-generated method stub
		Class taskClass = t.getClass();
		Method execMethod = null;
		try {
			execMethod = taskClass.getMethod("execute");
			
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( execMethod.isAnnotationPresent( Gridify.class ) ){
			//Gridify Task
			
			if( target != null ){
				if( target.equals("@SERVER") ) return t.execute();
				else{
					try {
						Compute compute = (Compute)registry.lookup(target);
						return compute.executeTask(t, null);
					} catch (NotBoundException e) {
						//如果target不存在的話，則Server自己執行Task
						
						return t.execute();
					}
				}				
			}
            
            try {
            	Gridify gridify = execMethod.getAnnotation( Gridify.class );
            	Method mapper = taskClass.getMethod(gridify.mapper(), Integer.TYPE );
            	Method reducer = taskClass.getMethod(gridify.reducer(), Vector.class);
            	
				Vector<User> vOnlineUsers = userInfo.getOnlineUsers();
				
				// mapper 將Task 分成 小subTask
				Object vMappedTask = mapper.invoke(t, vOnlineUsers.size() );
				Class cMapperTask = vMappedTask.getClass();
				// 看Mapper 分成幾個 subTask 就找幾個Client 來做
				Method sizeMethod = cMapperTask.getMethod("size");
				int size = (Integer) sizeMethod.invoke( vMappedTask );
				
				Method getMethod = cMapperTask.getMethod( "get", Integer.TYPE );
				Vector vResults = new Vector();
				
				ArrayList<ParallelProcess> aParallelProcess = new ArrayList<ParallelProcess>();
				for( int i=0; i<size; i++ ){
					User user = vOnlineUsers.get(i);
					// Vector.get( int index )
					Task subTask = (Task)getMethod.invoke(vMappedTask, i);
					Compute compute = (Compute)registry.lookup( user.m_sUsername );
					
					// Parallel processing
					ParallelProcess parallelProcess = new ParallelProcess( compute, subTask, vResults ) ;
					parallelProcess.start();
					aParallelProcess.add( parallelProcess );
				}
				
				// wait all process terminated
				for( ParallelProcess p : aParallelProcess ) p.join();
				
				// reducer 將結果整合
				return reducer.invoke(t, vResults);
				
            } catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            return null;
		} else {
			//Normal Task
			
			if( target == null || target.equals("@SERVER") ) return t.execute();
			else{
				try {
					Compute compute = (Compute)registry.lookup(target);
					return compute.executeTask(t, null);
				} catch (NotBoundException e) {
					//如果target不存在的話，則Server自己執行Task
					
					return t.execute();
				}
			}			
		}


	}
	
	UserInfo userInfo;
	Registry registry; // rmi registry for lookup the remote objects.
}

class ParallelProcess implements Runnable{
    Compute compute;
    Task subTask;
    Vector vResults;
    Thread thread;
    ParallelProcess(Compute compute, Task subTask, Vector vResults){
    	this.compute = compute;
    	this.subTask = subTask;
    	this.vResults = vResults;
    	thread = new Thread(this);
    }
    
    public void start(){
    	thread.start();
    }
    
    public void join(){
    	try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	public void run() {
		try {
			vResults.add( compute.executeTask( subTask, null ) );
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
};