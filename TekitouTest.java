class TekitouTest {
	public static void main(String[] args)
	{
		long start = System.nanoTime();
		int i;
		double nanotoone = 1000000000.0;

		for (i = 0; i < 100000; i++)
			;
		long end = System.nanoTime();
		System.out.println("time is "+(end-start)/nanotoone);
	}
}
