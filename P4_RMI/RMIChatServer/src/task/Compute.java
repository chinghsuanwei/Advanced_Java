package task;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Compute extends Remote{
	Object executeTask(Task t, String target) throws RemoteException;
}