package epackage;

// m-rec for linked list
// initialized with original EList
class ListMRec {
	ListMRecList tbl[];

	public static final int BUCKET_SIZE = 1021;

	private int hashToIndex(int hash)
	{
		return hash % BUCKET_SIZE;
	}

	// hash is EList.head.hashCode()
	ListMRec(int hash)
	{
		int idx = hashToIndex(hash);

		this.tbl = new ListMRecList[BUCKET_SIZE];
		this.tbl[idx] = new ListMRecList(null, hash);
	}

	void add_rec(ListNode node, ListNode nextnode)
	{
		int hash = node.hashCode();
		int idx = hashToIndex(hash);

		if (this.tbl[idx] == null) {
			this.tbl[idx] = new ListMRecList(nextnode, hash);
		} else {
			this.tbl[idx].addNode(nextnode, hash);
		}
	}

	// removes record of broken (or deleted) ListNode and
	// returns next node of broken node
	// hash is brokenNode.hashCode()
	ListNode remove_rec(ListNode node)
	{
		int hash = node.hashCode();
		int idx = hashToIndex(hash);

		if (this.tbl[idx] == null) { // error
			return null;
		}

		ListNode nextnode = this.tbl[idx].delNode(hash); // delete m-rec

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
