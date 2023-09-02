package epackage;

// EMEs-aware B+-tree
public class EBpTree {
	BpTreeNode root;

	public EBpTree(int key, Object obj)
	{
		this.root = new BpTreeNode(key, obj);
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
		}
	}

	// search and return node that (may) contain key
	// used to find not only node,
	// but also left next leaf
	BpTreeNode search_node(int key)
	{
		int i;

		try
		{
			BpTreeNode node = this.root;

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
			//   - reading node.is_leaf
			//   - reading node.size
			//   - reading array addr (keys[0])
			//   - reading array value (keys[i])
			//   - reading array addr (nodes[0])
			//   - reading array value (nodes[i])
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
			return;
		}

		key = minimum_key(addnode);

		if (node.size == 0) {
			if (node.nodes[0] == null) {
				node.nodes[0] = addnode;
				addnode.parent = node;
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
		}
		catch (ECCuncorrectableMemoryError eme)
		{
			// EMEs can occur at
			//   - reading node.size or node.is_leaf
			//   - writing addnode.parent
			//   - reading/writing node.nodes[i] (value)
			//   - reading array addr (nodes[0])
			//   - reading array addr (keys[0])
			//   - writing/reading array value (keys[i])
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
			//   - reading/writing node.size
			//   - reading array addr (keys[0] or nodes[0])
			//   - reading/writeing array value (keys[i] or nodes[i])
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
			//   - reading node.is_leaf
			//   - reading/writing node.size
			//   - reading array addr (keys[0] or nodes[0])
			//   - reading/writing array value (keys[i] or nodes[i])
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
			//   - reading node.parent, node.size
			//   - reading array addr (nodes[0])
		}
	}

	void insert_nodes_to_node(BpTreeNode left, BpTreeNode right, BpTreeNode node)
	{
		int right_minkey = minimum_key(right);

		if (node == null) { // no parent node
			this.root = new BpTreeNode();
			this.root.keys[0] = right_minkey;
			this.root.size = 1;
			this.root.nodes[0] = left;
			this.root.nodes[1] = right;
			left.parent = this.root;
			right.parent = this.root;
			return;
		}

		if (node.size < BpTreeNode.degree) {
			insert_node_to_node(right, node);
			return;
		}

		if (node.size == BpTreeNode.degree) {
			insert_node_split(right, node);
		}
	}

	void insert_leaf_split(int key, BpTreeNode objnode, BpTreeNode node)
	{
		BpTreeNode left, right;
		int d = BpTreeNode.degree;

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

		insert_nodes_to_node(left, right, left.parent);
	}

	void rewrite_key(int before, int after, BpTreeNode node)
	{
		if (node == null) return;

		for (int i = 0; i < node.size; i++) {
			if (node.keys[i] == before) {
				node.keys[i] = after;
				break;
			}
		}

		rewrite_key(before, after, node.parent); // recursive call
	}

	void delete_key_enough(int key, BpTreeNode node)
	{
		if (!node.is_leaf) return;

		int head_entry = node.keys[0];

		delete_key_from_leaf(key, node);

		if (key == head_entry) {
			rewrite_key(key, minimum_key(node), node.parent);
		}
	}

	void merge_nodes(BpTreeNode node_a, BpTreeNode node_b)
	{
		if (node_a == null || node_a.has_enough_entry_node()) return;

		int i = 0;
		while (node_b.nodes[i] != null) {
			insert_node_to_node(node_b.nodes[i], node_a);
			i++;
		}
	}

	void balance_tree(BpTreeNode node)
	{
		if (node.parent == null) { // root node
			if (node.nodes[1] != null) { // two or more sub-trees
				// do nothing
			} else { // only one sub-tree
				this.root = node.nodes[0];
			}
			return; // finish balancing
		}

		if (node.nodes[(BpTreeNode.degree)/2] != null) { // entries >= ROUNDUP((degree+1)/2)
			// node has enough entries
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
				if (left.has_enough_entry_node()) {
					insert_node_to_node(left.nodes[left.size], node);
					delete_node_from_node(left.nodes[left.size], left);
					rewrite_key(minimum_key(node.nodes[1]), minimum_key(node.nodes[0]), node.parent);
					return;
				}
			}
			if (right != null) {
				if (right.has_enough_entry_node()) {
					insert_node_to_node(right.nodes[0], node);
					for (i = 0; i < right.size; i++) {
						right.keys[i] = right.keys[i+1];
						right.nodes[i] = right.nodes[i+1];
					}
					right.nodes[i] = null;
					right.size--;
					rewrite_key(minimum_key(node.nodes[node.size]), minimum_key(right.nodes[0]), node.parent);
					return;
				}
			}

			// merge
			if (left != null) {
				merge_nodes(left, node);
				delete_node_from_node(node, node.parent);
				balance_tree(node.parent);
			} else if (right != null) {
				// implies node is leftmost child of node.parent
				merge_nodes(node, right);
				delete_node_from_node(right, node.parent);
				balance_tree(node.parent);
			}
		}
	}

	// merge leaf_b entries into leaf_a
	// leaf_b will be removed
	void merge_leaves(BpTreeNode leaf_a, BpTreeNode leaf_b)
	{
		if (leaf_a == null || leaf_b == null) return;
		if (leaf_a.parent != leaf_b.parent) return;
		if (leaf_a.size + leaf_b.size > BpTreeNode.degree) return;

		int i;
		for (i = 0; i < leaf_b.size; i++) {
			insert_key_to_leaf(leaf_b.keys[i], leaf_b.nodes[i], leaf_a);
		}
	}

	void delete_key_not_enough(int key, BpTreeNode node)
	{
		if (!node.is_leaf) return;

		if (key == node.keys[0])
			rewrite_key(node.keys[0], node.keys[1], node.parent);
		delete_key_from_leaf(key, node);

		// right leaf check
		BpTreeNode right = node.nodes[BpTreeNode.degree];
		if (right != null && node.parent == right.parent) {
			if (right.has_enough_entry_leaf()) {
				insert_key_to_leaf(right.keys[0], right.nodes[0], node);
				delete_key_from_leaf(right.keys[0], right);
				rewrite_key(node.keys[node.size-1], right.keys[0], node.parent);
				return;
			}
		}

		// left leaf check
		BpTreeNode left = search_node(node.keys[0]-1);
		if (left != null && left.parent == node.parent) {
			if(left.has_enough_entry_leaf()) {
				insert_key_to_leaf(left.keys[left.size-1], left.nodes[left.size-1], node);
				delete_key_from_leaf(left.keys[left.size-1], left);
				rewrite_key(node.keys[1], node.keys[0], node.parent);
				return;
			}
		}

		// merge
		int i;
		BpTreeNode p = node.parent;
		if (left != null && left.parent == node.parent) { // merge into left leaf
			left.nodes[BpTreeNode.degree] = right;
			merge_leaves(left, node);
			delete_node_from_node(node, p);
			balance_tree(p);
		} else if (right != null) { // merge into right leaf
		// implies node == node.parent.nodes[0]
			node.nodes[BpTreeNode.degree] = right.nodes[BpTreeNode.degree];
			merge_leaves(node, right);
			delete_node_from_node(right, p);
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
		if (node == null) return ;
		if (!node.is_leaf) System.out.printf("parent => ");
		print_node(node);

		for (int i = 0; i <= node.size; i++) {
			if (node.nodes[i] != null && !node.is_leaf) {
				p(node.nodes[i]);
			}
		}
	}

	public void p(){ p(this.root); }

	public void insert(int key, Object obj)
	{
		BpTreeNode node = search_node(key);

		if (node.is_key_included(key)) return;

		if (node.size < BpTreeNode.degree) {
			insert_key_to_leaf(key, new BpTreeNode(obj), node);
		} else {
			insert_leaf_split(key, new BpTreeNode(obj), node);
		}
	}

	public void delete(int key)
	{
		BpTreeNode node = search_node(key);

		if (!node.is_key_included(key)) return;

		if (node == this.root) {
			delete_key_from_leaf(key, node);
		} else if (node.has_enough_entry_leaf()) {
			delete_key_enough(key, node);
		} else {
			delete_key_not_enough(key, node);
		}
	}

	public Object get_object(int key)
	{
		BpTreeNode node = search_node(key);
		if (node == null) return null;

		for (int i = 0; i < node.size; i++) {
			if (node.keys[i] == key) return node.nodes[i].obj;
		}
		return null;
	}
}
