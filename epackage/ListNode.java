package epackage;

// EMEs-aware list node
// obj and hash can be any data
class ListNode {
	Object obj;
	int hash;
	ListNode next;

	// constructor
	ListNode(Object obj, int hash)
	{
		this.obj = obj;
		this.hash = hash;
		next = null;
	}

	// constructor for head of list
	ListNode()
	{
		this.obj = null;
		this.hash = 0;
		next = null;
	}
}
