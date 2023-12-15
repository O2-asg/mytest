import mypackage.MyChainHash;

public class MyChainHashTester {
	public static void main(String[] args)
	{
		int NUM = 100000;
		int i;
		int h[] = new int[NUM];

		MyChainHash ch = new MyChainHash();
		Object o[] = new Object[NUM];
		long start, end;
		double million = 1000000.0;

		for (i = 0; i < NUM; i++) {
			o[i] = new Object();
			h[i] = o[i].hashCode();
			ch.hash_store(o[i], h[i]);
		}

		start = System.nanoTime();
		for (i = 0; i < NUM; i++) {
			ch.hash_delete(h[i]);
		}
		end = System.nanoTime();

		System.out.println((end-start)/million); // ms
	}
}
