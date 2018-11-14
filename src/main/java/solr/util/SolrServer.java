package solr.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.util.NamedList;

import solr.bean.SolrPage;

public class SolrServer {
	
	private static String url = "http://127.0.0.1:8080/solrserver/core2";//solr服务器地址
	private static HttpSolrClient solrClient = new HttpSolrClient.Builder(url).build();//solrJ连接solr服务器的接口
	
	
	/**按Text字段查找
	 * @param page 传入当前页
	 * @param num  一页的个数
	 * @param param 搜索的条件
	 * @return SolrPage一个封装的page类有当前页，总条数，一个list<String>结果集
	 */
	public SolrPage SolrQuery(int page,int num,String query){
		SolrPage solrPage = new SolrPage();
		List<String> list = new ArrayList();
		SolrQuery params = new SolrQuery();
		String param = ClientUtils.escapeQueryChars(query);
		try{
			params.set("q","text:"+param);
			params.set("start",(page-1)*num);
			params.set("rows", num);
			QueryResponse result = solrClient.query(params);
			SolrDocumentList solrDoList = result.getResults();
			solrPage.setAllNum(solrDoList.getNumFound());
			solrPage.setPage(page);
			for(SolrDocument a : solrDoList){
				list.add(a.get("id").toString());
			}
			solrPage.setList(list);
		}catch(Exception e){
			e.printStackTrace();
		}
		return solrPage;
	}
	
	/**按id字段查找
	 * @param page 传入当前页
	 * @param num  一页的个数
	 * @param param 搜索的条件
	 * @return SolrPage一个封装的page类有当前页，总条数，一个list<String>结果集
	 */
	public SolrPage SolrQueryById(int page,int num,String query){
		SolrPage solrPage = new SolrPage();
		List<String> list = new ArrayList();
		SolrQuery solrQuery = new SolrQuery();
		String param = ClientUtils.escapeQueryChars(query);
		try{
			solrQuery.set("q", "id:*"+param+"*");
			solrQuery.set("start",(page-1)*num);
			solrQuery.set("rows", num);
			QueryResponse result = solrClient.query(solrQuery);
			SolrDocumentList solrDoList = result.getResults();
			solrPage.setAllNum(solrDoList.getNumFound());
			solrPage.setPage(page);
			for(SolrDocument a : solrDoList){
				list.add(a.get("id").toString());
			}
			solrPage.setList(list);
		}catch(Exception e){
			e.printStackTrace();
		}
		return solrPage;
	}
	
	/**按“id”字段删除
	 * @param name 删除的文件的名称
	 * @return
	 */
	public boolean DeleteSolrById(String name){
		boolean flag = false;
		try{
			solrClient.deleteById(name);
			flag = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	/**按“id”字段批量删除
	 * @param name 删除的文件的名称
	 * @return
	 */
	public void DeleteSolrList(List<String> list){
		try{
			solrClient.deleteById(list);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**更新或新增索引
	 * @param name
	 * @param path
	 */
	public void UpdateSolr(String name,String path){
		try{
			SolrInputDocument solr = new SolrInputDocument();
			solr.addField("id", name);
			solr.addField("content", readFile(path));
			solrClient.add(solr);
		}catch(Exception d){
			d.printStackTrace();
		}
	}
	
	/**批量更新或新增索引
	 * @param map 一个map集合，key为id信息，value为文件完整路径
	 */
	public void UpdateSolrs(Map<String,String> map){
		try{
			List<SolrInputDocument> list = new ArrayList(); 
			for(Map.Entry<String, String> entry: map.entrySet()){
				SolrInputDocument solr = new SolrInputDocument();
				solr.addField("id", entry.getKey());
				solr.addField("content", readFile(entry.getValue()));
				list.add(solr);
			}
			solrClient.add(list);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 在更新或新增索引后执行一次提交操作，就可以立即搜索到这个索引
	 */
	public void Commit(){
		try {
			solrClient.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 全量跟新
	 */
	public  void bulidRequest(){
		Map<String, String> map = new HashMap();
		map.put("command", "full-import");
        map.put("clean", "true");
        map.put("commit", "true");
        map.put("optimize", "false");
        map.put("entity", "files");
        map.put("index", "false");
        map.put("debug", "false");
        map.put("wt", "json");
        SolrRequest<QueryResponse> solr = new QueryRequest(new MapSolrParams(map));
        solr.setPath("/dataimport");
		NamedList<Object> resp;
		try {
			resp = solrClient.request(solr);
			System.out.println(resp.toString());
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**读取文件内容，
	 * @param path 传入文件的完整路径
	 * @return
	 */
	public String readFile(String path) {
		String str = "";
		String flag = "";
		FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try{
        	File file = new File(path);
        	fis = new FileInputStream(file);
        	isr = new InputStreamReader(fis);
        	br = new BufferedReader(isr);
        	StringBuffer buffer = new StringBuffer();
        	
        	for(int i=0;(str = br.readLine())!=null;i++){
        		buffer = buffer.append(str);
        	}
        	flag = buffer.toString();
        	 //不要忘记关闭
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }
        }catch(Exception e){
        	e.printStackTrace();
        }finally {
           
        }
		return flag;
	}
	
	/**
	 * 使用结束后可以关闭连接
	 */
	public static void close(){
		try {
			solrClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
