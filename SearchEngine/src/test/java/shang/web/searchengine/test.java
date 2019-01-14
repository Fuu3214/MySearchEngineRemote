package shang.web.searchengine;

import shang.web.searchengine.PreProcess.PreProcess;

public class test {
	public static void main(String[] args) {
			
		String RawPath = SearchEngineApplication.RAWPATH ;
		String urlPath = SearchEngineApplication.URLPATH;
		String GraphPath = SearchEngineApplication.GRAPHPATH;
		String termPath = SearchEngineApplication.TERMPATH;
		String postingPath = SearchEngineApplication.POSTINGPATH;
		String l2NormPath = SearchEngineApplication.L2NORMPATH;
		String pageRankPath = SearchEngineApplication.PAGERANKPATH;
		
		PreProcess pp = new PreProcess();
		pp.computeFrom(RawPath, urlPath, GraphPath);
		pp.save(termPath, postingPath, l2NormPath, pageRankPath);
		
		PreProcess ppr = new PreProcess();
		ppr.recover(urlPath, GraphPath, termPath, postingPath, l2NormPath, pageRankPath);
//		System.out.println(ppr.getNodeMapping().toString());
		for(int termIdx:pp.getPostingList().keySet()) {
			for(int docIdx : pp.getPostingList().get(termIdx).keySet()) {
				for(int i = 0; i < pp.getPostingList().get(termIdx).get(docIdx).size(); i++) {
					if(!pp.getPostingList().get(termIdx).get(docIdx).get(i).equals(ppr.getPostingList().get(termIdx).get(docIdx).get(i))) {
						System.out.println(pp.getPostingList().get(termIdx).get(docIdx).get(i));
						System.out.println(ppr.getPostingList().get(termIdx).get(docIdx).get(i));
						System.out.println("---------------------");
					}
				}
			}
		}
	}
}
