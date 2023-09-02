import epackage.EBpTree;

class EMEsBpTreeTester {
	public static void main(String[] args)
	{
		Object o = new Object();
		EBpTree t = new EBpTree(o.hashCode(), o);

		for (int i = 0; i < 10; i++) {
			o = new Object();
			t.insert(o.hashCode(), o);
		}

		t.p();
	}
}
