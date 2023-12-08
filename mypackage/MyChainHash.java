package mypackage;

public class MyChainHash {
	public static final int BUCKET_SIZE = 1021;

	MyList tbl[];

	public MyChainHash()
	{
		this.tbl = new MyList[BUCKET_SIZE];
	}

	public void hash_store(Object obj, int hashcode)
	{
		int idx = hashcode % BUCKET_SIZE;

		if (this.tbl[idx] == null) {
			this.tbl[idx] = new MyList();
		}

		this.tbl[idx].addNode(obj, hashcode);
	}

	public void hash_delete(int hashcode)
	{
		int idx = hashcode % BUCKET_SIZE;

		if (this.tbl[idx] == null)
			return;

		this.tbl[idx].delNode(hashcode);
	}

	public Object hash_get(int hashcode)
	{
		int idx = hashcode % BUCKET_SIZE;

		if (this.tbl[idx] == null)
			return null;

		return this.tbl[idx].getObject(hashcode);
	}
}
