package shang.web.searchengine.Query;

import java.util.ArrayList;
import java.util.HashMap;

import shang.web.searchengine.PreProcess.PreProcess;
import shang.web.searchengine.Scoring.Scoring;
import shang.web.searchengine.Utility.GeneralTuple2D;
import shang.web.searchengine.Utility.WeightedQ;

public class QueryProcessor {
    private Scoring score;
    private int N;
    private String[] nodeNames;
    double[] pageRank;
    
    public QueryProcessor(PreProcess pp){
    	score = new Scoring(pp.getNodeMapping(), pp.getTermMapping(), pp.getPostingList(), pp.getNodeNames(), pp.getl2NormVd());

    	this.nodeNames = pp.getNodeNames();
    	this.pageRank = pp.getPageRank();
    	
    	N = nodeNames.length;
    }
    
    public QueryProcessor(HashMap<String, Integer> nodeMapping, HashMap<String, Integer> termMapping, HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> posting, String[] nodeNames, double[] l2NormVd, double[] pageRank){
    	score = new Scoring(nodeMapping, termMapping, posting, nodeNames, l2NormVd);
    	N = nodeNames.length;
    	this.nodeNames = nodeNames;
    	this.pageRank = pageRank;
    }
    
    //This program will have a method named topKDocs that gets a query and an integer k as parameter and returns an
    //ArrayList consisting of top k documents that are relevant to the query.
    
	private double[] normalize(double[] S) {
		double average = 0.0;
		int size = S.length;
		for (int i=0; i<size; i++) {
			average += S[i];
		}
		average /= size;
 
		double variance = 0.0;
		for (int i=0; i<size; i++) {
			double p = S[i];
			variance += Math.pow(p - average, 2);
		}
		variance /= size;
		
		double[] ret = new double[size];
		for (int i=0; i<size; i++) {
			ret[i] = (S[i] - average)/Math.sqrt(variance);
		}
		System.out.println(average + " " + variance);
		return ret;
	}
    
    public ArrayList<String> topKDocs(String query, int k) {
    	System.out.println("==========================================");
    	System.out.println("query: " + query);

    	double[] VSScores = normalize(score.VSSForQueryDocs(query));
    	double[] TPScores = normalize(score.TPForQueryDocs(query));
    	double[] pageRankScore = normalize(pageRank);
        WeightedQ<String, Double> TP = new WeightedQ<>();
        WeightedQ<String, Double> VSS = new WeightedQ<>();
        WeightedQ<String, Double> Relevance = new WeightedQ<>();
        WeightedQ<String, Double> finalScores = new WeightedQ<>();

        Boolean tpStatus = score.isOneQueryWord(query);
        double TPdefault = score.defaultTP(query);
        
        for(int i = 0; i < N; i++) {
        	double tp;
        	if(tpStatus)
        		tp = 0.0;
        	else
        		tp = TPScores[i] == -1 ? TPdefault : TPScores[i];        	
        	double vs = VSScores[i];
        	double rel = 0.7 * tp + 0.3 * vs;
//        	System.out.println(vs);
        	TP.add(new GeneralTuple2D<String, Double>(nodeNames[i], tp));
        	VSS.add(new GeneralTuple2D<String, Double>(nodeNames[i], vs));
        	Relevance.add(new GeneralTuple2D<String, Double>(nodeNames[i], rel));
        	double finalScore = 0.8 * rel + 0.1 * pageRankScore[i];
        	finalScores.add(new GeneralTuple2D<String, Double>(nodeNames[i], finalScore));
        }
                
        System.out.println("-Top " + k + " TPScore: ");
        topk(TP,10);
        System.out.println("-Top " + k + " VSScore: ");
        topk(VSS, 10);
        System.out.println("-Top " + k + " Relevance: ");
        topk(Relevance, 10);
        System.out.println("-Top " + k + " Rel&PageRank: ");
        ArrayList<String> topkFin = topk(finalScores, k);
        System.out.print("\n");
        
        return topkFin;
    }

        
    private ArrayList<String> topk( WeightedQ<String, Double> Scores, int k) {
    	int sizeQ = Scores.size();
    	if(sizeQ == 0)
    		return null;
    	ArrayList<String> topk = new ArrayList<String>();
    	int size = k < sizeQ? k : sizeQ;
    	for(int i = 0; i < size; i++) {
    		GeneralTuple2D<String, Double> element = Scores.extract();
    		
    		String name = element.getItem();
    		double score = element.getValue();
    		
    		System.out.println("  " + (i + 1) + "\tdoc: " + name + "\tscore: " + score);
    		if(name != null)
    			topk.add(name);
    	}
    	return topk;
    }


}
