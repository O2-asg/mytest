package epackage;

class ListMRecRList {
	ListMRecRNode head;

	ListMRecRList(ListNode nextnode, Object obj, int key, int hash)
	{
		this.head = new ListMRecRNode(nextnode, obj, key, hash);
	}

	void addNode(ListNode nextnode, Object obj, int key, int hash)
	{
		ListMRecRNode newnode = new ListMRecRNode(nextnode, obj, key, hash);

		newnode.next = this.head;
		this.head = newnode;
	}

	// deletes record of broken (or to be deleted) ListNode and
	// returns brokenNode.next
	// hash is brokenNode.hashCode()
	ListMRecRNode delNode(int hash)
	{
		ListMRecRNode ret = null;

		if (this.head.hash == hash) { // this.head holds the info
			ret = this.head;
			this.head = this.head.next;
			return ret;
		}

		ListMRecRNode n = this.head;

		while (n.next != null) {
			if (n.next.hash == hash) { // find the info
				ret = n.next;
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
		ListMRecRNode n = this.head;

		while (n != null) {
			if (n.hash == hash) {
				n.nextnode = nextnode;
				return;
			}
			n = n.next;
		}
	}
}
