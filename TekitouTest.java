class TekitouTest {
	public static void main(String[] args)
	{
		Object o = new Object();
		Object tmp = o.clone();

		System.out.printf("original: %x, clone: %x\n", o.hashCode(), tmp.hashCode());
	}
}
