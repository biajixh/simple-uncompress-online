package zip;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class Test {
  public void unzipfile(String inputpath,String outpath,String zippath) throws ZipException{
		ZipFile zipFile = new ZipFile(inputpath);  
		zipFile.setFileNameCharset("GBK");
		@SuppressWarnings("rawtypes")
		List fileHeaderList = zipFile.getFileHeaders();  
		  
		String path = zippath;
		System.out.println(path);
		Pattern pattern = Pattern.compile("^" +path +".*");
		for (int i = 0; i < fileHeaderList.size(); i++){
			FileHeader fileHeader = (FileHeader)fileHeaderList.get(i);
			System.out.println(fileHeader.getFileName());
			Matcher matcher = pattern.matcher(fileHeader.getFileName());
        	boolean b= matcher.matches();
        	System.out.println(b);
        	if(b){
        		zipFile.extractFile(fileHeader.getFileName(), outpath); 
        	}
		}
		System.out.println("finished!");
	}
}
