import epackage.EList_v2;

public class EMEsList_v2Tester {
	public static void main(String[] args)
	{
		int num = 1000;
		Object o[] = new Object[num];
		double million = 1000000;
		long start, end;
		int h[] = new int[num];

		for (int i = 0; i < num; i++) {
			o[i] = new Object();
			h[i] = o[i].hashCode();
		}

		EList_v2 lst = new EList_v2(o[0], h[0]);

		for (int i = 1; i < num; i++) {
			lst.addNode(o[i], h[i]);
		}

		start = System.nanoTime();
		for (int i = 1; i < num; i++) {
			lst.delNode(h[i]);
		}
		end = System.nanoTime();

		System.out.println((end-start)/million); // ms
	}
}
