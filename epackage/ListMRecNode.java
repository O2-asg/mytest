package epackage;

class ListMRecNode {
	int hash;
//	Object o; // essential for recovering original node
	ListNode nextnode;
	ListMRecNode next;

	ListMRecNode(ListNode nextnode, int hash)
	{
		this.nextnode = nextnode;
		this.hash = hash;
		this.next = null;
	}
}
