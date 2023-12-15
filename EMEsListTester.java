import epackage.EList;

public class EMEsListTester {
	public static void main(String[] args)
	{
		EList lst = new EList();
		int num = 10000;
		Object o[] = new Object[num];
		double million = 1000000;
		long start, end;
		int h[] = new int[num];

		for (int i = 0; i < num; i++) {
			o[i] = new Object();
			h[i] = o[i].hashCode();
			lst.addNode(o[i], h[i]);
		}

		start = System.nanoTime();
		for (int i = 0; i < num; i++) {
			lst.delNode(h[i]);
		}
		end = System.nanoTime();

		System.out.println((end-start)/million); // ms
	}
}
