import epackage.EChainHash;

public class EMEsChainHashTester {
	public static void main(String[] args)
	{
		int NUM = 100000;
		int i;
		int h[] = new int[NUM];
		long start, end;
		double million = 1000000.0;

		EChainHash ch = new EChainHash();
		Object o[] = new Object[NUM];

		for (i = 0; i < NUM; i++) {
			o[i] = new Object();
			h[i] = o[i].hashCode();
			ch.hash_store(h[i], o[i]);
		}

		start = System.nanoTime();
		for (i = 0; i < NUM; i++) {
			ch.hash_delete(h[i]);
		}
		end = System.nanoTime();

		System.out.println((end-start)/million); // ms
	}
}
