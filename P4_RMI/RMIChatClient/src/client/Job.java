package client;

import task.Task;

public class Job {
	Job( String sJobId, String sJobName, String sArgs, Task task )
	{
		m_sJobId = sJobId;
		m_sJobName = sJobName;
		m_sArgs = sArgs;
		m_task = task;
	}
	
	String m_sJobId;
	String m_sJobName;
	String m_sArgs;
	Task m_task;
}
