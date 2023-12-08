package mypackage;

public class MyList {
	ListNode head;

	public MyList()
	{
		this.head = new ListNode();
	}

	public void addNode(Object obj, int hash)
	{
		ListNode newnode = new ListNode(obj, hash);

		newnode.next = this.head.next;
		this.head.next = newnode;
	}

	public void delNode(int hash)
	{
		ListNode n = this.head;
		ListNode target = null;

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

	public void showList()
	{
		ListNode n = this.head;

		while (n != null) {
			if (n == this.head) {
				n = n.next;
				continue;
			}
			System.out.printf("Object %s, hashCode is %x\n", n.obj.toString(), n.hash);
			n = n.next;
		}
	}

	public Object getObject(int hash)
	{
		ListNode n = this.head;

		n = n.next;

		while (n != null) {
			if (n.hash == hash)
				return n.obj;
			n = n.next;
		}

		return null;
	}
}
