import epackage.EBpTree;

class EMEsBpTreeTester {
	public static void main(String[] args)
	{
/*		Object o = new Object();
		EBpTree t = new EBpTree(o.hashCode(), o);

		for (int i = 0; i < 10; i++) {
			o = new Object();
			t.insert(o.hashCode(), o);
		}*/

		EBpTree t = new EBpTree(37, null);
		t.insert(33, null);
		t.insert(28, null);
		t.insert(41, null);
		t.insert(1, null);
		t.insert(22, null);
		t.insert(35, null);
		t.insert(38, null);
		t.insert(42, null);
		t.insert(11, null);
		t.insert(14, null);
		t.insert(25, null);
		t.insert(46, null);
		t.insert(2, null);
		t.insert(47, null);
		t.insert(15, null);
		t.insert(17, null);

		t.p();
//		t.treeinfo_p();
	}
}
