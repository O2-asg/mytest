package epackage;

// EMEs-aware list
public class EList_v2 {
	ListNode head;
	ListMRec mr;

	// EMEs can occur here
	public EList_v2(Object value, int key)
	{
		this.head = new ListNode(value, key);
		this.mr = new ListMRec(this.head.hashCode());
	}

	private EList_v2(EList_v2 original)
	{
		this.head = original.head;
		this.mr = original.mr;
	}

	public EList_v2 cloneInstance()
	{
		return new EList_v2(this);
	}

	// check ListNode (broken or not)
	// (i.e., can read/write)
	boolean is_brokenNode(ListNode node)
	{
		if (node == null) return false;

		try
		{
			// read/write check
			node.obj = node.obj;
			node.hash = node.hash;
			node.next = node.next;

			return false; // not broken
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			return true; // broken
		}
	}

	// add new record to Recorder
	void addEntry(ListNode node, ListNode nextnode)
	{
		if (node == null) return; // error

		try
		{
			this.mr.add_rec(node, nextnode);
		}
		// ignore Recorder's error
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("addEntry failed: broken m-rec");
		}
	}

	// delete record of target
	// also rewrites record of previous ListNode of target
	ListNode delEntry(ListNode target)
	{
		if (target == this.head) return null;
		if (target == null) return null;

		ListNode ret = null;

		try
		{
			ret = this.mr.remove_rec(target);
			return ret; // returns target.next
		}
		// ignore Recorder's error
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("delEntry failed: broken m-rec");
			return null;
		}
	}

	void updateEntry(ListNode node, ListNode nextnode)
	{
		try
		{
			this.mr.update_rec(node, nextnode);
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("updateEntry failed: broken m-rec");
		}
	}

	// deicard broken head node of list
	// since head is broken, we can't access this.head.next
	void discardHead()
	{
		ListNode newhead = null;
		try
		{
			// remove_rec returns next ListNode
			newhead = this.mr.remove_rec(this.head);
			this.head = newhead;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("unknown error");
		}
	}

	// internal deletion function
	// get node.next from m-rec
	void delete_node_from_list(ListNode target)
	{
		if (target == this.head) {
			discardHead();
			return;
		}

		ListNode n = this.head;
		ListNode nextnode = null;

		try
		{
			while (n != null) {
				if (n.next == target) {
					nextnode = delEntry(target);
					updateEntry(n, nextnode);
					n.next = nextnode;
					break;
				}
				n = n.next;
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(n)) { // found broken node before target
				delete_node_from_list(n); // recursive call
				delete_node_from_list(target); // try again!
			} else {  } // ignore

			return;
		}
	}

	// add node to EMEs-aware list
	public void addNode(Object value, int key)
	{
		ListNode newnode = null, oldhead = this.head;

		try
		{
			// normal operation
			newnode = new ListNode(value, key);

			newnode.next = oldhead; // set next to old head
			this.head = newnode; // set head to newnode

			// update m-rec
			addEntry(newnode, oldhead);
		}
		catch (ECCuncorrectableMemoryError eme)	// at newnode
		{
			// broken newnode
			if (is_brokenNode(newnode)) {
				addNode(value, key); // try again!
				return;
			}
		}
	}

	// delete node from EMEs-aware list
	public void delNode(int key)
	{
		ListNode n = this.head, prev = this.head;

		try
		{
			// normal operation
			if (this.head.hash == key) {
				this.head = this.head.next;
				delEntry(n);
				return;
			}

			n = n.next;
			while (n != null) {
				if (n.hash == key) {
					break;
				}
				prev = n;
				n = n.next;
			}

			if (n != null) {
				prev.next = n.next;
				delEntry(n);
				updateEntry(prev, n.next);
			}
		}
		catch (ECCuncorrectableMemoryError eme) // at this.head or any other node
		{
			// error when R/W this.head.next
			if (is_brokenNode(this.head)) {
				discardHead();
				delNode(key); // try again!
			} else if (is_brokenNode(n)) { // error with node n
				delete_node_from_list(n);
				delNode(key); // try again!
			} else if (is_brokenNode(prev)) { // error when writing to prev.next
				delete_node_from_list(prev);
				delNode(key); // try again!
			} else { /* ignore other EMEs */ }

			return;
		}
	}

	// show EMEs-aware list
	public void showList()
	{
		ListNode n = this.head;
		try
		{
			// normal operation
			while (n != null) {
				System.out.printf("Object %s, hashCode is %x\n", n.obj.toString(), n.hash);
				n = n.next;
			}
		}
		catch (ECCuncorrectableMemoryError eme) // at this.head or any other node
		{
			if (is_brokenNode(this.head)) { // error when reading this.head.next
				discardHead();
				showList(); // try again!
			} else if (is_brokenNode(n)) {
				delete_node_from_list(n);
				System.out.println("showList() aborted: caught EME");
				System.out.println("sorry, some information has lost");
			} else { /* ignore other EME */ }

			return;
		}
	}

	// search and get object from given hashcode
	public Object getObject(int key)
	{
		ListNode n = this.head;

		try
		{
			// normal operation
			while (n != null) {
				if (n.hash == key)
					return n.obj;
				n = n.next;
			}

			return null;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(this.head)) {
				discardHead();
				return getObject(key); // try again!
			} else if (is_brokenNode(n)) {
				delete_node_from_list(n);
				return null;
			} else { /* ignore other EME */ }

			return null;
		}

	}
}
