package shang.web.searchengine.Utility;

import java.util.ArrayList;
import java.util.HashMap;

public class WeightedQ<T1, T2 extends Comparable<T2>> {
    private Boolean weighted;//if it is weighted
    public ArrayList<GeneralTuple2D<T1, T2>> queue;
    private HashMap<T1, Integer> visited;//<item,order>(this uses hashCode() method internally)

    public WeightedQ() {
        this.weighted = true;
        queue = new ArrayList<>();
        queue.add(new GeneralTuple2D<T1, T2> (null, null));
        visited = new HashMap<T1, Integer>();
    }

    public WeightedQ(Boolean weighted) {
        this.weighted = weighted;
        queue = new ArrayList<>();
        queue.add(new GeneralTuple2D<T1, T2> (null, null));
        visited = new HashMap<T1, Integer>();
    }
    
    public void add(GeneralTuple2D<T1, T2> e) {
    	T1 curItem = e.getItem();
        if (!visited.containsKey(curItem)) {
        	
//            System.out.println(visited.toString());
            
            queue.add(e);
            visited.put(curItem, visited.size());//record name and order
            
            int cur = queue.size() - 1;//e is at the bottom
            if (weighted) {
                // Heap insert according to the weight
//            	System.out.println(" -- " + queue.get(cur).toString() + " " + queue.get(parent(cur)).toString());
                while (stableLarger(queue.get(cur), queue.get(parent(cur)))) {
                    swap(cur, parent(cur));
                    cur = parent(cur);
//                    System.out.println(" ---- " + queue.toString());
                }
//                System.out.println(" -- " + queue.toString());
            }
        }
        if (weighted) {
            int oldIndex = searchHeap(e, 1);

            if((oldIndex != -1) && (e.compareTo(queue.get(oldIndex)) > 0)) {
                queue.get(oldIndex).setValue(e.getValue());
                int cur = oldIndex;
                while (stableLarger(queue.get(cur), queue.get(parent(cur)))) {//can only go up
                    swap(cur, parent(cur));
                    cur = parent(cur);
                }
            }
        }
    }
    public GeneralTuple2D<T1, T2> extract() {
    	if (weighted) {
        	int root = 1;
        	int last = queue.size() - 1;
        	if(last > root) {
    	    	swap(root, last);
    	    	GeneralTuple2D<T1, T2> max = queue.remove(last);
//    	    	System.out.println("after swap:" + queue.toString());
    	    	maxHeapify(root);
//    	    	System.out.println("after heapify: " + queue.toString());
    	        return max;
        	}
        	else if(last == root) 
        		return  queue.remove(root);
        	else return null;
    	}
    	else {
    		if(queue.size() > 1) return queue.remove(1);
    		else return null;
    	}

    }
    public int size() {
    	return queue.size();
    }
    
    // Heap operation
    private int parent(int pos) {
        if (pos == 1) return 1;
        return pos >> 1;//faster
    }
    private int leftChild(int pos)
    {
        return pos << 1;
    }
    private int rightChild(int pos)
    {
        return (pos << 1) + 1;
    }
    private void swap(int fpos,int spos) {
        GeneralTuple2D<T1, T2> tmp = queue.get(fpos);
        queue.set(fpos, queue.get(spos));
        queue.set(spos, tmp);
    }
    private void maxHeapify(int pos) {
    	int leftchild = leftChild(pos);
    	int rightchild = rightChild(pos);
    	int greatest = pos;
    	int heapSize = queue.size();
//    	System.out.println("-init: " + greatest);
    	if(leftchild < heapSize && stableLarger(queue.get(leftchild), queue.get(greatest)))
    		greatest = leftchild;
    	if(rightchild < heapSize && stableLarger(queue.get(rightchild), queue.get(greatest)))
    		greatest = rightchild;
//    	System.out.println("--greatest: " + greatest);
    	if(greatest != pos) {
    		swap(pos, greatest);
    		maxHeapify(greatest);
    	}
    }
    private boolean stableLarger(GeneralTuple2D<T1, T2> lhs, GeneralTuple2D<T1, T2> rhs) {//siguoyi
    	return lhs.compareTo(rhs) > 0
				|| (lhs.compareTo(rhs) == 0
				&& (visited.get(lhs.getItem()) < visited.get(rhs.getItem())));
    }
    public int searchHeap(GeneralTuple2D<T1, T2> target, int root) {
        if(root >= queue.size()) return -1;
        GeneralTuple2D<T1, T2> rootNode = queue.get(root);
//        System.out.println(rootNode.toString() + " " + target.toString());
        if(rootNode.equals(target)) return root;//rewrite equals method
        
        int left = searchHeap(target, leftChild(root));
        if(left == -1) {
        	int right = searchHeap(target, rightChild(root));
//        	System.out.println(queue.get(right));
        	return right;
    	}
        else { 
//        	System.out.println(queue.get(left));
        	return left;
    	}
    }
    
}
