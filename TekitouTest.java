class TekitouTest {
	public static void main(String[] args)
	{
		int n = 14;

		try
		{
			n++;
			if (n%3 == 0)
				throw new Exception();
		}
		catch (Exception e)
		{
			System.out.println("n is "+n);
		}
	}
}
