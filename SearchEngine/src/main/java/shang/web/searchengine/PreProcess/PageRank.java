package shang.web.searchengine.PreProcess;

import java.util.HashMap;
import java.util.LinkedList;

import shang.web.searchengine.Utility.GeneralTuple2D;
import shang.web.searchengine.Utility.Graph;
import shang.web.searchengine.Utility.WeightedQ;

public class PageRank {
	private double epsilon;
	private double beta;
	
	private Graph G;
	private int N;
	private int M;
	
	private int iteration;
	
	private double[] pageRank;
	
	
	public PageRank(Graph G, double epsilon, double beta){
		this.epsilon = epsilon;
		this.beta = beta;

		this.G = G;
		N = G.numNodes();
		M = G.numEdges();
		iteration = 0;
		
		pageRank = computePageRank();
	}
		
	public int numEdges() {
		return M;
	}
	public int outDegreeOf(int input) {
		String nodeName = Integer.toString(input);//convert your "nice" integer input to string node name
		int index = G.indexofNode(nodeName);
		/**
		 * if the node has no out edge, the method should return 0
		 */
		LinkedList<Integer> Q = G.adjacentList().get(index);
		if (Q == null) {
			return 0;
		}
		return Q.size();
	}
	public int[] topKOutDegree(int k) {
		WeightedQ<Integer, Integer> wq = new WeightedQ<Integer, Integer>();
		HashMap<Integer, LinkedList<Integer>> adjacentList = G.adjacentList();
		for(int u : adjacentList.keySet()) {
			LinkedList<Integer> nodeList = adjacentList.get(u);
			int outdegree = nodeList.size();
			GeneralTuple2D<Integer, Integer> tuple = new GeneralTuple2D<Integer, Integer>(u, outdegree);
			wq.add(tuple);
		}
//		System.out.print(wq.toString());
		int size = k < adjacentList.size()? k : adjacentList.size();
		int[] maxk = new int[size];
		for(int i = 0; i < size; i++) {
			int index  = (int) wq.extract().getItem();
// //			System.out.println(index);
 			maxk[i] = Integer.parseInt(G.nameOf(index));
		}
//		System.out.println(nodes.toString());
		return maxk;
	}
	public int inDegreeOf(int input) {
		String nodeName = Integer.toString(input);//convert your "nice" integer input to string node name
		int index = G.indexofNode(nodeName);
		int count = 0;
		HashMap<Integer, LinkedList<Integer>> adjacentList = G.adjacentList();
		for(int u : adjacentList.keySet()) {
			LinkedList<Integer> nodeList = adjacentList.get(u);
			if(nodeList.contains(index)) 
				count++;
		}
		return count;
	}
	public int[] topKInDegree(int k) {
		int[] Table = new int[N];//initially 0
		HashMap<Integer, LinkedList<Integer>> adjacentList = G.adjacentList();
		WeightedQ<Integer, Integer> wq = new WeightedQ<Integer, Integer>();
		for(LinkedList<Integer> V : adjacentList.values()) {
			for(int index : V) {
				Table[index]++;
				}
		}
		
		for(int index = 0; index < Table.length; index++) {
			GeneralTuple2D<Integer, Integer> tuple = new GeneralTuple2D<Integer, Integer>(index, Table[index]);
			wq.add(tuple);
		}
		
//		System.out.print(wq.toString());
		
		int size = k < N? k : N;
		int[] maxk = new int[size];
		for(int i = 0; i < size; i++) {
			int index = (int)wq.extract().getItem();
//			System.out.println(index + " " + names[index] + " " + Table[index]);
			maxk[i] = Integer.parseInt(G.nameOf(index));
 		}
// //		System.out.println(G.toString());
		return maxk;
		
	}

	private boolean isEpsilonClose(double[] P, double[] Q) {
		double sum = 0;
		for(int i = 0; i < N; i++) {
			sum += Math.abs(P[i] - Q[i]);
		}
//		System.out.println(sum);
		return sum <= epsilon;
	}
	private double[] initWith(double value) {
		double[] P = new double[N];
		for(int i = 0; i < N; i++) {
			P[i] = value;
		}
		return P;
	}
	private double[] computePageRank() {
		HashMap<Integer, LinkedList<Integer>> adjacentList = G.adjacentList();
		double[] P = initWith((double)(1.0)/(N));
		boolean flag = false;
		while(!flag) {
			iteration++;
			double[] nextP = initWith((double)(1.0-beta)/(N));
			for(int u = 0; u < N; u++) {
				LinkedList<Integer> Q = adjacentList.get(u);
				if(Q == null) 
					for(int v = 0; v < N; v++) {
						nextP[v] += (double)(beta/N) * P[u];
					}
				else {
					int sizeofQ = Q.size();
					for(int v : Q) {
						nextP[v] += (double)(beta/sizeofQ) * P[u];
					}
				}
			}
//			System.out.println(Arrays.toString(P));
//			System.out.println(Arrays.toString(nextP));
			
			flag = isEpsilonClose(P,nextP);
			P = nextP;
		}
		return P;
	}
	public double pageRankOf(int input) {
		String nodeName = Integer.toString(input);//convert your "nice" integer input to string node name
		int index = G.indexofNode(nodeName);//get integer index
		return pageRank[index];
	}
	public int[] topKPageRank(int k) {
		WeightedQ<Integer, Double> wq = new WeightedQ<Integer, Double>();
		for(int index = 0; index < pageRank.length; index++) {
			GeneralTuple2D<Integer, Double> tuple = new GeneralTuple2D<Integer, Double>(index, pageRank[index]);
			wq.add(tuple);
		}
		
//		System.out.print(wq.toString());
		int size = k < N? k : N;
		int[] maxk = new int[size];
		for(int i = 0; i < size; i++) {
			int index = (int)wq.extract().getItem();
//			System.out.println(index + " " + names[index] + " " + pageRank[index]);
			maxk[i] = Integer.parseInt(G.nameOf(index));
		}
//		System.out.println(nodes.toString());
		return maxk;
	}
	public int numIteration() {
		return iteration;
	}

	public int lowestPageRank() {
		String[] names = G.getNodeNames();
		double[] temp = pageRank.clone();
		sort(temp, names, 0, temp.length-1);
//		for (int i = 0; i < names.length; i++) {
//			System.out.print("Page Rank of "+ names[i]);
//			System.out.format(" is %.4f%n" , temp[i]);
//
//		}
		return Integer.valueOf(names[0]);
	}
	private void sort (double[] arr, String[] names, int low, int high) {
		if (low < high) {
			int pivot = partition(arr, names, low, high);
			sort(arr, names, low, pivot-1);
			sort(arr, names,pivot+1, high);
		}
	}
	private int partition (double[] arr, String[] names, int low, int high) {
		double pivot = arr[high];
		int i = (low - 1);
		for (int j = low; j < high; j++) {
			if (arr[j] <= pivot) {
				i ++;
				double temp = arr[i];
				String temps = names[i];
				arr[i] = arr[j];
				names[i] = names[j];
				arr[j] = temp;
				names[j] = temps;
			}
		}
		double temp = arr[i+1];
		String temps = names[i+1];
		arr[i+1] = arr[high];
		names[i+1] = names[high];
		arr[high] = temp;
		names[high] = temps;

		return i+1;
	}
	
	
	public double[] getPageRank(){
		return pageRank;
	}

}
