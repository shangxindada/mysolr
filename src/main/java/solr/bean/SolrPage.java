package solr.bean;

import java.util.List;

public class SolrPage {
	
	private int page;
	private long allNum;
	private List<String> list;
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public long getAllNum() {
		return allNum;
	}
	public void setAllNum(long allNum) {
		this.allNum = allNum;
	}
	public List<String> getList() {
		return list;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
}
