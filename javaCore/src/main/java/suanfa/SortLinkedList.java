package suanfa;

/**
 * create by huirong on 2021-02-19 14:27
 */

public class SortLinkedList {


    public Node[] splitList(Node node){
        Node single=new Node();
        Node doub=new Node();
        int count=0;
        do{
            Node singlePoint=single;
            Node doubPoint=doub;
            if (count%2!=0){
                singlePoint.setNext(new Node(node.getValue()));
                single=single.next;
            }
            else {
                doubPoint.setNext(new Node(node.getValue()));
                doubPoint=doubPoint.next;
            }
            node=node.next;
            count++;
        }
        while (node!=null);

        return new Node[]{doub,single};
    }

    public Node reverseList(Node node){
        Node curr=node;
        Node next=curr.next;
        Node pre=null;
        while (next!=null){
            curr.next=pre;
            pre=curr;
            curr=next;
            next=next.next;
        }
        return curr;
    }


    public Node mergeList(Node node1,Node node2){
        Node node=new Node(Math.min(node1.getValue(),node2.getValue()));
        if(node1.getValue() >= node2.getValue()){
            node2=node2.next;
        }
        else{
            node1=node1.next;
        }
        Node point=node.next;
        while (node1!=null&&node2!=null) {
            if (node1.getValue() >= node2.getValue()) {
                point =new Node(node2.getValue());
                node2=node2.next;
            }
            else{
                point =new Node(node1.getValue());
                node1=node1.next;
            }
            point=point.next;

        }
        return node;
    }







    class Node {
        private int value;
        private Node next;

        public Node() {

        }

        public Node(int v) {
            this.value = v;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }
}
