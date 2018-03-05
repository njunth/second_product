package zhuanzhuan_crawl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import org.omg.CORBA.portable.InputStream;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class crawl_xianyu extends Thread{
	
	public String []url_start = {"https://s.2.taobao.com/list/list.htm?spm=2007.1000337.0.0.4a1b2979OmDSNu&catid=50100398&st_trust=1&",
	                             "https://s.2.taobao.com/list/list.htm?spm=2007.1000337.0.0.2f022979FYTiV8&catid=50100401&st_trust=1&",
	                             "https://s.2.taobao.com/list/list.htm?spm=2007.1000337.0.0.9c2429792wexpW&catid=50100402&st_trust=1&",
	                             "https://s.2.taobao.com/list/list.htm?spm=2007.1000337.0.0.2ba02979L4JW36&catid=50100403&st_trust=1&",  
								};
	
	public String keywords ="";
	
	public crawl_xianyu(String keywords){
		this.keywords = keywords;
	}
	
	public void run_onetime() {
		try {
			crawl_info(keywords);
			System.out.println("一次爬取咸鱼完成");
		} catch (UnsupportedEncodingException | MalformedURLException | UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void run() {
		try {
			while(true){
				crawl_info(keywords);
				Date d = new Date();  
				System.out.print(d);	
				System.out.println(" 爬取咸鱼完成");		
				sleep(1000*300);
			}			
		} catch (UnsupportedEncodingException | MalformedURLException | UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void crawl_info(String keywords) throws UnsupportedEncodingException, MalformedURLException, UnknownHostException{
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		System.out.println("crawl xianyu");
//		get_info("https://s.2.taobao.com/list/list.htm?spm=2007.1000337.0.0.4a1b2979OmDSNu&catid=50100398&st_trust=1&page={1}&ist=0", keywords, mongoClient);
		for(int n=0;n<4;n++){
			for(int i=1;i<101;i++){
				String u=url_start[n];
				u+="page={";
				u+=String.valueOf(i);
				u+="}&ist=0";
				get_info(u, keywords, mongoClient);
			}
		}
		mongoClient.close();
	}
	

	public void get_info(String u, String keywords, MongoClient mongoClient) throws MalformedURLException, UnknownHostException{//爬取具体信息
//		MongoClient mongoClient = new MongoClient("localhost", 27017);
		DB db = mongoClient.getDB("xianyu");
		DBCollection coll = db.getCollection("shuma");
		URL url = new URL(u);
		try {
			System.setProperty("sun.net.client.defaultConnectTimeout", "3000");
			System.setProperty("sun.net.client.defaultReadTimeout", "3000");
			url = new URL(u);
			URLConnection context =url.openConnection();                              //建立连接，进行搜索
			java.io.InputStream input=context.getInputStream();
			BufferedReader read=new BufferedReader(new InputStreamReader(input,"gbk"));
		
			String s;                                      
			while((s = read.readLine()) != null){		
//				System.out.println(s);
				if(s.matches(".*item-info-wrapper.*")){
					BasicDBObject doc = new BasicDBObject();
					s = read.readLine();
					int insert_flag = 1;
					while(!s.matches(".*</li>.*")){
						if(s.matches(".*item-title.*")&&s.indexOf("</a>")>=0){	     //获取标题和url
							int begin =s.indexOf(">", s.indexOf("href"));
							String url_item = "http:"+s.substring(s.indexOf("href")+6,begin-2);
							DBCursor cursor = coll.find(new BasicDBObject("url_info", url_item));
							if(cursor.hasNext())  {//查重操作，若已经爬取直接跳过，否则才进行爬取和存储
								insert_flag=0;
								break;
							}  
							doc.append("url_info", url_item);
							String title = s.substring(begin+1, s.indexOf("</a>"));
//							System.out.println(title);	
							if(!title.contains(keywords)){
								insert_flag=0;
								break;				
							}		
//							System.out.println(title);	
							doc.append("title", title);
						}
							
						if(s.matches(".*item-description.*")){	     //获取描述
							String description = s.substring(s.indexOf(">")+1, s.indexOf("</div>"));
//							System.out.println(description);
							doc.append("description", description);
						}
							
						if(s.matches(".*item-price.*")&&s.indexOf("<em>")>=0){	     //获取价格
							String price = s.substring(s.indexOf("<em>")+4, s.indexOf("</em>"));
//							System.out.println(price);
							if(price.indexOf("万")>0){
								double p = Double.valueOf(price.substring(0, price.indexOf("万")));
								p = p*10000;
								price = String.valueOf(p);
							}
							doc.append("price", price);
						}
						
						if(s.matches(".*seller-location.*")){                 //获取区域
							String area = s.substring(s.indexOf(">")+1, s.indexOf("</div>"));
//							System.out.println(area);
							doc.append("area", area);
						}
						s=read.readLine();
					}
					if(insert_flag==1){
						doc.append("screen_flag", "0");
						coll.insert(doc);
					}				
				}						
			}	
			read.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
