package zip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.exception.ZipException;

public class ReadURLZip {
	
	public boolean[] check;
	
	//读取文件头
	public List<Map<String,Object>> readZipFile(String URL) 
			throws  IOException, FileNotFoundException, ZipException {  
        
    	List<String> list = new ArrayList<String>();
    	Map<String,Long> fileSize = new HashMap<String,Long>();
    	Map<String,Long> compSize = new HashMap<String,Long>();
    	
		List fileHeaderList =  Reader.read(URL);
    	
    	for (int i = 0; i < fileHeaderList.size(); i++) {
			FileHeader fileHeader = (FileHeader)fileHeaderList.get(i);
			
			list.add(fileHeader.getFileName());
			fileSize.put(fileHeader.getFileName(), fileHeader.getUncompressedSize());
			compSize.put(fileHeader.getFileName(), fileHeader.getCompressedSize());
		}
        
        //为便于理解打印list容器中所有条目；
        for(String a:list){
        	System.out.println(a);
        }
        
        List<Map<String, Object>> lists = getResult(list,fileSize,compSize);
        
        return lists;
    }

 	//读取文件信息，信息放在map结构里，众多的map放在lists里
	//这里需要大改，这一步做好之后没法分辨文件夹和文件
	private List<Map<String, Object>> getResult(List<String> list, 
			Map<String,Long> fileSize, Map<String,Long> compSize) {
		
		List<Map<String,Object>> lists = new ArrayList<Map<String,Object>>();
		
		int count = 1;
		
		for(String entryName : list) {
			
			String[] entryNames = entryName.split("/");
			
			//增加个标签，记录是否是文件，是文件的话就是true，是文件夹的话是false
			boolean isfile=false;
			
			int len=entryName.length();
			char chr=entryName.charAt(len-1);
			//如果最后一个字符是'/'的话就表示是文件夹
			if (chr != '/') isfile=true;
			
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
		}
	    return lists;
	}
 
	//判断在lists里面是否已经保存过这个path
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
	
	//返回此文件的上层文件夹的id
	private int getIdByPpath(String p_path, List<Map<String, Object>> lists) {
		
		for(Map<String, Object> map : lists) {

			String path =  (String) map.get("path");
			int id   =  (Integer)map.get("id");
			
			if(path.equals(p_path)) { return id;}
		}
		return 0;
	}
	
	//给lists排序，排成树形的结构
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
			
			int pid = (Integer)map.get("pid");
			//pid是0则说明是根目录
			if (pid==0) sort_lists.add(map);
			else continue;
			check[i]=true;
			
			//用递归的算法
			int id = (Integer)map.get("id");
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
