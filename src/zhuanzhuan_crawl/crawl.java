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

public class crawl extends Thread{
	
	public String url_start = "http://sz.58.com/shuma/";
	public ArrayList<String> url_list = new ArrayList<String>();
	public String keywords ="";
	
	public crawl(String keywords){
		this.keywords = keywords;
	}
	
	public void run_onetime() {
		try {
			crawl_info(keywords);
			System.out.println("һ����ȡתת���");
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
				System.out.println(" ��ȡתת���");		
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
		keywords =  java.net.URLEncoder.encode(keywords, "utf-8");
		String u = url_start + "?key=";
		u += keywords;
		u += "&cmcskey=";
		u += keywords;		
		u += "&jump=3&searchtype=1&sourcetype=5";   //����url
		System.out.println("crawl zhuanzhuan");
		System.out.println(u);
		get_urllist(u);
		for(int i =0; i<url_list.size();i++){
			get_info(url_list.get(i), mongoClient);
		}	
		mongoClient.close();
	}
	
	@SuppressWarnings("deprecation")
	public void get_urllist(String u) throws MalformedURLException{//��ȡ�����õ�����������url	
		URL url = new URL(u);
		try {
			System.setProperty("sun.net.client.defaultConnectTimeout", "3000");
			System.setProperty("sun.net.client.defaultReadTimeout", "3000");
			url = new URL(u);
			URLConnection context =url.openConnection();                              //�������ӣ���������
			java.io.InputStream input=context.getInputStream();
			BufferedReader read=new BufferedReader(new InputStreamReader(input,"UTF-8"));
		
			String s;
			while((s = read.readLine()) != null){		
				//��ȡ����url
//				System.out.println(s);
				if(s.matches(".*<a onClick.*target.*")){	
					if(s.matches(".*class=\"t\".*")){
						continue;
					}
//					System.out.println(s);
					String t = s.substring(s.indexOf("href")+6, s.indexOf("target")-2);
//					System.out.println(t);
					url_list.add(t);
//					System.out.println(url_list.size());
				}
				//������һҳ
				if(s.matches(".*��һҳ.*")){
//					System.out.println(s);
					String part_url = s.substring(s.indexOf("next")+12,s.indexOf("��һҳ")-8);
//					System.out.println(part_url);
					get_urllist("http://sz.58.com"+part_url);
				}				
			}						
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void get_info(String u, MongoClient mongoClient) throws MalformedURLException, UnknownHostException{//��ȡ������Ϣ
//		MongoClient mongoClient = new MongoClient("localhost", 27017);
		DB db = mongoClient.getDB("zhuanzhuan");
		DBCollection coll = db.getCollection("shuma");
		URL url = new URL(u);
		try {
			System.setProperty("sun.net.client.defaultConnectTimeout", "3000");
			System.setProperty("sun.net.client.defaultReadTimeout", "3000");
			url = new URL(u);
			URLConnection context =url.openConnection();                              //�������ӣ���������
			java.io.InputStream input=context.getInputStream();
			BufferedReader read=new BufferedReader(new InputStreamReader(input,"UTF-8"));
		
			String s;
			DBCursor cursor = coll.find(new BasicDBObject("url_info", u));
			if(!cursor.hasNext()){                                                 //���ز��������Ѿ���ȡֱ������������Ž�����ȡ�ʹ洢
				BasicDBObject doc = new BasicDBObject("url_info", u);
				while((s = read.readLine()) != null){		
					//��ȡ����url
//					System.out.println(s);
					if(s.matches(".*info_titile.*")&&s.indexOf("info_titile")>=0){	     //��ȡ����
//						System.out.println(s);
						String title = s.substring(s.indexOf("info_titile")+13);
//						System.out.println(title);
						doc.append("title", title);
					}
					
					if(s.matches(".*icon_png sanjiao.*")){	     //��ȡ����
						s = read.readLine();
//						System.out.println(u);
//						System.out.println(s);
						String description = s.substring(s.indexOf("<p>")+3,s.length());
//						System.out.println(description);
						doc.append("description", description);
					}
					
					if(s.matches(".*price_now.*")&&s.indexOf("<i>")>=0){	     //��ȡ�۸�
						String price = s.substring(s.indexOf("<i>")+3, s.indexOf("</i>"));
//						System.out.println(price);
						if(price.indexOf("��")>0){
							double p = Double.valueOf(price.substring(0, price.indexOf("��")));
							p = p*10000;
							price = String.valueOf(p);
						}
						doc.append("price", price);
					}
					
					if(s.matches(".*����.*")&&s.indexOf("<i>")>=0){                 //��ȡ����
						String area = s.substring(s.indexOf("<i>")+3, s.indexOf("</i>"));
//						System.out.println(area);
						doc.append("area", area);
					}							
				}
				doc.append("screen_flag", "0");
				coll.insert(doc);
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
