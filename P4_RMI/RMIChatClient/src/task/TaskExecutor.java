package task;
import java.io.FileDescriptor;
import java.io.Serializable;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import task.*;

public class TaskExecutor implements Serializable, Compute{
	
	@Override
	public Object executeTask(Task t, String target) throws RemoteException {
		// TODO Auto-generated method stub
		return t.execute();
	}

}
