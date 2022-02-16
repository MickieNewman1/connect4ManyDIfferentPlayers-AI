@SuppressWarnings("serial")

/**
 * A custom exception for when a player is out of time for calculating their next move.
 * 
 * @author Daniel Szafir
 *
 */
public class TimeUpException
	extends Exception
{
	public TimeUpException(String s)
	{
		super(s);
	}

	public TimeUpException()
	{
	}
}
