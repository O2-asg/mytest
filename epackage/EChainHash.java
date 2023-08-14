package epackage;

public class EChainHash {
	// size can be tunable
	// should be a prime number?
	public static final int BUCKET_SIZE = 1021;
	EList tbl[];
	EList backup_tbl[];

	public EChainHash()
	{
		this.tbl = new EList[BUCKET_SIZE];
	}

	public void hash_store(Object obj, int hashcode)
	{
		int idx = hashcode % BUCKET_SIZE;

		if (this.tbl[idx] == null) {
			this.tbl[idx] = new EList();
		}

		this.tbl[idx].addNode(obj, hashcode);
	}

	public void hash_delete(int hashcode)
	{
		int idx = hashcode % BUCKET_SIZE;
		EList lst = this.tbl[idx];

		if (lst == null)
			return;

		lst.delNode(hashcode);

	}

	public Object hash_get(int hashcode)
	{
		int idx = hashcode % BUCKET_SIZE;
		EList lst = this.tbl[idx];

		if (lst == null)
			return null;

		return lst.getObject(hashcode);
	}
}
