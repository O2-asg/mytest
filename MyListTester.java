import mypackage.MyList;

public class MyListTester {
	public static void main(String[] args)
	{
		MyList lst = new MyList();
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
