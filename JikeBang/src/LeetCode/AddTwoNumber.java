package Test;

public class AddTwoNumber {
    public static class ListNode{
        int val;
        ListNode next;
        ListNode(int x){
            this.val=x;
        }
    }
//    public ListNode addTwoNumber(ListNode l1,ListNode l2){
//        ListNode p =new ListNode(0);
//        ListNode head=p;
//        boolean flag_1=false;
//        boolean flag_2=false;
//        while(l1 != null && l2 !=null){
//            if(l1.next ==null){flag_1=true;}
//            if(l2.next == null){flag_2=true;}
//            p.val+=l1.val+l2.val;
//            if(p.val >= 10){
//                p.val-=10;
//                p.next=new ListNode(1);
//                p=p.next;
//            }
//            else if(flag_1==false || flag_2==false){
//                p.next=new ListNode(0);
//                p=p.next;}
//            l1=l1.next;
//            l2=l2.next;
//        }
//        while(l1 != null){
//            p.val+=l1.val;
//            if(p.val >= 10){
//                p.val=p.val%10;
//                p.next=new ListNode(1);
//                p=p.next;
//                l1=l1.next;
//            }
//            else {
//                p.next = l1.next;
//                break;
//            }
//        }
//        while(l2 != null){
//            p.val+=l2.val;
//            if(p.val >= 10){
//                p.val=p.val%10;
//                p.next=new ListNode(1);
//                p=p.next;
//                l2=l2.next;
//            }
//            else {
//                p.next = l2.next;
//                break;
//            }
//        }
//        return head;
//    }
    public ListNode addTwoNumber(ListNode l1,ListNode l2) {
        ListNode p = l1;
        ListNode q = l2;
        ListNode head = new ListNode(0);
        ListNode run = head;
        int flag = 0;
        while (p != null || q != null) {
            int p_val = (p != null) ? p.val : 0;
            int q_val = (q != null) ? q.val : 0;
            int sum = p_val + q_val + flag;
            flag = sum / 10;
            run.next = new ListNode(sum%10);
            run = run.next;
            if (p != null) {
                p = p.next;
            }
            if (q != null) {
                q = q.next;
            }
        }
        if(flag >0){run.next=new ListNode(flag);}
        return head.next;
    }
    public static void main(String[] args){
        AddTwoNumber atn = new AddTwoNumber();
        ListNode l1 =new ListNode(9);
        l1.next=new ListNode(1);
        l1.next.next=new ListNode(6);
        ListNode l2 =new ListNode(0);
        //l2.next=new ListNode(6);
        //l2.next.next=new ListNode(4);
        //l2.next.next.next=new ListNode(2);
        ListNode result =atn.addTwoNumber(l1,l2);
        while(result != null){
            System.out.println(result.val);
            result=result.next;
        }

    }
}
