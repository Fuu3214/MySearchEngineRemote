package shang.web.searchengine.Utility;

import java.util.Objects;

public class SearchNode{
	
	public SearchNode parent;
	
	private String nodeName;
	
	public SearchNode(String name) {
		nodeName = name;
		parent = this;
	}
	public SearchNode(String name, SearchNode pn) {
		nodeName = name;
		parent = pn;
	}

	public String getNodeName() {
		return nodeName;
	} 
	public void set_parent(SearchNode pn) {
		parent = pn;
	}
	public SearchNode get_parent() {
		return parent;
	} 
		    
    @Override
    public String toString() {
    	return  nodeName;
    }
    
    @Override
    public int hashCode() {
    	return (parent.getNodeName() + nodeName).hashCode();
    }
        
    @Override
    public boolean equals(Object o) {//compare parent + nodeName  
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchNode compared_search_node = (SearchNode) o;
        if(Objects.equals(nodeName, compared_search_node.nodeName) && Objects.equals(parent.getNodeName(), compared_search_node.get_parent().getNodeName()))
        	return true;
        else return false;
    }

}
