package MRR_Solution;

class ListNode {
    int val;
    ListNode next;
    ListNode(int x) { val = x; }
 }
class Solution {
  public boolean isPalindrome(ListNode head) {
    if (head == null || head.next == null) {
      return true;
    }

    ListNode prev = null;
    ListNode slow = head;
    ListNode fast = head;

    while (fast != null && fast.next != null) {
      fast = fast.next.next;
      System.out.println(fast.val+"fast");
      System.out.println(slow.val+"slow");
      ListNode next = slow.next;
      slow.next = prev;
      prev = slow;
      slow = next;
    }

    if (fast != null) {
      slow = slow.next;
    }
    while (slow != null) {
      if (slow.val != prev.val) {
        return false;
      }
      slow = slow.next;
      prev = prev.next;
    }

    return true;
  }
  public static void main(String[] args) {
	Solution s = new Solution();
	ListNode l =new ListNode(1);
	l.next=new ListNode(2);
	l.next.next=new ListNode(3);
	l.next.next.next=new ListNode(2);
	l.next.next.next.next=new ListNode(1);
    System.out.println(s.isPalindrome(l));
}
}