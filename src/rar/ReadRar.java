package rar;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

public class ReadRar {
	
	public boolean[] check;

		
//		String path1 = new String("D:/The World Download.rar");
		
//		List<Map<String, Object>> list1 = new ReadRar().readRarFile(path1);
//		List<Map<String, Object>> list2 = new ReadRar().sortList(list1);
		
		/*System.out.println("输出ZIP包结构：");
	    for(Map<String,Object> map : list1) {
	    	System.out.println(map.toString());
	    }*/
	    
//	    System.out.println("输出整理后结构：");
//	    for(Map<String,Object> map : list2) {
//	    	System.out.println(map.toString());
//	    }
	
	
	//读取文件头
		public List<Map<String,Object>> readRarFile(String path1) 
				throws  IOException, FileNotFoundException, RarException {  
	        
//			List<String> list = new ArrayList<String>();
//			List<Map<String, Object>> list1 = new ReadRar().readRarFile(path1);
//			List<Map<String, Object>> list2 = new ReadRar().sortList(list1);
			Map<String,Long> fileSize = new HashMap<String,Long>();     //用来存放压缩包中文件条目以及对应的文件大小
	    	Map<String,Long> compSize = new HashMap<String,Long>();		//用来存放压缩包中文件条目以及压缩大小
			
	    	List<Map<String,Object>> lists = new ArrayList<Map<String,Object>>();
	    	
	    	int count = 1;
	    	
	    	File rf = new File(path1);
	    	Archive a = new Archive(rf);
	    	
	    	if (a != null) {
                FileHeader fh = a.nextFileHeader();
                boolean isfile=false;
                
                while (fh != null) {
                	
                	String entryName;
                	
                    if (fh.isDirectory()) { // 文件夹 
                        entryName=fh.getFileNameString();
                        isfile=false;
                    } else { // 文件
                    	entryName=fh.getFileNameString().trim();
                    	isfile=true;
                    }
                    
                    entryName = entryName.replace("\\","/");
                    String[] entryNames = entryName.split("/");
                    
                    for(int i = 0 ;i < entryNames.length;i++) {

        				String name = entryNames[i];	
        				
        				String p_path = "";				
        				String path   = "";	
        				
        				boolean thisisfile;
        				
        				for(int j = 0 ;j < i;j++) {
        					//p_path指的是上级目录
        					path   += entryNames[j] + "/";
        					p_path += entryNames[j] + "/";
        				}

        				if (i<(entryNames.length-1) || isfile == false)
        				{
        					//如果还没有读到entryNames的最后一个元素，证明目前的还是文件夹
        					path += name + "/";
        					thisisfile=false;
        				}
        				else
        				{
        					path += name;
        					thisisfile=true;
        				}
        				
        				//把信息插入到lists中
        				if(!exist(path,lists)) {		
        					
        					Map<String,Object> map = new HashMap<String,Object>();
        					
        					map.put("id"    , count++);						//int
        					map.put("pid"   , getIdByPpath(p_path,lists));	//int
        					map.put("path"  , path);						//String
        					map.put("p_path", p_path);						//String
        					map.put("name"  , name);						//String
        					map.put("isfile", thisisfile);					//boolean
        					map.put("fileSize", fileSize.get(path));		//Long
        					map.put("compSize", compSize.get(path));		//Long
        					
        					lists.add(map);
        				}
        			}
                    
                    fh = a.nextFileHeader();
                }
                a.close();
            }
	        
	        return lists;
	    }
		
		private boolean exist(String path, List<Map<String, Object>> lists) {
			
			boolean flag = false;
			
			for(Map<String, Object> map : lists) {

				String _path =  (String)map.get("path");
				
				if(path.equals(_path)) {
					flag = true;
				}
			}
			return flag;
		}  
		
		private int getIdByPpath(String p_path, List<Map<String, Object>> lists) {
			
			for(Map<String, Object> map : lists) {

				String path =  (String) map.get("path");
				int id   =  (Integer)map.get("id");
				
				if(path.equals(p_path)) { return id;}
			}
			return 0;
		}
		
		public List<Map<String, Object>> sortList(List<Map<String, Object>> list1)
		{
			int len=list1.size();
			List<Map<String,Object>> sort_lists = new ArrayList<Map<String,Object>>();
			
			//check是全局变量，用来记录是否已经加入sort_lists
			check=new boolean[len];
			for (int i=0;i<len;i++)
			{
				check[i]=false;
			}
			
			for (int i=0;i<len;i++)
			{
				Map<String,Object> map=list1.get(i);
				
				int pid=(Integer)map.get("pid");
				//pid是0则说明是根目录
				if (pid==0) sort_lists.add(map);
				else continue;
				check[i]=true;
				
				//用递归的算法
				int id=(Integer)map.get("id");
				sort_lists=find(list1,sort_lists,id);
			}
			
			return sort_lists;
		}
		
		private List<Map<String, Object>> find(List<Map<String, Object>> list1,
				List<Map<String, Object>> sort_lists,int id)
		{
			for (int i=0;i<list1.size();i++)
			{
				if(check[i]==false)
				{
					Map<String,Object> map=list1.get(i);
					
					int pid=(Integer)map.get("pid");
					//判断此map的上层pid是否等于传入的id
					if (pid==id) sort_lists.add(map);
					else continue;
					check[i]=true;
					int nid=(Integer)map.get("id");
					sort_lists=find(list1,sort_lists,nid);
				}
			}
			
			return sort_lists;
		}
		
		public List<Map<String, String>> pathlist(List<Map<String, Object>> list1){
			 List<Map<String, String>> pathlist = new ArrayList<Map<String, String>>();
			 for(Map<String, Object> map1:list1){
				 Map<String, String> map = new HashMap<String, String>();
				 String path = (String)map1.get("path");
				 map.put("path",path);
				 pathlist.add(map);
			 }
			 return pathlist;
		}

}