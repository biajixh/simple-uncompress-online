package zip;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class URLUnzip {
	void unzipfile(String inputpath,String outpath,String zippath) throws ZipException, IOException{
		
		List fileHeaderList = Reader.read(inputpath);  
		  
		String path = zippath;
		Pattern pattern = Pattern.compile("^" +path +".*");
		for (int i = 0; i < fileHeaderList.size(); i++){
			FileHeader fileHeader = (FileHeader)fileHeaderList.get(i);
			System.out.println(fileHeader.getFileName());
			Matcher matcher = pattern.matcher(fileHeader.getFileName());
        	boolean b= matcher.matches();
        	System.out.println(b);
        	if(b){
        		Reader.extractfile(inputpath,fileHeader.getFileName(), outpath); 
        	}
		}
		System.out.println("finished!");
	}
}
