package rar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

public class UnRar {
	public void unRarFile(String srcFilePath,String dstFilePath,String selected) throws RarException, IOException {
		// TODO Auto-generated method stub
		File file = new File(srcFilePath);
		Archive archive = new Archive(file);
		FileHeader fh = null;

		
		String path = selected.replace("/", "\\");

		String fileName = "";
		while((fh=archive.nextFileHeader())!=null){

			fileName = fh.getFileNameW().isEmpty()?fh.getFileNameString():fh.getFileNameW();
			System.out.println(path);
			System.out.println(fileName);

			boolean b = path.equals(fileName);
        	System.out.println(b);
        	if(b){
        	if(fh.isDirectory()){//文件夹
				File fol = new File(dstFilePath+File.separator+fileName);
				fol.mkdirs();
			}else{//文件
				File out = new File(dstFilePath+File.separator+fileName.trim());
				
					if(!out.exists()){
						if(!out.getParentFile().exists()){ //相对路径可能多级，需要创建父目录
							out.getParentFile().mkdirs();
						}
						out.createNewFile();
					
        	FileOutputStream os = new FileOutputStream(out);
        	archive.extractFile(fh, os);
        	}
        	fh = archive.nextFileHeader();
		}
	}
		}
	}
}
