package task;
import client.CommandDispatcher;

public class DisplayResultForTA 
{
	
	public static String sUsername;
	public static CommandDispatcher cmdDispatcher;
	
	public static String getUsername()
	{
		return sUsername;
	}
	
	public static void displayUsingWidget(String widget, int x, int y, String args )
	{
		cmdDispatcher.sendByTA( "/post " + widget + " " + x + " " + y + " " + args );
	}
}
