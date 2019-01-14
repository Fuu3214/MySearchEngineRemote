package shang.web.searchengine.Crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shang.web.searchengine.Utility.*;

public class WikiCrawler {
//	private static final boolean DEBUGGING = false;
	private String baseUrl;
	private String rawPath;
	private String urlPath;
	private String graphPath;
	
	private String[] keywords;
	private int max;

	private Boolean isTopicSensitive;
	
	private HashSet<String> forbidden; //from robots.txt
	private WeightedQ<SearchNode, Double> fringe;   
	private HashSet<String> expanded; 
	
	private int counter;//count times we send request to wiki
	private int countNode;
	
	private StringBuilder urlBuilder;
	private StringBuilder graphBuilder;


	
	public WikiCrawler(int max, String baseUrl, String rawPath, String urlPath, String graphPath, Boolean isTopicSensitive){
		
		this.max = max;
		this.baseUrl = baseUrl;
		this.rawPath = rawPath;
		this.urlPath = urlPath;
		this.graphPath = graphPath;
		
		this.isTopicSensitive = isTopicSensitive;
		
		forbidden = new HashSet<String>();
		expanded = new HashSet<String>();
		fringe = new WeightedQ<SearchNode, Double>(isTopicSensitive);

		counter = 0;

		getForbidden();
		
		graphBuilder = new StringBuilder();
		
		urlBuilder = new StringBuilder();
		
		countNode = 0;

	}
		
	private void getForbidden() {
		try {
			System.out.println("Crawling robots.txt");
			URL url = new URL(baseUrl + "/robots.txt");  
			HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();  
			httpUrlConn.setDoInput(true);  
			httpUrlConn.setRequestMethod("GET");  
			httpUrlConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		
			InputStream input = httpUrlConn.getInputStream();
		
			InputStreamReader read = new InputStreamReader(input, "utf-8");
		
			BufferedReader br = new BufferedReader(read);  
		
			String line = br.readLine();
			
			
		    while ((line = br.readLine()) != null) {
	            if(line.contains("Disallow:")) {
	            	String link = line.replaceAll("Disallow: ", "");
	            	if(!link.contains(":") && !link.contains("#"))//disregard urls with #,: .We are not going to visit them anyway
	            		forbidden.add(link);
	            }
	        }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
		
	public String extractContents(String hyperlink) {
		try {  
			counter = (counter + 1) % 10;
			if(counter == 0) {
				try {
					System.out.println("waiting...");
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			URL url = new URL(hyperlink);  
			HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();  
			httpUrlConn.setDoInput(true);  
			httpUrlConn.setRequestMethod("GET");  
			httpUrlConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		
			InputStream input = httpUrlConn.getInputStream();
		
			InputStreamReader read = new InputStreamReader(input, "utf-8");
		
			BufferedReader br = new BufferedReader(read);  
		
			String line = br.readLine();
		    while ((line = br.readLine()) != null) {//skip before <p>
	            if(line.contains("<p>")) {
	            	break;
	            }
	        }

			StringBuilder sb = new StringBuilder(1024000);//build the entire doc
		    sb.append(line);
		    sb.append(" ");
		    
		    while ((line = br.readLine()) != null) {
//		    	System.out.println(line);
	            sb.append(line);
	            sb.append(" ");
	        }
		
			br.close();  
			read.close();  
			input.close();  	
			httpUrlConn.disconnect();  
			return sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean exploreChilden(SearchNode parent, String contents, int index)
	{
		writeFile(rawPath, filterHtml(contents), false);
		Pattern p = Pattern.compile("(<a href=\")(/wiki/.*?)(\")(.*?)(>)(.*?)(</a>)");
		Matcher m = p.matcher(contents);
		while(m.find()){
			String hyperlink = m.group(2);
			if(hyperlink.contains("#") || hyperlink.contains(":")) continue;
			if(forbidden.contains(hyperlink)) continue;
			if(hyperlink.equals(parent.getNodeName())) continue;//refuse self-loop
			String hypertext = m.group(6);
//			if (DEBUGGING) {
//				System.out.println(Arrays.toString(before));
//				System.out.println(hyperlink);
//				System.out.println(contents.);
//				System.out.println(hypertext);
//				System.out.println(Arrays.toString(after));
//				System.out.println(h_n);
//			}
			double h_n;
			
			if(isTopicSensitive) {
				int left = m.start();
				int right = m.end();
				
				int startOfLeft = left < 100 ? 0 : left - 100;
				int endOfRight = right > contents.length()-1 ? contents.length()-1 : right + 100;
				
				String beforeLowerCase = contents.substring(startOfLeft, left).toLowerCase();
				String afterLowerCase = contents.substring(right, endOfRight).toLowerCase();
				
				h_n = distanceHeuristic(hyperlink, hypertext, beforeLowerCase, afterLowerCase);
				
//				System.out.println(hyperlink + " " + hypertext + " " + h_n);
			}
			else
				h_n = 0.0;
			
			SearchNode child = new SearchNode(hyperlink, parent);
			
			GeneralTuple2D<SearchNode, Double> tuple = new GeneralTuple2D<SearchNode, Double>(child, h_n);
			
			fringe.add(tuple);

		}

//		if (DEBUGGING) System.out.println("Complete Node: " + parent.getNodeName());
		return false;
	}
	
	private double distanceHeuristic(String hyperlink, String hypertext, String beforeBuffer, String afterBuffer) {
		//larger the heuristic the better in this case
		if(containsKeywords(hyperlink) || containsKeywords(hypertext)) {
			return 1;
		}
		
		if(!containsKeywords(beforeBuffer) && !containsKeywords(afterBuffer)) {
			//some optimization
			return 0;
		}
		
		String[] before = beforeBuffer.split("\\W+");
		String[] after = afterBuffer.split("\\W+");
		int beforeDistance = Integer.MAX_VALUE;
		for(int i = before.length - 1; i >= Math.max(0, before.length - 17) ; i--) {
			if(keywordsAppear(before[i])) {
				beforeDistance = before.length - i;
				break;
			} 
		}
		int afterDistance = Integer.MAX_VALUE;
		for(int i = 0; i < Math.min(17, after.length) ; i++) {
			if(keywordsAppear(after[i])) {
				afterDistance = i + 1;
				break;
			}
		}
		int distance = Math.min(beforeDistance, afterDistance);
//		System.out.println(distance);
		return (distance > 17? 0 : 1/((double)distance + 2));
	}
	
	private boolean keywordsAppear(String str) {
		for(String keyword : keywords) {
			if (str != null && str.equalsIgnoreCase(keyword))
				return true;
		}
		return false;
	}
	private boolean containsKeywords(String str) {
		String tmp = str.toLowerCase();
		for(String keyword : keywords) {
			if (tmp != null && tmp.indexOf(keyword.toLowerCase()) >= 0)
				return true;
		}
		return false;
	}
	
	private static String filterHtml(String contents) {
		Pattern p = Pattern.compile("(<p>)(.*?)(</p>)");
		Matcher m = p.matcher(contents);
		StringBuilder sb = new StringBuilder(1024000);//build the entire doc
		while(m.find()){
		    sb.append(m.group(2));
		    sb.append(" ");
		}
		return delHTMLTag(sb.toString());
	}

	private static String delHTMLTag(String htmlStr) {
        String regEx_html = "<[^>]+>"; 
 
        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); 
 
        return htmlStr.trim(); // �����ı��ַ���
    }
	
	
	private void writeGraph(String contents) {
		writeFile(this.graphPath, contents, false);
	}
	
	private void writeFile(String name, String contents, boolean flag) {
		Writer writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(name, flag), "utf-8"));
		    writer.write(contents);
		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}
		
	private void appendLineGraph(StringBuilder sb, SearchNode node) {
		sb.append(node.get_parent().toString());
		sb.append(" ");
		sb.append(node.toString());
		sb.append("\r\n");
	}
	private void appendLineURL(StringBuilder sb, String line) {
		sb.append(line);
		sb.append("\r\n");
	}
	public void crawl(String seedUrl,String[] queries) {
		urlBuilder.append(max);
		urlBuilder.append("\r\n");
		doCrawl(seedUrl, queries);
	}
	public void crawlMulti(String[] seedUrls, String[][] queries) {
		if(seedUrls.length != queries.length) {
			System.out.println("error");
			return;
		}
		urlBuilder.append(max * queries.length);
		urlBuilder.append("\r\n");
		
		for(int i=0; i<queries.length; i++) {
			doCrawl(seedUrls[i], queries[i]);
		}
	}
	public void doCrawl(String seedUrl, String[] queries) {
		
		keywords = queries;
		
		int local_count = 0;
		
		System.out.println("Searching, " + "keywords: " + Arrays.toString(keywords));
				
		fringe.add(new GeneralTuple2D<SearchNode, Double>(new SearchNode(seedUrl), 0.0));
		
		SearchNode currentNode = fringe.extract().getItem();
		String rootName = currentNode.getNodeName();
		if(expanded.contains(rootName)) return;
		
		expanded.add(rootName);
		countNode++;
		local_count++;
		System.out.println(" Number of expanded sites: " + countNode);
		if (local_count >= max)
			return;
		
		String currentURL = baseUrl + rootName;
		appendLineURL(urlBuilder, rootName);
		
		exploreChilden(currentNode, extractContents(currentURL), countNode);

		while (currentNode != null){	
			
			currentNode = fringe.extract().getItem();
			String nodeName = currentNode.getNodeName();
			
			appendLineGraph(graphBuilder, currentNode);
			
			if(expanded.contains(nodeName)) continue;
			
			expanded.add(nodeName);
			
			countNode++;	
			local_count++;
			System.out.println(" Number of expanded sites: " + countNode);

			currentURL = baseUrl + nodeName;
			appendLineURL(urlBuilder, nodeName);
			
			exploreChilden(currentNode, extractContents(currentURL), countNode);
			
			if (local_count >= max) {
				GeneralTuple2D<SearchNode, Double> tuple = fringe.extract();
//				System.out.println(expanded.toString());
				while(tuple != null) {
					currentNode = tuple.getItem();
					if(expanded.contains(currentNode.getNodeName())) {
						appendLineGraph(graphBuilder, currentNode);
					}
					tuple = fringe.extract();
				}
				break;
			}
				

			// Provide a status report.
//			if (DEBUGGING) System.out.println("Nodes visited = " + nodesVisited
//					+ " |fringe| = " + fringe.size());
//			if (DEBUGGING) System.out.println("==========================================");
		}
	
		System.out.println("\nExpanded " + countNode + " nodes, starting @" +
				" " + baseUrl + seedUrl + "\n");
		
		
		writeGraph(graphBuilder.toString());
		System.out.println(countNode);
		writeFile(urlPath, urlBuilder.toString(), countNode == max);
		System.out.println("Writed to file: " + this.graphPath);
	}
	
}
