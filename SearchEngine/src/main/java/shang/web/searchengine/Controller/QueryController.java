package shang.web.searchengine.Controller;

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import shang.web.searchengine.SearchEngineApplication;
import shang.web.searchengine.PreProcess.PreProcess;
import shang.web.searchengine.Query.QueryProcessor;


@Controller
public class QueryController {
	
	private String BASE_URL = SearchEngineApplication.BASE_URL;
	
	@Autowired
	private PreProcess pp;
	
    @RequestMapping("/hello")
    public String hello(HttpServletRequest request, @RequestParam(value = "name", required = false, defaultValue = "springboot-thymeleaf") String name) {
		
        request.setAttribute("name", name);
		
        return "hello";
    }
    
    @RequestMapping(value = "/search/{query}", method = RequestMethod.GET)
    public String getUser(@PathVariable String query) {
    	
    	QueryProcessor qp = new QueryProcessor(pp);
    	ArrayList<String> ret = qp.topKDocs(query.replace('_', ' '), 10);
    	
    	for(String url : ret) {
    		url = BASE_URL + url;
    		System.out.println(url);
    	}
    	
        return "hello";
    }

}

