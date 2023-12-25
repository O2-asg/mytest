package epackage;

// m-rec for linked list
// initialized with original EList
class ListMRecR {
	ListMRecRList tbl[];

	public static final int BUCKET_SIZE = 1021;

	private int hashToIndex(int hash)
	{
		return hash % BUCKET_SIZE;
	}

	// hash is EList.head.hashCode()
	ListMRecR(int hash, Object obj, int key)
	{
		int idx = hashToIndex(hash);

		this.tbl = new ListMRecRList[BUCKET_SIZE];
		this.tbl[idx] = new ListMRecRList(null, obj, key, hash);
	}

	void add_rec(ListNode node, ListNode nextnode, Object obj, int key)
	{
		int hash = node.hashCode();
		int idx = hashToIndex(hash);

		if (this.tbl[idx] == null) {
			this.tbl[idx] = new ListMRecRList(nextnode, obj, key, hash);
		} else {
			this.tbl[idx].addNode(nextnode, obj, key, hash);
		}
	}

	// removes record of broken (or deleted) ListNode and
	// returns next node of broken node
	// hash is brokenNode.hashCode()
	ListMRecRNode remove_rec(ListNode node)
	{
		int hash = node.hashCode();
		int idx = hashToIndex(hash);

		if (this.tbl[idx] == null) { // error
			return null;
		}

		ListMRecRNode nextnode = this.tbl[idx].delNode(hash); // delete m-rec

		return nextnode;
	}

	// updates record of prev
	void update_rec(ListNode prev, ListNode nextnode)
	{
		int idx = hashToIndex(prev.hashCode());

		if (this.tbl[idx] == null) return;

		this.tbl[idx].updateNextNode(nextnode, prev.hashCode());
	}
}
