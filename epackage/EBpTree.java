package epackage;

// EMEs-aware B+-tree node
public class EBpTree {
	BpTreeNode root;

	public EBpTree(int key, Object obj)
	{
		this.root = new BpTreeNode(key, obj);
	}

	int minimum_key(BpTreeNode node)
	{
		if (node.is_leaf)
			return node.keys[0];
		else
			return minimum_key(node.nodes[0]);
	}

	void insert_node_to_node(BpTreeNode addnode, BpTreeNode node)
	{
		int i, key, tmpkey;

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

	void delete_node_from_node(BpTreeNode delnode, BpTreeNode node)
	{
		if (node.size == 0) return;
		if (node.nodes[0] == delnode) return;

		int i, key;

		key = minimum_key(delnode);
		for (i = 0; i < node.size-1; i++) {
			if (key <= node.keys[i]) {
				node.keys[i] = node.keys[i+1];
				node.nodes[i+1] = node.nodes[i+2];
				break;
			}
		}

		node.keys[i] = -1;
		node.nodes[i+1] = null;
		node.size--;
	}

	void delete_key_from_node(int key, BpTreeNode node)
	{
		if (node.size == 0) return;

		int i;
		for (i = 0; i < node.size-1; i++) {
			if (key <= node.keys[i]) {
				node.keys[i] = node.keys[i+1];
				node.nodes[i] = node.nodes[i+1];
				break;
			}
		}

		node.keys[i] = -1;
		node.nodes[i+1] = null;
		node.size--;
	}

	BpTreeNode search_node(int key)
	{
		int i;
		BpTreeNode node = this.root;

		while (!node.is_leaf) {
			for (i = 0; i < node.size; i++) {
				if (node.keys[i] > key) break;
			}

			node = node.nodes[i];
		}

		return node;
	}

	void insert_key_to_leaf(int key, Object obj, BpTreeNode node)
	{
		if (node.size == BpTreeNode.degree && !node.is_leaf) return;

		int i;
		for (i = node.size; i > 0 && key < node.keys[i-1]; i--) {
			node.keys[i] = node.keys[i-1];
			node.nodes[i] = node.nodes[i-1];
		}

		node.keys[i] = key;
		node.nodes[i] = new BpTreeNode(obj);
		node.size++;
	}

	// split node (right is node's child)
	void insert_node_split(BpTreeNode right, BpTreeNode node)
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

	void insert_leaf_split(int key, Object obj, BpTreeNode node)
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
				insert_key_to_leaf(key, obj, right);
				i++;
				continue;
			}

			insert_key_to_leaf(left.keys[i], left.nodes[i-1], right);
			delete_key_from_node(left.keys[i], left);
		}

		if (left.size == d/2)
			insert_key_to_leaf(key, obj, left);

		right.nodes[d] = left.nodes[d];
		left.nodes[d] = right;

		insert_nodes_to_node(left, right, left.parent);
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

		if (node.size < BpTreeNode.degree) {
			insert_key_to_leaf(key, obj, node);
		}
		else {
			insert_leaf_split(key, obj, node);
		}
	}
}
