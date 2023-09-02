package epackage;

// EMEs-aware list
public class EList {
	ListNode head;
	ConnectionRecorder cr;

	// EMEs can be occur here
	public EList()
	{
		this.head = new ListNode();
		this.cr = new ConnectionRecorder(this.head, null);
	}

	private EList(EList original)
	{
		this.head = original.head;
		this.cr = original.cr;
	}

	public EList cloneInstance()
	{
		return new EList(this);
	}

	// check if node is broken
	// (i.e., can read/write)
	boolean is_brokenNode(ListNode node)
	{
		try
		{
			// read/write check
			node.obj = node.obj;
			node.hash = node.hash;
			node.next = node.next;

			return false; // not broken node
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			return true; // broken node
		}
	}

	// add new entry to Recorder
	void addEntry(ListNode n, ListNode n_next)
	{
		try
		{
			ConnectionRecorder newentry = new ConnectionRecorder(n, n_next);
			newentry.next = this.cr.next;
			this.cr.next = newentry;
		}
		// ignore Recorder's error
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("addEntry failed");
		}
	}

	// delete entry of Recorder
	void delEntry(ListNode n)
	{
		try
		{
			ConnectionRecorder m = this.cr;

			// preventry is the previous entry of argument node's entry.
			// preventry.next will be deleted.
			// prevnode_entry is the entry of previous node of argument node.
			// preventry.next_ndaddr will be overwritten.
			ConnectionRecorder preventry = null, prevnode_entry = null;

			while (m.next != null) {
				if (m.next.ndaddr == n) // found preventry
					preventry = m;
				if (m.next.next_ndaddr == n) // found prevnode_entry
					prevnode_entry = m.next;
				if (preventry != null && prevnode_entry != null) break;
				m = m.next;
			}

			if (preventry != null && prevnode_entry != null) {
				// overwrite next node's address of entry
				prevnode_entry.next_ndaddr = preventry.next.next_ndaddr;

				// delete(skip) argument node's entry
				preventry.next = preventry.next.next;
			}
		}
		// ignore Recorder's error
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("delEntry failed");
		}
	}

	// get previous node from Recorder
	// only used for node discard (skip)
	ListNode getPrevNode(ListNode n)
	{
		try
		{
			ConnectionRecorder m = this.cr;

			if (n == this.head) return null; // previous node of head node doesn't exist

			while (m != null) {
				if (m.next_ndaddr == n) // found
					return m.ndaddr;
				m = m.next;
			}

			return null; // not found

		}
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("getPrevNode failed");
			return null;
		}
	}

	// get next node from Recorder
	// only used for node discard (skip)
	ListNode getNextNode(ListNode n)
	{
		try
		{
			ConnectionRecorder m = this.cr;

			while (m != null) {
				if (m.ndaddr == n) // found
					return m.next_ndaddr;
				m = m.next;
			}

			return null; // not found
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("getNextNode failed");
			return null;
		}
	}

	// replace broken head node of list
	// since head is broken, accessing head.next is dangerous
	void replaceHead()
	{
		try
		{
			ListNode newhead = new ListNode();
			ListNode next = getNextNode(this.head); // avoid accessing head.next

			if (next != null) {
				newhead.next = next;
				this.head = newhead;
				this.cr.ndaddr = this.head; // update entry
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("head replacement failed");
		}
	}

	// skip broken node and
	// delete entry of broken node
	void skip_brokenNode(ListNode broken_node)
	{
		ListNode prevnode = getPrevNode(broken_node);
		ListNode nextnode = getNextNode(broken_node);
		if (prevnode != null && nextnode != null) {
			prevnode.next = nextnode; // skip
			delEntry(broken_node); // delete entry
		}
	}

	// add node to EMEs-aware list
	public void addNode(Object obj, int hash)
	{
		try
		{
			ListNode newnode = new ListNode(obj, hash);

			newnode.next = this.head.next; // set next
			this.head.next = newnode; // insert
			addEntry(newnode, newnode.next);
			this.cr.next_ndaddr = newnode;
		}
		// caught EME(s) during addNode
		catch (ECCuncorrectableMemoryError eme)
		{
			// broken head
			if (is_brokenNode(this.head)) {
				replaceHead();
				addNode(obj, hash); // try again!
			}
			// if newnode is broken, then abort
			// if Recorder is broken, do nothing
		}
	}

	// delete node from EMEs-aware list
	// key is hashCode
	public void delNode(int hash)
	{
		ListNode n = this.head;
		ListNode target = null;

		try
		{
			while (n.next != null) {
				if (n.next.hash == hash) {
					target = n.next;
					break;
				}
				n = n.next;
			}

			if (target != null) {
				n.next = target.next;
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// error when reading this.head -> head error
			if (is_brokenNode(this.head)) {
				replaceHead();
				delNode(hash); // try again!
				return;
			}

			// error when reading (writing) n -> n error
			if (is_brokenNode(n))
				skip_brokenNode(n);

			// error when reading target (n.next) -> target error
			if (target != null && is_brokenNode(target))
				skip_brokenNode(target);
		}
	}

	// show EMEs-aware list
	public void showList()
	{
		ListNode n = this.head;
		try
		{
			while (n != null) {
				if (n == this.head) { // head node doesn't contain data
					n = n.next;
					continue;
				}

				System.out.printf("Object %s, hashCode is %x\n", n.obj.toString(), n.hash);
				n = n.next;
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(this.head)) {
				replaceHead();
				showList(); // try again!
				return;
			}
			if (is_brokenNode(n))
				skip_brokenNode(n);

			System.out.println("showList() stopped: caught EMEs");
		}
	}

	// search and get object from given hashcode
	public Object getObject(int hash)
	{
		ListNode n = this.head;

		try
		{
			n = n.next;

			while (n != null) {
				if (n.hash == hash)
					return n.obj;
				n = n.next;
			}

			return null;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(this.head)) {
				replaceHead();
				return getObject(hash); // try again!
			}
			if (is_brokenNode(n)) {
				skip_brokenNode(n);
				return null;
			}
			return null;
		}

	}
}
