package epackage;

class ListMRecList {
	ListMRecNode head;

	ListMRecList(ListNode nextnode, int hash)
	{
		this.head = new ListMRecNode(nextnode, hash);
	}

	void addNode(ListNode nextnode, int hash)
	{
		ListMRecNode newnode = new ListMRecNode(nextnode, hash);

		newnode.next = this.head;
		this.head = newnode;
	}

	// deletes record of broken (or to be deleted) ListNode and
	// returns brokenNode.next
	// hash is brokenNode.hashCode()
	ListNode delNode(int hash)
	{
		ListNode ret = null;

		if (this.head.hash == hash) { // this.head holds the info
			ret = this.head.nextnode;
			this.head = this.head.next;
			return ret;
		}

		ListMRecNode n = this.head;

		while (n.next != null) {
			if (n.next.hash == hash) { // find the info
				ret = n.next.nextnode; // get next to repair data structure
				n.next = n.next.next; // delete record
				return ret;
			}
			n = n.next;
		}

		/* should not reach here */
		return ret; // null
	}

	// after deletion of ListNode, connection has changed
	// must update connection
	void updateNextNode(ListNode nextnode, int hash)
	{
		ListMRecNode n = this.head;

		while (n != null) {
			if (n.hash == hash) {
				n.nextnode = nextnode;
				return;
			}
			n = n.next;
		}
	}
}
