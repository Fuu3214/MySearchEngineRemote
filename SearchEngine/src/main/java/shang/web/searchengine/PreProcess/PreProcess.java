package shang.web.searchengine.PreProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.springframework.stereotype.Service;

import shang.web.searchengine.Utility.Graph;

@Service
public class PreProcess {
	private Graph G;
	int N;
	double[] pageRank;
	private HashMap<String, Integer> termMapping;
	private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> posting;
	private double[] l2NormVd;
		
	public void computeFrom(String RawPath, String urlPath, String GraphPath) {
		G = new Graph(urlPath, GraphPath);
		PageRank pr = new PageRank(G, 0.001, 0.85);
		pageRank = pr.getPageRank();
		PositionalIndex pi = new PositionalIndex(RawPath, G);
		termMapping = pi.getTermMapping();
		l2NormVd = pi.getl2NormVd();
		posting = pi.getPostingList();
	}
	
	public void recover(String nodePath, String WebGraph, String termPath, String postingPath, String l2NormPath, String pageRankPath) {
		G = new Graph(nodePath, WebGraph);
		N = G.numNodes();
		
		pageRank = new double[N];
		l2NormVd = new double[N];
		termMapping = new HashMap<>();
		posting = new HashMap<>();
		
		recoverArray(l2NormVd, l2NormPath);
		recoverArray(pageRank, pageRankPath);
		recoverTermMapping(termMapping, termPath);
		recoverPosting(posting, postingPath);
	}
	private void recoverArray(double[] arr, String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {            
            String line;
    		int i = 0;
            while ((line = br.readLine()) != null) {
            	arr[i] = Double.parseDouble(line);
            	i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	private void recoverTermMapping(HashMap<String, Integer> mapping, String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {            
            String line;
            while ((line = br.readLine()) != null) {
            	mapping.put(line, mapping.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	private void recoverPosting(HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>posting, String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {            
            String line;
            while ((line = br.readLine()) != null) {
            	StringTokenizer st = new StringTokenizer(line, ":");
            	int termIdx = Integer.parseInt(st.nextToken());
            	posting.put(termIdx, new HashMap<>());
            	HashMap<Integer, ArrayList<Integer>> termPosting = posting.get(termIdx);
            	while( st.hasMoreElements() ){
            	    String str = st.nextToken(":;");
            	    StringTokenizer st1 = new StringTokenizer(str, "-");
            	    int docId = Integer.parseInt(st1.nextToken());
            	    termPosting.put(docId, new ArrayList<>());
            	    ArrayList<Integer> position = termPosting.get(docId);
            	    while(st1.hasMoreElements()) {
            	    	String tmp = st1.nextToken("-[], ");
            	    	int pos = Integer.parseInt(tmp);
            	    	position.add(pos);
            	    }
            	}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void save(String termPath, String postingPath, String l2NormPath, String pageRankPath) {
		storeArray(l2NormVd, l2NormPath);
		storeArray(pageRank, pageRankPath);
		
		storeTermMapping(termMapping, termPath);
		storePosting(posting, postingPath);
	}
	
	
	private void storePosting(HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>posting, String path) {
		StringBuilder sb = new StringBuilder();
		for(int termIdx : posting.keySet()) {
			sb.append(termIdx);
			sb.append(':');
			HashMap<Integer, ArrayList<Integer>> term = posting.get(termIdx);
			for(int docIdx : term.keySet()) {
				sb.append(docIdx);
				sb.append('-');
				ArrayList<Integer> pos = term.get(docIdx);
				sb.append(pos.toString());
				sb.append(';');
			}
			sb.append("\r\n");
		}
		writefile(sb, path);
	}
	private void storeTermMapping(HashMap<String, Integer> mapping, String path) {
		String[] terms = new String[mapping.size()];
		for(String term : mapping.keySet()) {
			terms[mapping.get(term)] = term;
		}
		storeArray(terms, path);
	}
	private void storeArray(String[] arr, String path) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < arr.length; i++) {
			sb.append(arr[i]);
			sb.append("\r\n");
		}
		writefile(sb, path);
	}
	private void storeArray(double[] arr, String path) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < arr.length; i++) {
			sb.append(arr[i]);
			sb.append("\r\n");
		}
		writefile(sb, path);
	}
	private void writefile(StringBuilder sb, String path) {
		Writer writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(path), "utf-8"));
		    writer.write(sb.toString());
		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}
	
	public double[] getPageRank(){
		return pageRank;
	}
	public HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> getPostingList(){
		return posting;
	}
	public double[]getl2NormVd(){
		return l2NormVd;
	}
	public String[] getNodeNames() {
		return G.getNodeNames();
	}
	public HashMap<String, Integer> getTermMapping() {
		return termMapping;
	}
	public HashMap<String, Integer> getNodeMapping() {
		return G.getMapping();
	}
}
