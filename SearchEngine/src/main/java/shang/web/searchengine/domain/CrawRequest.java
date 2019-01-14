package shang.web.searchengine.domain;

public class CrawRequest {
	private String[][] topics;
	private String[] seedUrls;
	private int max;
	public String[][] getTopics() {
		return topics;
	}
	public void setTopics(String[][] topics) {
		this.topics = topics;
	}
	public String[] getSeedUrls() {
		return seedUrls;
	}
	public void setSeedUrls(String[] seedUrls) {
		this.seedUrls = seedUrls;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}

}
