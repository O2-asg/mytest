package epackage;

// EMEs-aware chain hash
public class EChainHash {
	// size can be tunable
	// should be a prime number?
	public static final int BUCKET_SIZE = 1021;

	// EMEs might occur when accessing tbl[idx]
	// two tables must be at other places
	EList tbl[];
	EList backup_tbl[];

	// EMEs can be occur here
	public EChainHash()
	{
		this.tbl = new EList[BUCKET_SIZE];
		this.backup_tbl = new EList[BUCKET_SIZE];
	}

	// broken table check
	boolean is_brokenArray(EList lst[])
	{
		try
		{
			for (int i = 0; i < BUCKET_SIZE; i++)
				lst[i] = lst[i];

			return false; // not broken
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			return true; // broken
		}
	}

	// create a new table from argument
	EList[] reallocate_table(EList lst[])
	{
		EList ret[] = null;

		try
		{
			ret = new EList[BUCKET_SIZE];

			for (int i = 0; i < BUCKET_SIZE; i++) {
				if (lst[i] != null)
					ret[i] = lst[i].cloneInstance();
			}

			return ret;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenArray(lst)) return null;
			if (is_brokenArray(ret)) return reallocate_table(lst); // try again!

			return null;
		}
	}

	// store new key and value
	public void hash_store(Object obj, int hashcode)
	{
		int idx = hashcode % BUCKET_SIZE;

		try
		{
			if (this.tbl[idx] == null) {
				this.tbl[idx] = new EList();
				this.backup_tbl[idx] = this.tbl[idx].cloneInstance();
			}

			this.tbl[idx].addNode(obj, hashcode);
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenArray(this.tbl)) {
				this.tbl = reallocate_table(this.backup_tbl);
				this.backup_tbl = reallocate_table(this.backup_tbl);
			}
		}
	}

	// delete key and value using key
	public void hash_delete(int hashcode)
	{
		int idx = hashcode % BUCKET_SIZE;

		try
		{
			if (this.tbl[idx] == null)
				return;

			this.tbl[idx].delNode(hashcode);
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenArray(this.tbl)) {
				this.tbl = reallocate_table(this.backup_tbl);
				this.backup_tbl = reallocate_table(this.backup_tbl);
			}
		}
	}

	// get object from hashcode (key)
	public Object hash_get(int hashcode)
	{
		int idx = hashcode % BUCKET_SIZE;

		try
		{
			if (this.tbl[idx] == null)
				return null;

			return this.tbl[idx].getObject(hashcode);
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenArray(this.tbl)) {
				this.tbl = reallocate_table(this.backup_tbl);
				this.backup_tbl = reallocate_table(this.backup_tbl);
				return hash_get(hashcode); // try again!
			}

			return null;
		}
	}
}
