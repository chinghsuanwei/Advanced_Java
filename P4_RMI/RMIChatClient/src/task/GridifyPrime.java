package task;

import java.util.Vector;

public class GridifyPrime implements Task{
	
	long prime;
	long min, max;
	
	//Return a vector of tasks
	public Vector<GridifyPrime> primeMapper( int num )
	{
		Vector<GridifyPrime> vGridifyPrime = new Vector<GridifyPrime>();
		long range = max - min + 1;
		long quotient = range / num;
		long remainder = range % num;
		
		long tmpMin = min;
		for( int i=0; i<num; i++ ){
			long reducedMin = tmpMin;
			long reducedMax = tmpMin + quotient - 1;
			
			if( remainder > 0 ){
				// 如果有剩的餘數 則前面的Task都會幫忙分擔1
				reducedMax++;
				remainder--;
			}
			
			GridifyPrime gridifyPrime = new GridifyPrime();
			String init_str = prime + " " + reducedMin + " " + reducedMax;
			gridifyPrime.init( init_str );
			vGridifyPrime.add( gridifyPrime );
			
			tmpMin = reducedMax + 1;
		}
		
		return vGridifyPrime;
	}
	
	//Collect a vector of mapped results and reduce them to on result
	public Long primeReducer( Vector<Long> mapped_results ) 
	{
		for( Long l : mapped_results ){
			if( l > 1 ) return l;
		}
		
		return new Long(1);
	}
	
	
	@Override
	@Gridify( mapper = "primeMapper", reducer="primeReducer")
	public Object execute() 
	{
		//Test for rmi.
		//DisplayResultForTA.displayUsingWidget("StringWidget", (int)min, (int)max, "100 20 #ffffff #000000 " +  DisplayResultForTA.getUsername() );
		// TODO Auto-generated method stub
		for( long l=min; l<=max ; l++ ){
			// 找大於1 和 小於 Prime 間的因數
			if( l > 1 && l < prime && (prime % l) == 0 ) return new Long(l);
		}
		return new Long(1);
	}

	@Override
	public void init(String init_str) 
	{
		// arg = "value min-range max-range"
		String sTokens[] = init_str.split("\\s+", 3);
		prime = Integer.valueOf( sTokens[0] );
		min = Integer.valueOf( sTokens[1] );
		max = Integer.valueOf( sTokens[2] );
		
		if( min > max ){
			long tmp = min;
			min = max;
			max = tmp;
		}
		
		if( max > prime ) max = prime;
	}
	
}
