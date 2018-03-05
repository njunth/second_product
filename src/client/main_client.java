package client;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.*;
import java.awt.event.*;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import zhuanzhuan_crawl.crawl;
import zhuanzhuan_crawl.crawl_xianyu;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import db_tool.mongo_tool;

public class main_client extends JFrame{
	private static final Font fontc = new Font("微软雅黑",Font.PLAIN+Font.BOLD,20);   //字体设置
	private static final Font fonte = new Font("Calibri",Font.PLAIN,16);
	private static final Font fontc1 = new Font("微软雅黑",Font.PLAIN,14);
	private static final Font fontc2 = new Font("微软雅黑",Font.PLAIN+Font.BOLD,17);

	public JTextField crawl_keyword = new JTextField(30);   //爬虫关键词
	public JButton crawl_button = new JButton("开始搜索");    //爬虫确认按钮
	public JButton crawl_button_forever = new JButton("常驻搜索");    //常驻爬虫确认按钮
	
	public JButton select = new JButton("筛选");             //筛选数据按钮
	
	public JTextField screen_keyword = new JTextField(30);   //屏蔽关键词
	public JButton screen_button = new JButton("关键词屏蔽");    //屏蔽确认按钮
	
	public JTable show_info;                          //展示表格
	public TableModel dataModel;  
	public JScrollPane scrollpane;  
	public Timer timer; 
	public JButton add = new JButton("增加");             //增删改屏蔽
	public JButton remove = new JButton("删除");
	public JButton change = new JButton("修改");
	public JButton screen_one = new JButton("屏蔽");
	
	public static Toolkit toolkit = Toolkit.getDefaultToolkit();     //蜂鸣组件
	public int show_count = 0;
	
	public String title_keyword = "";
	public String description_keyword = "";
	public String area = "";
	public double price_up = 100000;                          //初始价格区间0-100000
	public double price_down = 0;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		main_client client=new main_client();
		client.setLocationRelativeTo(null);
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.setTitle("二手低价商品推荐系统");

		client.setVisible(true);
	}
	
	public main_client(){
		JPanel mainj=new JPanel();     //总的背景模块
		mainj.setLayout(new BorderLayout());
		setSize(650, 550);
		
		//模块版面设置
		Box up=Box.createHorizontalBox();                     //客户端上部模块
		up.add(Box.createHorizontalStrut(10));
		up.add(crawl_keyword);
		up.add(Box.createHorizontalStrut(20));
		up.add(crawl_button);
		crawl_button.setFont(fontc1);                        //字体设置
		crawl_button.setBounds(200,10,200,25);
		up.add(Box.createHorizontalStrut(20));
		up.add(crawl_button_forever);
		crawl_button_forever.setFont(fontc1);                        //字体设置
		crawl_button_forever.setBounds(200,10,200,25);
		up.add(Box.createHorizontalStrut(40));
		up.add(select);
		select.setFont(fontc1);                             //字体设置
		select.setBounds(200,10,200,25);
		up.add(Box.createHorizontalStrut(40));
		up.add(screen_keyword);
		up.add(Box.createHorizontalStrut(20));
		up.add(screen_button);
		screen_button.setFont(fontc1);                             //字体设置
		screen_button.setBounds(200,10,200,25);
		up.add(Box.createHorizontalStrut(10));
		up.setSize(600, 20);
		mainj.add(up, BorderLayout.NORTH);
		
		//下部展示板块
		dataModel = getTableModel();  
	    show_info = new JTable(dataModel); 
	    show_info.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    show_count = show_info.getRowCount();
	    scrollpane = new JScrollPane(show_info);
	    timer=new Timer(20000,new ActionListener() {              //动态刷新展示页面，每20秒1次
            public void actionPerformed(ActionEvent evt) {  
            	System.out.println("刷新");
            	show_info.validate();  
            	show_info.updateUI();  
            	int now_count = show_info.getRowCount();
            	if(now_count > show_count){                       //用于刷新时查看是否有新的数据进来，有的话蜂鸣
            		toolkit.beep();      		
            	}
            	show_count = now_count;
            }  
        });
	    timer.start();
		
		JPanel inf = new JPanel();	
		inf.setBorder(new TitledBorder(null, "筛选结果", TitledBorder.LEFT, TitledBorder.TOP,fontc1,Color.black));
		inf.add(scrollpane);
		mainj.add(inf, BorderLayout.CENTER);
		
		Box down=Box.createVerticalBox();
		down.add(Box.createVerticalStrut(80));
		add.setFont(fontc1);
		down.add(add);
		down.add(Box.createVerticalStrut(20));
		remove.setFont(fontc1);
		down.add(remove);
		down.add(Box.createVerticalStrut(20));
		change.setFont(fontc1);
		down.add(change);
		down.add(Box.createVerticalStrut(20));
		screen_one.setFont(fontc1);
		down.add(screen_one);
		down.add(Box.createVerticalStrut(80));
		down.setSize(40, 200);
		mainj.add(down, BorderLayout.EAST);
		
		//监听器设置
		crawl_button.addActionListener(new ActionListener(){             //点击搜索按钮
			public void actionPerformed(ActionEvent e) {
				String keywords=crawl_keyword.getText();
				System.out.println(keywords);
				crawl c = new crawl(keywords);
				c.run_onetime();
				crawl_xianyu c_x= new crawl_xianyu(keywords);
				c_x.run_onetime();
//					c.crawl_info(keywords);                          //开始爬取
//					c_x.crawl_info(keywords);
//					c.get_info("http://zhuanzhuan.58.com/detail/968110988854968326z.shtml?fullCate=5%2C37&fullLocal=4&from=pc&metric=null");			
			}		
		});
		
		crawl_button_forever.addActionListener(new ActionListener(){             //点击常驻搜索按钮
			public void actionPerformed(ActionEvent e) {
				String keywords=crawl_keyword.getText();
				System.out.println(keywords);
				crawl c = new crawl(keywords);
				c.start();
				crawl_xianyu c_x= new crawl_xianyu(keywords);
				c_x.start();
//					c.crawl_info(keywords);                          //开始爬取
//					c_x.crawl_info(keywords);
//					c.get_info("http://zhuanzhuan.58.com/detail/968110988854968326z.shtml?fullCate=5%2C37&fullLocal=4&from=pc&metric=null");			
			}		
		});
		
		screen_button.addActionListener(new ActionListener(){              //点击屏蔽按钮
			public void actionPerformed(ActionEvent e){
				String screen_key = screen_keyword.getText();
				try {
					mongo_tool mt = new mongo_tool();
					mt.screen(screen_key);
					show_info.validate();  
	            	show_info.updateUI(); 
	            	show_count = show_info.getRowCount();
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		select.addActionListener(new ActionListener(){                    //点击筛选按钮
			public void actionPerformed(ActionEvent e){
				JFrame frame1 = new JFrame("设置筛选条件");                //跳出新的界面
                //设置在屏幕的位置  
                frame1.setLocation(200,100);  
                
                JPanel pan=new JPanel();  
                frame1.setSize(330, 250);
                pan.setLayout(new FlowLayout());
                
                JLabel title_l = new JLabel("标题关键词");
                JTextField title_t = new JTextField(20);
                pan.add(title_l);
                pan.add(title_t);
                
                JLabel description_l = new JLabel("描述关键词");
                JTextField description_t = new JTextField(20);
                pan.add(description_l);
                pan.add(description_t);
                
                JLabel area_l = new JLabel("地区关键词");
                JTextField area_t = new JTextField(20);
                pan.add(area_l);
                pan.add(area_t);
                
                JLabel price_up_l = new JLabel("价格上限");
                JTextField price_up_t = new JTextField(20);
                pan.add(price_up_l);
                pan.add(price_up_t);
                
                JLabel price_down_l = new JLabel("价格下限");
                JTextField price_down_t = new JTextField(20);
                pan.add(price_down_l);
                pan.add(price_down_t);
                
                JButton enter = new JButton("确认");
                enter.addActionListener(new ActionListener(){
                	public void actionPerformed(ActionEvent e){
                		title_keyword = title_t.getText();
                		description_keyword = description_t.getText();
                		area = area_t.getText();
                		if(price_up_t.getText().length()>0){
                			price_up = Double.valueOf(price_up_t.getText());
                		}
                		if(price_down_t.getText().length()>0){
                			price_down = Double.valueOf(price_down_t.getText());
                		}               		
                		frame1.dispose();
                		System.out.println(title_keyword+description_keyword+area+price_up+price_down);
                		show_info.validate();  
                    	show_info.updateUI();  
                    	show_count = show_info.getRowCount();
                	}
                });
                pan.add(enter);
                frame1.add(pan);
                frame1.setVisible(true);     
			}
		});		
		
		add.addActionListener(new ActionListener(){                    //点击添加按钮
			public void actionPerformed(ActionEvent e){
				JFrame frame1 = new JFrame("添加数据");                //跳出新的界面
                //设置在屏幕的位置  
                frame1.setLocation(200,100);  
                
                JPanel pan=new JPanel();  
                frame1.setSize(300, 250);
                pan.setLayout(new FlowLayout());
                
                JLabel title_l = new JLabel("标题");
                JTextField title_t = new JTextField(20);
                pan.add(title_l);
                pan.add(title_t);
                
                JLabel description_l = new JLabel("描述");
                JTextField description_t = new JTextField(20);
                pan.add(description_l);
                pan.add(description_t);
                
                JLabel area_l = new JLabel("地区");
                JTextField area_t = new JTextField(20);
                pan.add(area_l);
                pan.add(area_t);
                
                JLabel price_l = new JLabel("价格");
                JTextField price_t = new JTextField(20);
                pan.add(price_l);
                pan.add(price_t);
                
                JLabel url_l = new JLabel("url链接");
                JTextField url_t = new JTextField(20);
                pan.add(url_l);
                pan.add(url_t);
                
                JButton enter = new JButton("确认");
                enter.addActionListener(new ActionListener(){
                	public void actionPerformed(ActionEvent e){
                		String title_add = title_t.getText();
                		String description_add = description_t.getText();
                		String area_add = area_t.getText();
                		String price_add = price_t.getText();
                		BasicDBObject doc = new BasicDBObject("url_info", url_t.getText());
                		doc.append("title", title_add);
                		doc.append("description", description_add);
                		doc.append("area", area_add);
                		doc.append("price", price_add);
                		doc.append("screen_flag", "0");
                		MongoClient mongoClient;
						try {
							mongoClient = new MongoClient("localhost", 27017);
							DB db1 = mongoClient.getDB("zhuanzhuan");							
							DBCollection coll1 = db1.getCollection("shuma");
							coll1.insert(doc);
							System.out.println("插入成功！");
							mongoClient.close();							
						} catch (UnknownHostException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}               		               		
                		frame1.dispose();
                		show_info.validate();  
                    	show_info.updateUI();  
                    	show_count = show_info.getRowCount();
                	}
                });
                pan.add(enter);
                frame1.add(pan);
                frame1.setVisible(true);     
			}
		});	
		
		remove.addActionListener(new ActionListener(){                    //点击删除按钮
			public void actionPerformed(ActionEvent e){
				int row = show_info.getSelectedRow();
				String title_remove = (String) show_info.getValueAt(row, 1); 
				MongoClient mongoClient;
				try {
					mongoClient = new MongoClient("localhost", 27017);
					DB db1 = mongoClient.getDB("zhuanzhuan");							
					DBCollection coll1 = db1.getCollection("shuma");
					if(title_remove.endsWith("null")){
						title_remove="";
					}
					DBObject useless = new BasicDBObject("title", title_remove);
					coll1.remove(useless);
					
					DB db2 = mongoClient.getDB("xianyu");							
					DBCollection coll2 = db2.getCollection("shuma");
					coll2.remove(useless);
					
					System.out.println("删除成功！");
					mongoClient.close();
					show_info.validate();  
	            	show_info.updateUI(); 
	            	show_count = show_info.getRowCount();
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}						
			}
		});
		
		change.addActionListener(new ActionListener(){                    //点击修改按钮
			public void actionPerformed(ActionEvent e){
				JFrame frame1 = new JFrame("修改数据");                //跳出新的界面
                //设置在屏幕的位置  
                frame1.setLocation(200,100);  
                frame1.setSize(500, 350);
                frame1.setLayout(new BorderLayout());
                
                JPanel jp = new JPanel();
                jp.setLayout(new GridLayout(1,2,10,10));
                
                JPanel pan=new JPanel();               
                pan.setLayout(new GridLayout(5,2,5,5));
                pan.setBorder(new TitledBorder(null, "原始信息", TitledBorder.LEFT, TitledBorder.TOP,fontc1,Color.black));
                
                JLabel title_lo = new JLabel("标题");
                int row = show_info.getSelectedRow();
				String t = (String) show_info.getValueAt(row, 1);
                JTextArea title_o = new JTextArea(t);
                pan.add(title_lo);
                pan.add(title_o);
                
                JLabel price_lo = new JLabel("价格");
                String p = (String) show_info.getValueAt(row, 2);
                JTextArea price_o = new JTextArea(p);
                pan.add(price_lo);
                pan.add(price_o);
                
                JLabel description_lo = new JLabel("描述");
				String des = (String) show_info.getValueAt(row, 3);
                JTextArea description_o = new JTextArea(des);
                pan.add(description_lo);
                pan.add(description_o);
                
                JLabel area_lo = new JLabel("地区");
				String a = (String) show_info.getValueAt(row, 4);
                JTextArea area_o = new JTextArea(a);
                pan.add(area_lo);
                pan.add(area_o);
                
                JLabel url_lo = new JLabel("url链接");
                String u = (String) show_info.getValueAt(row, 5);
                JTextArea url_o = new JTextArea(u);
                pan.add(url_lo);
                pan.add(url_o);
                
                jp.add(pan);
                
                JPanel pan_c=new JPanel();               
                pan_c.setLayout(new GridLayout(6,2,20,20));
                pan_c.setBorder(new TitledBorder(null, "修改后信息", TitledBorder.LEFT, TitledBorder.TOP,fontc1,Color.black));

                
                JLabel title_l = new JLabel("标题");
                JTextField title_t = new JTextField(20);
                pan_c.add(title_l);
                pan_c.add(title_t);

                JLabel price_l = new JLabel("价格");
                JTextField price_t = new JTextField(20);
                pan_c.add(price_l);
                pan_c.add(price_t);
                
                JLabel description_l = new JLabel("描述");
                JTextField description_t = new JTextField(20);
                pan_c.add(description_l);
                pan_c.add(description_t);
                
                JLabel area_l = new JLabel("地区");
                JTextField area_t = new JTextField(20);
                pan_c.add(area_l);
                pan_c.add(area_t);
                
                JLabel url_l = new JLabel("url链接");
                JTextField url_t = new JTextField(20);
                pan_c.add(url_l);
                pan_c.add(url_t);
                
                JButton enter = new JButton("确认");
                enter.addActionListener(new ActionListener(){
                	public void actionPerformed(ActionEvent e){
                		String title_origin = title_o.getText();
                		String title_change = title_t.getText();
                		String description_change = description_t.getText();
                		String area_change = area_t.getText();
                		String price_change = price_t.getText();
                		BasicDBObject doc = new BasicDBObject("url_info", url_t.getText());
                		doc.append("title", title_change);
                		doc.append("description", description_change);
                		doc.append("area", area_change);
                		doc.append("price", price_change);
                		doc.append("screen_flag", "0");
                		MongoClient mongoClient;
						try {
							mongoClient = new MongoClient("localhost", 27017);
							DB db1 = mongoClient.getDB("zhuanzhuan");							
							DBCollection coll1 = db1.getCollection("shuma");
							if(title_origin.endsWith("null")){
								title_origin="";
							}
							BasicDBObject origin = new BasicDBObject("title", title_origin);
							DBCursor cursor = coll1.find(new BasicDBObject("title", title_origin));
							if(cursor.hasNext()){
								coll1.remove(origin);
								coll1.insert(doc);
							}					
							
							DB db2 = mongoClient.getDB("xianyu");							
							DBCollection coll2 = db2.getCollection("shuma");
							DBCursor cursor2 = coll2.find(new BasicDBObject("title", title_origin));
							if(cursor2.hasNext()){
								coll2.remove(origin);
								coll2.insert(doc);
							}							
							
							System.out.println("修改成功！");
							mongoClient.close();							
						} catch (UnknownHostException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}               		               		
                		frame1.dispose();
                		show_info.validate();  
                    	show_info.updateUI();  
                    	show_count = show_info.getRowCount();
                	}
                });
                pan_c.add(enter);
                jp.add(pan_c);
                frame1.add(jp, BorderLayout.NORTH);
                frame1.setVisible(true);
			}
		});
		
		screen_one.addActionListener(new ActionListener(){                    //点击下面的屏蔽按钮
			public void actionPerformed(ActionEvent e){
				int row = show_info.getSelectedRow();
				String title_screen = (String) show_info.getValueAt(row, 1); 
				MongoClient mongoClient;
				try {
					mongoClient = new MongoClient("localhost", 27017);
					DB db1 = mongoClient.getDB("zhuanzhuan");							
					DBCollection coll1 = db1.getCollection("shuma");
					if(title_screen.endsWith("null")){
						title_screen="";
					}
					BasicDBObject query = new BasicDBObject("title", title_screen);
					BasicDBObject up = new BasicDBObject("$set", new BasicDBObject("screen_flag", "1"));
					coll1.update(query, up);
					
					DB db2 = mongoClient.getDB("xianyu");							
					DBCollection coll2 = db2.getCollection("shuma");
					coll2.update(query, up);
					
					show_info.validate();  
	            	show_info.updateUI(); 
	            	show_count = show_info.getRowCount();
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
			}
		});		
		
		//悬浮提示单元格的值   
		show_info.addMouseMotionListener(new MouseAdapter(){  
		    public void mouseMoved(MouseEvent e) {  
		        int row=show_info.rowAtPoint(e.getPoint());  
		        int col=show_info.columnAtPoint(e.getPoint());  
		        if(row>-1 && col>-1){  
		            String value=show_info.getValueAt(row, col).toString(); 
		            String s = "<html>";
		            if(null!=value && !"".equals(value)){	            	
		            	for(int i=0;i<value.length();){
		            		if(i+40<value.length()){
		            			s+= "<p>";
		            			s+=value.substring(i,i+40);
		            			s+= "</p>";
		            		}
		            		else{
		            			s+= "<p>";
		            			s+=value.substring(i);
		            			s+= "</p>";
		            		}		            		
		            		i+=40;
		            	}
		            	s += "</html>";
		            	show_info.setToolTipText(s);//悬浮显示单元格内容  
		            }
		        }  
		    }  
		});
		
		add(mainj);
	}
	
	public ArrayList<String> getData() throws UnknownHostException {                  //从数据库中读取符合筛选条件的数据
		ArrayList<String> s = new ArrayList<String>();
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		DB db1 = mongoClient.getDB("zhuanzhuan");
		DBCollection coll1 = db1.getCollection("shuma");
		DBCursor cursor1 = coll1.find();
		while(cursor1.hasNext()){
			Map data = cursor1.next().toMap();
			if(data.size()<=5)
				continue;
			String title = (String) data.get("title");
			String description = (String) data.get("description");
			double price=0;
			if(isNumeric(data.get("price").toString())&&data.get("price").toString().length()>0){
				price = Double.valueOf((String) data.get("price"));
			}		
			String area_temp = (String) data.get("area");
			String flag = (String) data.get("screen_flag");
			String url = data.get("url_info").toString();
			if(area_temp.length()==0)
				area_temp="null";
			if(title.length()==0)
				title="null";
			if(description.length()==0)
				description="null";
			if(price==0)
				price = 0;
			if(url.length()==0)
				url="url";
			if(title.contains(title_keyword)&&description.contains(description_keyword)&&area_temp.contains(area)&&(price>=price_down)&&(price<=price_up)&&(flag.contains("0"))){
				s.add(title+"#"+String.valueOf(price)+"#"+description+"#"+area_temp+"#"+url);
			}		
		}
//		DB db2 = mongoClient.getDB("xianyu");
//		DBCollection coll2 = db2.getCollection("shuma");
//		BasicDBObject query1 = new BasicDBObject(); 
//		Pattern pattern1 = Pattern.compile("^.*"+title_keyword+".*$", Pattern.CASE_INSENSITIVE);
//        query1.put("title", pattern1);  
//        BasicDBObject query2 = new BasicDBObject();  
//        Pattern pattern2 = Pattern.compile("^.*"+description_keyword+".*$", Pattern.CASE_INSENSITIVE);
//        query2.put("description", pattern2);
//        ArrayList<BasicDBObject> andQueryList1 = new ArrayList<BasicDBObject>();  
//        andQueryList1.add(query1);  
//        andQueryList1.add(query2);  
//        BasicDBObject andQuery1 = new BasicDBObject("$and", andQueryList1);
//        
//        BasicDBObject query3 = new BasicDBObject();  
//        Pattern pattern3 = Pattern.compile("^.*"+area+".*$", Pattern.CASE_INSENSITIVE);
//        query3.put("area", pattern3);  
//        BasicDBObject query4 = new BasicDBObject();  
//        query4.put("screen_flag", "0");
//        ArrayList<BasicDBObject> andQueryList2 = new ArrayList<BasicDBObject>();  
//        andQueryList2.add(query3);  
//        andQueryList2.add(query4);  
//        BasicDBObject andQuery2 = new BasicDBObject("$and", andQueryList2);
//        
//        ArrayList<BasicDBObject> andQueryCombinationList = new ArrayList<BasicDBObject>();  
//        andQueryCombinationList.add(andQuery1);  
//        andQueryCombinationList.add(andQuery2);  
//  
//        BasicDBObject finalQuery = new BasicDBObject("$and",  andQueryCombinationList);
//        DBCursor cursor2 = coll2.find();
//		while(cursor2.hasNext()){
//			Map data = cursor2.next().toMap();
//			String title = (String) data.get("title");
//			String description = (String) data.get("description");
//			double price=0;
//			if(isNumeric(data.get("price").toString())&&data.get("price").toString().length()>0){
//				price = Double.valueOf((String) data.get("price"));
//			}		
//			String area_temp = (String) data.get("area");
//			String flag = (String) data.get("screen_flag");
//			String url = data.get("url_info").toString();			
//			if(area_temp.length()==0)
//				area_temp="null";
//			if(title.length()==0)
//				title="null";
//			if(description.length()==0)
//				description="null";
//			if(price==0)
//				price = 0;
//			if(url.length()==0)
//				url="url";
//			if(title.contains(title_keyword)&&description.contains(description_keyword)&&area_temp.contains(area)&&(price>=price_down)&&(price<=price_up)&&(flag.contains("0"))){
//				s.add(title+"#"+String.valueOf(price)+"#"+description+"#"+area_temp+"#"+url);
//			}		
//		}	
		DB db2 = mongoClient.getDB("xianyu");
		DBCollection coll2 = db2.getCollection("shuma");
		DBCursor cursor2 = coll2.find();
		while(cursor2.hasNext()){
			Map data = cursor2.next().toMap();
			if(data.size()<=5)
				continue;
			String title = (String) data.get("title");
			String description = (String) data.get("description");
			double price=0;
			if(isNumeric(data.get("price").toString())&&data.get("price").toString().length()>0){
				price = Double.valueOf((String) data.get("price"));
			}		
			String area_temp = (String) data.get("area");
			String flag = (String) data.get("screen_flag");
			String url = data.get("url_info").toString();			
			if(area_temp.length()==0)
				area_temp="null";
			if(title.length()==0)
				title="null";
			if(description.length()==0)
				description="null";
			if(price==0)
				price = 0;
			if(url.length()==0)
				url="url";
			if(title.contains(title_keyword)&&description.contains(description_keyword)&&area_temp.contains(area)&&(price>=price_down)&&(price<=price_up)&&(flag.contains("0"))){
				s.add(title+"#"+String.valueOf(price)+"#"+description+"#"+area_temp+"#"+url);
			}		
		}	
//		System.out.println(s.size());
		mongoClient.close();
		return s;
    }
	
	public AbstractTableModel getTableModel() {                     //获取展示表格的数据模型
        return new AbstractTableModel() {  
            public int getColumnCount() {  
                return 6;  
            }  
            public int getRowCount() {  
                try {
					return getData().size();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;  
            }  
            public String getColumnName(int columnIndex){
            	String[] columns = { "编号", "标题", "价格", "描述", "地区","URL"};
            	return columns[columnIndex];
            	}
            public Object getValueAt(int row, int col) {  
                switch (col) {  
                case (0): {  
                    return row + 1;  
                }  
                case (1): {  
                    try {
						return getData().get(row).split("#", 0)[0];
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
                }  
                case (2): {  
                    try {
						return getData().get(row).split("#", 0)[1];
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
                }  
                case (3): {  
                    try {
						return getData().get(row).split("#", 0)[2];
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
                }  
                case (4): {  
                    try {
						return getData().get(row).split("#", 0)[3];
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
                }  
                case (5): {  
                    try {
						return getData().get(row).split("#", 0)[4];
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
                }  
                default:  
                    try {
						return getData().get(row).split("#", 0)[5];
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
                }
				return col;  
            }  
        };  
    }
	
	public boolean isNumeric(String str){                            //判断是否是数字，用于价格
        Pattern pattern = Pattern.compile("[0-9]*(.)*[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
 }

}
