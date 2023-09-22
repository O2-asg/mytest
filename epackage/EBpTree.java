package epackage;

// EMEs-aware B+-tree
// btni holds a node's addr, child(ren) addr, and leaf or not
public class EBpTree {
	BpTreeNode root;
	BpTreeNodeInfo btni;

	// EMEs can be occur here
	public EBpTree(int key, Object obj)
	{
		this.root = new BpTreeNode(key, obj);
		this.btni = new BpTreeNodeInfo(this.root, 1, true); // (node, size, is_leaf)
		this.btni.nodes[0] = this.root.nodes[0];
	}

	boolean is_brokenNode(BpTreeNode node)
	{
		// read/write check
		try
		{
			node.keys = node.keys;
			node.obj = node.obj;
			node.size = node.size;
			node.nodes = node.nodes;
			node.parent = node.parent;
			node.is_leaf = node.is_leaf;
			return false;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			return true;
		}
	}

	BpTreeNodeInfo search_NodeInfo(BpTreeNode node)
	{
		BpTreeNodeInfo ni = this.btni;

		try
		{
			while (ni != null) {
				if (ni.ownaddr == node)
					return ni;
				ni = ni.next;
			}
			return ni;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// ignore btni's error
			return null;
		}
	}

	BpTreeNode replaceNode(BpTreeNode node)
	{
		BpTreeNodeInfo ni = search_NodeInfo(node);
		int i;

		if (ni != null) {
			// common operation (leaf and mid-node)
			BpTreeNode newnode = new BpTreeNode();
			BpTreeNodeInfo parent_ni = search_NodeInfo(ni.parent);
			for (i = 0; i < ni.size; i++) {
				newnode.nodes[i] = ni.nodes[i];
			}
			newnode.size = ni.size;
			newnode.parent = ni.parent;
			newnode.is_leaf = ni.is_leaf;
			for (i = 0; i <= parent_ni.size; i++) {
				if (parent_ni.nodes[i] == node) {
					ni.parent.nodes[i] = newnode;
					if (parent_ni != null) parent_ni.nodes[i] = newnode;
					break;
				}
			}

			if (ni.is_leaf) { // leaf
				for (i = 0; i < ni.size; i++)
					newnode.keys[i] = newnode.nodes[i].size; // key is stored in this entry
				newnode.nodes[BpTreeNode.degree] = ni.nodes[BpTreeNode.degree];
			}
			else { // non leaf
				newnode.nodes[i] = ni.nodes[i];
				for (i = 0; i < ni.size; i++)
					newnode.keys[i] = minimum_key(newnode.nodes[i+1]);
			}
			ni.ownaddr = newnode;
			if (node == this.root) this.root = newnode;
			return newnode;
		}
		return null;
	}

	void addInfo(BpTreeNode node)
	{
		BpTreeNodeInfo newinfo = new BpTreeNodeInfo(node, node.size, node.is_leaf);

		int i;

		try
		{
			for (i = 0; i < node.size; i++) { // deep copy
				newinfo.nodes[i] = node.nodes[i];
			}

			if (node.is_leaf) newinfo.nodes[BpTreeNode.degree] = node.nodes[BpTreeNode.degree];
			else newinfo.nodes[i] = node.nodes[i]; // non leaf
			newinfo.parent = node.parent;

			newinfo.next = this.btni.next;
			this.btni.next = newinfo;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// fatal: node information to replace broken node
			// has not made yet
			System.out.println("fatal error: EMEs");
		}
	}

	void update_entry_Info(BpTreeNode node, boolean is_add)
	{
		BpTreeNodeInfo ni;
		int i;
		int end;

		try
		{
			ni = this.btni;
			while (ni != null) {
				if (ni.ownaddr == node) {
					if (is_add) end = node.size;
					else end = ni.size;
					for (i = 0; i < end; i++) {
						ni.nodes[i] = node.nodes[i];
					}
					if (node.is_leaf) ni.nodes[BpTreeNode.degree] = node.nodes[BpTreeNode.degree];
					else ni.nodes[i] = node.nodes[i]; // non leaf
					ni.size = node.size;
					ni.parent = node.parent;
					break;
				}
				ni = ni.next;
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// fatal: tree can be inconsistent state
			// e.g., the same entries can be at two nodes
			System.out.printf("fatal error: EMEs");
		}
	}

	void delInfo(BpTreeNode node)
	{
		BpTreeNodeInfo ni;

		try
		{
			ni = this.btni;
			if (ni.ownaddr == node) {
				this.btni = ni.next;
				return;
			}

			while (ni.next != null) {
				if (ni.next.ownaddr == node) {
					ni.next = ni.next.next;
					break;
				}
				ni = ni.next;
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// ignore btni's error
		}
	}

	// returns minimum key of the node
	int minimum_key(BpTreeNode node)
	{
		try
		{
			if (node.is_leaf)
				return node.keys[0];
			else
				return minimum_key(node.nodes[0]); // recursive call
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// EMEs can occur at
			//   - reading node.is_leaf
			//   - reading array head addr (keys[0])
			//   - reading array value (keys[i]) (->iaload)
			//   - reading array head addr (nodes[0])
			if (is_brokenNode(node)) {
				BpTreeNode newnode = replaceNode(node);
				return minimum_key(newnode); // try again!
			}
			return -1;
		}
	}

	// search and return node that contains key
	BpTreeNode search_node(int key)
	{
		int i;
		BpTreeNode node = this.root;

		try
		{
			while (!node.is_leaf) {
				for (i = 0; i < node.size; i++) {
					if (node.keys[i] > key) break;
				}

				node = node.nodes[i];
			}

			return node;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// EMEs can occur at
			//   - reading addr of this.root
			//   - reading node.entries
			//   - reading array value (keys[i])
			//   - reading array value (nodes[i])
			if (is_brokenNode(node)) {
				replaceNode(node);
				return search_node(key); // try again!
			}
			return null;
		}
	}

	// can't use for leaf insertion
	void insert_node_to_node(BpTreeNode addnode, BpTreeNode node)
	{
		int i, key, tmpkey;

		try
		{
			if (node.size == BpTreeNode.degree || node.is_leaf) {
				addnode.parent = node;
				update_entry_Info(addnode, true);
				return;
			}

			key = minimum_key(addnode);

			if (node.size == 0) {
				if (node.nodes[0] == null) {
					node.nodes[0] = addnode;
					addnode.parent = node;
					update_entry_Info(addnode, true);
					return;
				}

				tmpkey = minimum_key(node.nodes[0]);
				if (key < tmpkey) {
					node.nodes[1] = node.nodes[0];
					node.keys[0] = tmpkey;
					node.nodes[0] = addnode;
				} else {
					node.keys[0] = key;
					node.nodes[1] = addnode;
				}
				node.size++;
				addnode.parent = node;
				update_entry_Info(addnode, true);
				return;
			}

			for (i = node.size; i > 0 && key < node.keys[i-1]; i--) {
				node.keys[i] = node.keys[i-1];
				node.nodes[i+1] = node.nodes[i];
			}

			node.keys[i] = key;
			node.nodes[i+1] = addnode;
			node.size++;

			addnode.parent = node;
			update_entry_Info(addnode, true);
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// EMEs can occur at
			//   - reading node.entries
			//   - writing addnode.parent
			//   - reading/writing node.nodes[i] (value)
			//   - writing/reading array value (keys[i])
			if (is_brokenNode(addnode) || is_brokenNode(node)) {
				BpTreeNode new_addnode = replaceNode(addnode);
				BpTreeNode newnode = replaceNode(node);
				insert_node_to_node(new_addnode, newnode); // try again!
			}
		}
	}

	void delete_node_from_node(BpTreeNode delnode, BpTreeNode node)
	{
		try
		{
			if (node.size == 0) return;
			if (node.nodes[0] == delnode) return;

			int i, key;

			key = minimum_key(delnode);
			for (i = 0; i < node.size-1; i++) {
				if (key <= node.keys[i]) {
					node.keys[i] = node.keys[i+1];
					node.nodes[i+1] = node.nodes[i+2];
				}
			}

			node.keys[i] = -1;
			node.nodes[i+1] = null;
			node.size--;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// EMEs can occur at
			//   - reading/writing node.entries
			//   - reading/writeing array value (keys[i] or nodes[i])
			if (is_brokenNode(node)) {
				BpTreeNode newnode = replaceNode(node);
				delete_node_from_node(delnode, newnode);
			}
		}
	}

	void delete_key_from_leaf(int key, BpTreeNode node)
	{
		try
		{
			if (node.size == 0 || !node.is_leaf) return;

			int i;
			for (i = 0; i < node.size-1; i++) {
				if (key <= node.keys[i]) {
					node.keys[i] = node.keys[i+1];
					node.nodes[i] = node.nodes[i+1];
				}
			}

			node.keys[i] = -1;
			node.nodes[i] = null;
			node.size--;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// EMEs can occur at
			//   - reading/writing node.entries
			//   - reading/writing array value (keys[i] or nodes[i])
			if (is_brokenNode(node)) { // how to recover leaf key? (#TODO)
				replaceNode(node);
			}
		}
	}

	// objnode: node that contains only object information
	void insert_key_to_leaf(int key, BpTreeNode objnode, BpTreeNode node)
	{
		try
		{
			if (node.size == BpTreeNode.degree && !node.is_leaf) return;

			int i;
			for (i = node.size; i > 0 && key < node.keys[i-1]; i--) {
				node.keys[i] = node.keys[i-1];
				node.nodes[i] = node.nodes[i-1];
			}

			node.keys[i] = key;
			node.nodes[i] = objnode;
			node.size++;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// EMEs can occur at
			// the same place to delete_key_from_leaf
			if (is_brokenNode(node)) { // how to recover leaf key? (#TODO)
				replaceNode(node);
			}
		}
	}

	// split node (right is node's child)
	void insert_node_split(BpTreeNode right, BpTreeNode node)
	{
		try
		{
			BpTreeNode left_node, right_node;
			int right_minkey = minimum_key(right);
			int d = BpTreeNode.degree;

			left_node = node;
			right_node = new BpTreeNode();
			right_node.parent = node.parent;

			int i;
			for (i = left_node.size-1; right_node.size < d/2; i--) {
				if ((left_node.keys[i] < right_minkey) && !right_node.is_node_included(right)) {
					insert_node_to_node(right, right_node);
					i++;
					continue;
				}

				insert_node_to_node(left_node.nodes[i+1], right_node);
				delete_node_from_node(left_node.nodes[i+1], left_node);
			}

			if (left_node.size == ((d+1)/2 -1))
				insert_node_to_node(right, left_node);

			insert_nodes_to_node(left_node, right_node, left_node.parent);
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// EMEs can occur at
			//   - allocating new BpTreeNode (write access)
			//   - writing new node's parent
			//   - reading node.entries
			if (is_brokenNode(node)) {
				BpTreeNode newnode = replaceNode(node);
				insert_node_split(right, newnode);
			}
		}
	}

	void insert_nodes_to_node(BpTreeNode left, BpTreeNode right, BpTreeNode node)
	{
		try
		{
			int right_minkey = minimum_key(right);

			if (node == null) { // no parent node
	// register right & new root
				this.root = new BpTreeNode();
				this.root.keys[0] = right_minkey;
				this.root.size = 1;
				this.root.nodes[0] = left;
				this.root.nodes[1] = right;
				left.parent = this.root;
				right.parent = this.root;
				addInfo(this.root);
				addInfo(right);
				update_entry_Info(left, true);
				return;
			}

			if (node.size < BpTreeNode.degree) {
	// register right node's info
				insert_node_to_node(right, node);
				addInfo(right);
				update_entry_Info(node, true); // parent entry increase
				return;
			}

			if (node.size == BpTreeNode.degree) {
	// register new node's info
	// means right (& new node(non leaf) & new node.parent) <- registerd by recursive call
				insert_node_split(right, node);
				addInfo(right);
				update_entry_Info(left, false); // parent entry decreases
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(this.root)) {
				replaceNode(this.root);
				insert_nodes_to_node(left, right, null); // try again!
			}
		}
	}

	void insert_leaf_split(int key, BpTreeNode objnode, BpTreeNode node)
	{
		BpTreeNode left, right;
		int d = BpTreeNode.degree;

		try
		{
			left = node;
			right = new BpTreeNode();
			right.parent = node.parent;
			right.is_leaf = true;

			int i;
			for (i = left.size-1; right.size < (d+1)/2; i--) {
				if ((left.keys[i] < key) && !right.is_key_included(key)) {
					insert_key_to_leaf(key, objnode, right);
					i++;
					continue;
				}

				insert_key_to_leaf(left.keys[i], left.nodes[i], right);
				delete_key_from_leaf(left.keys[i], left);
			}

			if (left.size == d/2)
				insert_key_to_leaf(key, objnode, left);

			right.nodes[d] = left.nodes[d];
			left.nodes[d] = right;

			update_entry_Info(left, false); // left entry must be reduced
			insert_nodes_to_node(left, right, left.parent);
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(node)) {
				replaceNode(node);
				insert_leaf_split(key, objnode, node); // try again!
			}
		}
	}

	void rewrite_key(int before, int after, BpTreeNode node)
	{
		if (node == null) return;

		try
		{
			for (int i = 0; i < node.size; i++) {
				if (node.keys[i] == before) {
					node.keys[i] = after;
					break;
				}
			}

			rewrite_key(before, after, node.parent); // recursive call
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(node)) {
				BpTreeNode newnode = replaceNode(node);
				rewrite_key(before, after, newnode); // try again!
			}
		}
	}

	void delete_key_enough(int key, BpTreeNode node)
	{
		try
		{
			if (!node.is_leaf) return;

			int head_entry = node.keys[0];

			delete_key_from_leaf(key, node);

			if (key == head_entry) {
				rewrite_key(key, minimum_key(node), node.parent);
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(node)) {
				BpTreeNode newnode = replaceNode(node);
				delete_key_enough(key, newnode); // try again!
			}
		}
	}

	void merge_nodes(BpTreeNode node_a, BpTreeNode node_b)
	{
		if (node_a == null) return;
		if (node_a.nodes[(BpTreeNode.degree+1)/2] != null) return;

		int i = 0;
		try
		{
			while (node_b.nodes[i] != null) {
				insert_node_to_node(node_b.nodes[i], node_a);
				i++;
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			BpTreeNode new_a = replaceNode(node_a);
			BpTreeNode new_b = replaceNode(node_b);
			merge_nodes(new_a, new_b); // try again!
		}
	}

	void balance_tree(BpTreeNode node)
	{
		if (node.parent == null) { // root node
			if (node.nodes[1] != null) { // two or more sub-trees
				update_entry_Info(node, false); //entry decreases
			} else { // only one sub-tree
				this.root = node.nodes[0];
				delInfo(node);
			}
			return; // finish balancing
		}

		if (node.nodes[(BpTreeNode.degree)/2] != null) { // entries >= ROUNDUP((degree+1)/2)
			// node has enough entries
			update_entry_Info(node, false); // entry decreases
			return;
		} else {
			int i;
			BpTreeNode left = null;
			BpTreeNode right = null;
			for (i = 0; i <= node.parent.size; i++) {
				if (node.parent.nodes[i] == node) {
					if (i != 0) left = node.parent.nodes[i-1];
					if (i != node.parent.size) right = node.parent.nodes[i+1];
					break;
				}
			}
			if (left != null) {
				if (left.nodes[(BpTreeNode.degree+1)/2] != null) { // has enough entries
					insert_node_to_node(left.nodes[left.size], node);
					delete_node_from_node(left.nodes[left.size], left);
					rewrite_key(minimum_key(node.nodes[1]), minimum_key(node.nodes[0]), node.parent);
					update_entry_Info(left, false); // entry decreases
					update_entry_Info(node, true); // entry increases
					return;
				}
			}
			if (right != null) {
				if (right.nodes[(BpTreeNode.degree+1)/2] != null) { // has enough entries
					insert_node_to_node(right.nodes[0], node);
					for (i = 0; i < right.size; i++) {
						right.keys[i] = right.keys[i+1];
						right.nodes[i] = right.nodes[i+1];
					}
					right.nodes[i] = null;
					right.size--;
					rewrite_key(minimum_key(node.nodes[node.size]), minimum_key(right.nodes[0]), node.parent);
					update_entry_Info(right, false); // entry decreases
					update_entry_Info(node, true); // entry increases
					return;
				}
			}

			// merge
			if (left != null) {
				merge_nodes(left, node);
				delete_node_from_node(node, node.parent);
				update_entry_Info(left, true); // entry increase
				delInfo(node);
				balance_tree(node.parent);
			} else if (right != null) {
				// implies node is leftmost child of node.parent
				merge_nodes(node, right);
				delete_node_from_node(right, node.parent);
				update_entry_Info(node, true); // entry increases
				delInfo(right);
				balance_tree(node.parent);
			}
		}
	}

	// merge leaf_b entries into leaf_a
	// leaf_b will be removed
	void merge_leaves(BpTreeNode leaf_a, BpTreeNode leaf_b)
	{
		if (leaf_a == null || leaf_b == null) return;

		try
		{
			if (leaf_a.parent != leaf_b.parent) return;
			if (leaf_a.size + leaf_b.size > BpTreeNode.degree) return;

			int i;
			for (i = 0; i < leaf_b.size; i++) {
				insert_key_to_leaf(leaf_b.keys[i], leaf_b.nodes[i], leaf_a);
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			BpTreeNode new_a = replaceNode(leaf_a);
			BpTreeNode new_b = replaceNode(leaf_b);
			merge_leaves(new_a, new_b); // try again!
		}
	}

	void delete_key_not_enough(int key, BpTreeNode node)
	{
		if (!node.is_leaf) return;

		if (key == node.keys[0])
			rewrite_key(node.keys[0], node.keys[1], node.parent);
		delete_key_from_leaf(key, node);
		update_entry_Info(node, false); // entry decreases

		// right leaf check
		BpTreeNode right = node.nodes[BpTreeNode.degree];
		if (right != null && node.parent == right.parent) {
			if (right.size > (BpTreeNode.degree+1)/2) { // enough entries
				insert_key_to_leaf(right.keys[0], right.nodes[0], node);
				delete_key_from_leaf(right.keys[0], right);
				rewrite_key(node.keys[node.size-1], right.keys[0], node.parent);
				update_entry_Info(node, true); // entry increases
				update_entry_Info(right, false); //entry decreases
				return;
			}
		}

		// left leaf check
		BpTreeNode left = null;
		int i;
		for (i = 0; i <= node.parent.size; i++)
			if (i != 0 && node.parent.nodes[i] == node)
				left = node.parent.nodes[i-1];
		if (left != null && left.parent == node.parent) {
			if(left.size > (BpTreeNode.degree+1)/2) {
				insert_key_to_leaf(left.keys[left.size-1], left.nodes[left.size-1], node);
				delete_key_from_leaf(left.keys[left.size-1], left);
				rewrite_key(node.keys[1], node.keys[0], node.parent);
				update_entry_Info(node, true); // entry increases
				update_entry_Info(left, false); // entry decreases
				return;
			}
		}

		// merge
		BpTreeNode p = node.parent;
		if (left != null && left.parent == node.parent) { // merge into left leaf
			left.nodes[BpTreeNode.degree] = right;
			merge_leaves(left, node);
			delete_node_from_node(node, p);
			update_entry_Info(left, true); // entry increases
			delInfo(node);
			balance_tree(p);
		} else if (right != null) { // merge into node
		// implies node == node.parent.nodes[0]
			node.nodes[BpTreeNode.degree] = right.nodes[BpTreeNode.degree];
			merge_leaves(node, right);
			delete_node_from_node(right, p);
			update_entry_Info(node, true); // entry increases
			delInfo(right);
			balance_tree(p);
		}
	}

	void print_node(BpTreeNode node)
	{
		int i;
		for (i = 0; i < node.size; i++) {
			System.out.printf("%d(index:%d), ", node.keys[i], i);
		}
		System.out.println("");
	}

	void p(BpTreeNode node)
	{
		try
		{
			if (node == null) return ;
			if (!node.is_leaf) System.out.printf("parent => ");
			print_node(node);

			for (int i = 0; i <= node.size; i++) {
				if (node.nodes[i] != null && !node.is_leaf) {
					p(node.nodes[i]);
				}
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(node)) {
				replaceNode(node);
			}
			System.out.println("EMEs: stopped printing");
			return;
		}
	}

	public void p(){ p(this.root); }
	public void treeinfo_p()
	{
		BpTreeNodeInfo ni;
		ni = this.btni;
		int i;

		while (ni != null) {
			if (!ni.is_leaf) {
				System.out.println("parent => "+ni.ownaddr);
				for (i = 0; i <= ni.size; i++)
					System.out.println("child => "+ni.nodes[i]+" "+ni.nodes[i].keys[0]);
			}
			else System.out.println("leaf => "+ni.ownaddr+" "+ni.ownaddr.keys[0]);
			ni = ni.next;
		}
	}

	public void insert(int key, Object obj)
	{
		BpTreeNode node = search_node(key);

		try
		{
			if (node.is_key_included(key)) return;

			BpTreeNode insertnode = new BpTreeNode(obj);
			insertnode.size = key; // this entry is used to recover leafnode key

			if (node.size < BpTreeNode.degree) {
				insert_key_to_leaf(key, new BpTreeNode(obj), node);
				update_entry_Info(node, true);
			} else {
				insert_leaf_split(key, new BpTreeNode(obj), node);
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(node)) {
				BpTreeNode newnode = replaceNode(node);
				insert(key, newnode); // try again!
			}
		}
	}

	public void delete(int key)
	{
		BpTreeNode node = search_node(key);

		try
		{
			if (!node.is_key_included(key)) return;

			if (node == this.root) {
				delete_key_from_leaf(key, node);
				update_entry_Info(node, false); // entry decreases
			} else if (node.size > (BpTreeNode.degree+1)/2) {
				delete_key_enough(key, node);
				update_entry_Info(node, false); // entry decreases
			} else {
				delete_key_not_enough(key, node);
			}
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(node)) {
				replaceNode(node);
				delete(key); // try again!
			}
		}
	}

	public Object get_object(int key)
	{
		BpTreeNode node = search_node(key);
		try
		{
			if (node == null) return null;
			for (int i = 0; i < node.size; i++) {
				if (node.keys[i] == key) return node.nodes[i].obj;
			}
			return null;
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			if (is_brokenNode(node)) {
				replaceNode(node);
			}
			return null;
		}
	}
}
