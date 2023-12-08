import mypackage.MyList;

public class MyListTester {
	public static void main(String[] args)
	{
		MyList lst = new MyList();
		Object o;
		int h = 0;

		for (int i = 0; i < 11; i++) {
			o = new Object();
			lst.addNode(o, o.hashCode());
			if (i == 5) h = o.hashCode();
		}

		lst.showList();
		System.out.println("----------------------");
		lst.delNode(h);
		lst.showList();
	}
}
