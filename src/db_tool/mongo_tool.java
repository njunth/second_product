package db_tool;

import java.net.UnknownHostException;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class mongo_tool {
	public MongoClient mongoClient;
		
	public void screen(String keywords) throws UnknownHostException{
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		DB db1 = mongoClient.getDB("zhuanzhuan");
		DBCollection coll1 = db1.getCollection("shuma");
		DBCursor cursor1 = coll1.find();
		while(cursor1.hasNext()){
			Map data = cursor1.next().toMap();
			if(data.size()<5)
				continue;
			String title = (String) data.get("title");
			String description = (String) data.get("description");
			System.out.print(title);
			System.out.println(" "+keywords);
			System.out.println(title.contains(keywords));
			if(title.contains(keywords)||description.contains(keywords)){
				BasicDBObject query = new BasicDBObject("title", title);
				BasicDBObject up = new BasicDBObject("$set", new BasicDBObject("screen_flag", "1"));
				coll1.update(query, up);
			}		
		}
		
		DB db2 = mongoClient.getDB("xianyu");
		DBCollection coll2 = db2.getCollection("shuma");
		DBCursor cursor2 = coll2.find();
		while(cursor2.hasNext()){
			Map data = cursor2.next().toMap();
			String title = (String) data.get("title");
			String description = (String) data.get("description");
			if(title.contains(keywords)||description.contains(keywords)){
				BasicDBObject query = new BasicDBObject("title", title);
				BasicDBObject up = new BasicDBObject("$set", new BasicDBObject("screen_flag", "1"));
				coll2.update(query, up);
			}		
		}
		System.out.println("ÆÁ±Î¹Ø¼ü´Ê"+keywords+"³É¹¦");
		mongoClient.close();
	}
	
	
	
	
}
