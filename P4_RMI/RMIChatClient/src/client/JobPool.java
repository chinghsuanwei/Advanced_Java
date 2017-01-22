package client;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class JobPool {
	
	JobPool( )
	{
		m_mJobs = new TreeMap<String, Job>();
	}
	
	boolean add( Job job, boolean bOne )
	{
		if ( isIdUsed( job.m_sJobId ) ) return false;
		
		m_mJobs.put(job.m_sJobId, job);
		return true;
	}
	
	Job get( String sJobId )
	{
		return m_mJobs.get( sJobId );
	}
	
	boolean isIdUsed( String sJobId )
	{
		return m_mJobs.get( sJobId ) != null;
	}
	
	Map<String, Job> getJobs()
	{
		return m_mJobs;
	}
	
	Map<String, Job> m_mJobs;
}
