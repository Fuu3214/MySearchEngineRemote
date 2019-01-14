package shang.web.searchengine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import shang.web.searchengine.PreProcess.PreProcess;

@SpringBootApplication
public class SearchEngineApplication {
	
	public static final String BASE_URL = "https://en.wikipedia.org";
	
	public static final String ROOT = System.getProperty("user.dir");
	
	public static final String RAWPATH = ROOT + "/RAW";
	public static final String DATAPATH = ROOT + "/Data";
	
	public static final String URLPATH = DATAPATH + "/url.txt";
	public static final String GRAPHPATH = DATAPATH + "/WebGraph.txt";
	public static final String TERMPATH = DATAPATH + "/term.txt";
	public static final String POSTINGPATH = DATAPATH + "/posting.txt";
	public static final String L2NORMPATH = DATAPATH + "/l2Norm.txt";
	public static final String PAGERANKPATH = DATAPATH + "/pageRank.txt";

	@Component
	public class MyStartupRunner1 implements CommandLineRunner {

		@Autowired
		private PreProcess pp;
		
	    @Override
	    public void run(String... args) throws Exception {
			pp.recover(URLPATH, GRAPHPATH,TERMPATH, POSTINGPATH, L2NORMPATH, PAGERANKPATH);	
	    }
	}
	
	public static void main(String[] args) {		
		SpringApplication.run(SearchEngineApplication.class, args);
	}

}

