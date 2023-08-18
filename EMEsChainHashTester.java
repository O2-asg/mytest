import epackage.EChainHash;

public class EMEsChainHashTester {
	public static void main(String[] args)
	{
		int NUM = 10;

		EChainHash ch = new EChainHash();
		int hashholder[] = new int[NUM];
		Object o[] = new Object[NUM];

		for (int i = 0; i < NUM; i++) {
			o[i] = new Object();
			hashholder[i] = o[i].hashCode();
			ch.hash_store(o[i], o[i].hashCode());
		}


		int cnt = 0;
		for (int i = 0; i < NUM; i++) {
			if (ch.hash_get(hashholder[i]) == o[i])
				cnt++;
		}

		if (cnt == NUM) System.out.println("test passed");
		else System.out.println("test failed: " + cnt + " is OK");
	}
}
