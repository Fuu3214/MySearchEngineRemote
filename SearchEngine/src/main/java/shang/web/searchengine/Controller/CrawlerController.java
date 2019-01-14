package shang.web.searchengine.Controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import shang.web.searchengine.SearchEngineApplication;
import shang.web.searchengine.Crawler.WikiCrawler;
import shang.web.searchengine.domain.CrawRequest;

public class CrawlerController {
	private String baseUrl = SearchEngineApplication.BASE_URL;
	private String rawPath = SearchEngineApplication.RAWPATH;
	private String urlPath = SearchEngineApplication.URLPATH;
	private String graphPath = SearchEngineApplication.GRAPHPATH;

	@RequestMapping(value = "/crawl/{crawRequest}", method = RequestMethod.GET)
    public String crawl(@PathVariable CrawRequest crawRequest) {
		
//      String[][] topics = {{"tennis", "grand slam"},{"basketball", "NBA"},{"Yukata", "festival"},{"computer", "turing"},{"science", "math"}, {"Anime", "Company"}, {"University", "Iowa"}};
//      String[] seedUrls = {"/wiki/Tennis", "/wiki/Basketball", "/wiki/Yukata","/wiki/Computer", "/wiki/Science", "/wiki/Anime", "/wiki/University"};
		WikiCrawler crawler = new WikiCrawler(crawRequest.getMax(), baseUrl, rawPath, urlPath, graphPath, true);
		crawler.crawlMulti(crawRequest.getSeedUrls(), crawRequest.getTopics());
		
        return "hello";
    }
}
