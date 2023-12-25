package epackage;

class ListMRecRNode {
	int hash;
	int key; // original node info
	Object o; // essential for recovering original node
	ListNode nextnode;
	ListMRecRNode next;

	ListMRecRNode(ListNode nextnode, Object obj, int key, int hash)
	{
		this.nextnode = nextnode;
		this.o = obj;
		this.key = key;
		this.hash = hash;
		this.next = null;
	}
}
