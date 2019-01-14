package shang.web.searchengine.Controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import shang.web.searchengine.SearchEngineApplication;
import shang.web.searchengine.PreProcess.PreProcess;

public class PreProcessController {
	
	private String RawPath = SearchEngineApplication.RAWPATH ;
	private String urlPath = SearchEngineApplication.URLPATH;
	private String GraphPath = SearchEngineApplication.GRAPHPATH;
	private String termPath = SearchEngineApplication.TERMPATH;
	private String postingPath = SearchEngineApplication.POSTINGPATH;
	private String l2NormPath = SearchEngineApplication.L2NORMPATH;
	private String pageRankPath = SearchEngineApplication.PAGERANKPATH;
	
    @RequestMapping(value = "/preprocess", method = RequestMethod.GET)
    public String preprocess(HttpServletRequest request, @RequestParam(value = "name", required = false, defaultValue = "springboot-thymeleaf") String name) {
		
        request.setAttribute("preprocess", name);
        
        PreProcess pp = new PreProcess();
        pp.computeFrom(RawPath, urlPath, GraphPath);
        pp.save(termPath, postingPath, l2NormPath, pageRankPath);
		
        return "hello";
    }
}
