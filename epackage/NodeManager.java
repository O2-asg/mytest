package epackage;

// used for list-node discard (skip)
// an entry holds a list-node's address (itself?), the next list-node address,
// node infomation (head or not)
class NodeManager {
	ListNode ndaddr;
	ListNode next_ndaddr;
	NodeManager next;

	// constructor for an entry
	NodeManager(ListNode n, ListNode n_next)
	{
		this.ndaddr = n;
		this.next_ndaddr = n_next;
		this.next = null;
	}
}
