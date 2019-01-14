package shang.web.searchengine.PreProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import shang.web.searchengine.Scoring.Scoring;
import shang.web.searchengine.Utility.Graph;

public class PositionalIndex {
    private HashMap<String, Integer> files;//search in hash map: O(1), ArrayList O(N)
    private String[] fileNames;
    private HashMap<String, Integer> terms;
    private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> posting;
    double[] l2NormVd;
    
    public PositionalIndex(String filename, Graph G) {
        this.files = G.getMapping();
        this.fileNames = G.getNodeNames();
        this.terms = new HashMap<>();
        this.posting = new HashMap<>();
        preprocessing(filename);
        Scoring score = new Scoring(files, terms, posting, fileNames, l2NormVd);
        l2NormVd = score.L2Norms();
    }

    //Returns string representation of the postings(t). The returned String must be in following format.
    public String postingsList(String t) {
    	if(!terms.containsKey(t))
    		return null;
        int wordInd = terms.get(t);
        String postings = "[";
        Set<Integer> keys = posting.get(wordInd).keySet();
        Iterator<Integer> itr = keys.iterator();

        while (itr.hasNext()) {
            int fileInd = itr.next();
            String s = "<";
            s = s + fileNames[fileInd] + " : ";
            ArrayList<Integer> q = posting.get(wordInd).get(fileInd);
            int size = q.size();
            for(int i = 0; i < size; i++) {
            	s = s + q.get(i) + ",";
            }
            s = s.substring(0, s.length()-1) + ">, ";
            postings = postings + s;
        }
        postings = postings.substring(0, postings.length()-2) + "]";
        return postings;
    }
    
    public void notContain(String t) {
    	if(!terms.containsKey(t))
    		return;
        int wordInd = terms.get(t);
        HashMap<Integer, ArrayList<Integer>> tmp = posting.get(wordInd);
        
        for(String str : fileNames) {
        	if(!tmp.containsKey(files.get(str))) {
        		System.out.println(str);
        	}
        }
    }
    public String[] getFileNames() {
    	return fileNames;
    }
    public HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>getPostingList(){
    	return posting;
    }
    public double[] getl2NormVd() {
    	return l2NormVd;
    }
    public HashMap<String, Integer> getTermMapping() {
    	return terms;
    }

    private void preprocessing(String folder) {
    	System.out.println("Preprocessing");
        File file = new File(folder);
        File[] temp = file.listFiles();
        int count = 0;
        int numFiles = temp.length;
        for (int i = 0; i < temp.length; i ++) {
            int pos = 0;
            File f = temp[i];
            String fName = f.getName();
            int index = Integer.parseInt(fName.substring(0, fName.lastIndexOf('.'))) - 1;
            String nodeName = fileNames[index];
            BufferedReader inFile = null;
            if (files.containsKey(nodeName)) {
            	int docID = files.get(nodeName);
            	
	            try {
	            	InputStreamReader isr = new InputStreamReader(new FileInputStream(f), "UTF-8");
	                inFile = new BufferedReader(isr);
                	String eachLine = null;
	                while ( (eachLine = inFile.readLine()) != null) {
	                    
	                    //remove symbols and keep decimals
	                    eachLine = processLine(eachLine);             	
	
	                    StringTokenizer getWord = new StringTokenizer(eachLine);
	                    while (getWord.hasMoreTokens()) {
	//                      if run keepDecimal() here then "abc.123"->"abc 123"
	//                    	but should be "abc", "123"
	                        String word = getWord.nextToken().trim();
	                        	
	                        if (!word.isEmpty()) {
	                            // add in words collection
	                            if (!terms.containsKey(word)) {
	                                terms.put(word, terms.size());
	                            }
	
	                            // add in term collection
	                            int wordInd = terms.get(word);
	                            pos++;
	                            if (!posting.containsKey(wordInd)) {
	                            	ArrayList<Integer> newDoc = new ArrayList<>();
	                            	HashMap<Integer, ArrayList<Integer>> newWord = new HashMap<>();
	                            	newDoc.add(pos);
	                            	newWord.put(docID, newDoc);
	                                posting.put(wordInd, newWord);
	                            } else {
	                            	HashMap<Integer, ArrayList<Integer>> oldWord = posting.get(wordInd);
	                                if (!oldWord.containsKey(docID)) {
	                                	ArrayList<Integer> newDoc = new ArrayList<>();
	                                	newDoc.add(pos);
	                                	oldWord.put(docID, newDoc);
	                                } else {
	                                	oldWord.get(docID).add(pos);
	                                }
	                            }
	                        }
	                    }
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        	count++;
	        	if(count % 2000 == 0) {
	        		double d = (double)count/numFiles;
	        		String s = String.format("%.2f",d * 100);
	        		System.out.println("--" + s + "%");
	        	}
            }
        }
        System.out.println("-finished");
    }

    private String processLine(String eachLine) {
//    	to lower, remove punc, keep decimal
//    	match '1234.abc', 'abc.1123', '1234.', '.1234', keep '1234.1234'
        eachLine = eachLine.toLowerCase();
        String regex = ",|\"|\\?|\\[|\\]|'|\\{|\\}|:|;|\\(|\\)|(?<!\\d)\\.|\\.(?!\\d)";
        eachLine = eachLine.replaceAll(regex, " ");
        return eachLine;
    }
  

}
