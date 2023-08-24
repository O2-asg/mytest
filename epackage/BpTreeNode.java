package epackage;

class BpTreeNode {
	public static final int degree = 3;
	int keys[];
	Object obj;
	int size;
	BpTreeNode nodes[];
	BpTreeNode parent;
	boolean is_leaf;

	BpTreeNode()
	{
		this.keys = new int[degree];
		this.obj = null;
		this.size = 0;
		this.nodes = new BpTreeNode[degree+1];
		this.parent = null;
		this.is_leaf = false;
	}

	BpTreeNode(Object obj)
	{
		this.obj = obj;
	}

	BpTreeNode(int key, Object obj)
	{
		this.keys = new int[degree];
		this.keys[0] = key;
		this.nodes = new BpTreeNode[degree+1];
		this.nodes[0] = new BpTreeNode(obj);
		this.size = 1;
		this.parent = null;
		this.is_leaf = true;
	}

	boolean is_key_included(int key)
	{
		for (int i = 0; i < this.size; i++) {
			if (this.keys[i] == key) return true;
		}

		return false;
	}

	boolean is_node_included(BpTreeNode target)
	{
		for (int i = 0; i < this.size+1; i++) {
			if (this.nodes[i] == target) return true;
		}

		return false;
	}

	// check if a leaf has enough entry (key) to delete
	boolean has_enough_entry_leaf()
	{
		if (this.is_leaf && (this.size > (degree+1)/2))
			return true;
		else return false;
	}

	// check if a node (not leaf) has enough entry to delete
	boolean has_enough_entry_node()
	{
		if (!this.is_leaf && this.nodes[(degree+1)/2] != null)
			return true;
		else return false;
	}
}
