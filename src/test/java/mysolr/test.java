package mysolr;

import java.util.List;

import solr.bean.SolrPage;
import solr.util.SolrServer;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SolrServer solrServer = new SolrServer();
		long begin = System.currentTimeMillis();
		//solrServer.UpdateSolr("sdfsd.txt", "H:\\test\\test4\\sdfsd.txt");
		//System.out.println(solrServer.DeleteSolrById("sdfsd.txt"));
		SolrPage result = solrServer.SolrQueryById(1, 10, "sdfsd.txt");
		List<String> list = result.getList();
		for(String a:list){
			System.out.println(a);
		}
		System.out.println(result.getAllNum());
		long end = System.currentTimeMillis();
        System.out.println("用时：" + (end - begin) + " ms");
	}

}
