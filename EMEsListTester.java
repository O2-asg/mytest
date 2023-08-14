import epackage.EList;

/*class ECCuncorrectableMemoryError extends Error {
// should extend VirtualMachineerror?
}

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

// EMEs-aware list
class EList {
	ListNode head;
	NodeManager nm;

	EList()
	{
		this.head = new ListNode();
		this.nm = new NodeManager(this.head, null);
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
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			return true; // broken node
		}
		return false; // not broken node
	}

	// add new entry to NodeManager
	void addEntry(ListNode n, ListNode n_next)
	{
		try
		{
			NodeManager newentry = new NodeManager(n, n_next);
			newentry.next = this.nm.next;
			this.nm.next = newentry;
		}
		// ignore manager's error
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("addEntry failed");
		}
	}

	// delete entry of NodeManager
	void delEntry(ListNode n)
	{
		try
		{
			NodeManager m = this.nm;

			// preventry is the previous entry of argument node's entry.
			// preventry.next will be deleted.
			// prevnode_entry is the entry of previous node of argument node.
			// preventry.next_ndaddr will be overwritten.
			NodeManager preventry = null, prevnode_entry = null;

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
		// ignore manager's error
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("delEntry failed");
		}
	}

	// get previous node from manager
	ListNode getPrevNode(ListNode n)
	{
		try
		{
			NodeManager m = this.nm;

			if (n == this.head) return null; // previous node of head node doesn't exist

			while (m != null) {
				if (m.next_ndaddr == n)
					return m.ndaddr;
				m = m.next;
			}

		}
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("getPrevNode failed");
		}
		// previous node not found or EMEs
		finally
		{
			return null;
		}
	}

	// get next node from manager
	ListNode getNextNode(ListNode n)
	{
		try
		{
			NodeManager m = this.nm;

			while (m != null) {
				if (m.ndaddr == n)
					return m.next_ndaddr;
				m = m.next;
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			System.out.println("getNextNode failed");
		}
		// next node not found or EMEs
		finally
		{
			return null;
		}
	}

	// replace broken head-node of list
	void replaceHead()
	{
		try
		{
			ListNode newhead = new ListNode();
			ListNode next = getNextNode(this.head);

			if (next != null) {
				newhead.next = next;
				this.head = newhead;
				this.nm.ndaddr = this.head; // update entry
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
	void addNode(Object obj, int hash)
	{
		try
		{
			ListNode newnode = new ListNode(obj, hash);

			newnode.next = this.head.next; // set next
			this.head.next = newnode; // insert
			addEntry(newnode, newnode.next);
			this.nm.next_ndaddr = newnode;

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
			// if NodeManager is broken, do nothing
		}
	}

	// delete node from EMEs-aware list
	// key is hashCode
	void delNode(int hash)
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
	void showList()
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
}*/

public class EMEsListTester {
	public static void main(String[] args)
	{
		EList lst = new EList();
		Object o;
		int h = 0;

		for (int i = 0; i < 11; i++) {
			o = new Object();
			lst.addNode(o, o.hashCode());
			if (i == 5) h = o.hashCode();
		}

		lst.showList();
		System.out.println("-----------------------------------");
		lst.delNode(h);
		lst.showList();
	}
}
