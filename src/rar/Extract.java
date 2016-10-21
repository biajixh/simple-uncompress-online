package rar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;

import rar.Extract;
import rar.Extract.BoundedInputStream;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

public class Extract {
public static void ecxtractfile(String URL, String outpath, String filename) throws ClientProtocolException, IOException, RarException{
		
		File file=new File("F:/ls");   //压缩文件
  		Archive a=new Archive(file);
        FileHeader fh=null;
        
		RandomAccessFile raf = new RandomAccessFile("F:/ls", "rw");
		List<FileHeader> list = a.getFileHeaders(); 
		
		long positionInFile = 0;
		long starpos = 0;
		long unpPackedSize = 0;
            
		for(FileHeader fileHeader : list){
			String entryName;
        	
            if (fileHeader.isDirectory()) {
                entryName=fileHeader.getFileNameString();
                //isfile=false;
            } else {
            	entryName=fileHeader.getFileNameString().trim();
            	//isfile=true;
            }
            
            if(entryName.equalsIgnoreCase(filename)){
            	positionInFile = fileHeader.getPositionInFile();
            	starpos = positionInFile+fileHeader.getHeaderSize();
            	unpPackedSize = fileHeader.getFullPackSize();
            }
            System.out.println(entryName);
		}
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpClient = httpClientBuilder.build();
		HttpGet httpget = new HttpGet(URL);
		BasicHttpContext context = new BasicHttpContext();
		
		httpget.addHeader("Range","bytes="+starpos+"-"+(starpos+unpPackedSize));
		CloseableHttpResponse response = httpClient.execute(httpget, context);
		BufferedInputStream in = new BufferedInputStream(response.getEntity().getContent());
		
		raf.seek(starpos);
		
		int b;
		while((b = in.read()) != -1){
			raf.write(b);
		}
		
		in.close();
		raf.close();
		httpClient.close();
		
		File fout = new File(outpath + filename);
		if(!fout.getParentFile().exists())
			fout.getParentFile().mkdirs();
           
        InputStream is=null;
        FileOutputStream fos=new FileOutputStream(fout); //解压后文件
        
        //读取目标文件流
        while((fh=a.nextFileHeader())!=null){
        	if(!fh.isDirectory()){
        	String name=fh.getFileNameString().trim();
        	
        		if(name.equals(filename)){
        			is=a.getInputStream(fh);
        			int len=0;
        			byte buy[]=new byte[1024];
        			while((len=is.read(buy))!=-1)
        				fos.write(buy, 0, len);
        		}	        		
        	}	        			        	
        }	        
        is.close();
        fos.close();
        System.out.println("解压完成");
		System.out.println(positionInFile);
		System.out.println(starpos);
		System.out.println(unpPackedSize);
}

public static void unzipstream (String name,String outpath) throws IOException{
	File f = new File(name);
	Extract et = new Extract(f);
	
	InputStream s = new FileInputStream(f);
	et.readZipFromStream(s,outpath);
			
}
	
private File f;
private boolean isStore;
	
public Extract(File f){
	this.f = f;
}

//�ֽ�����ת16����	
public static String bytes2HexString(byte[] b) {  
    String ret = "";  
    for (int i = 0; i < b.length; i++) {  
        String hex = Integer.toHexString(b[ i ] & 0xFF);  
    if (hex.length() == 1) {  
        hex = '0' + hex;  
    }  
     ret += hex.toUpperCase();  
  }  
  return ret;  
} 

//�ж��ļ�ͷ����־Ϊ0x04034b50
public static boolean isHeader(byte[] b){
	if(b.length >= 4)
		if((b[0] == 0x50) && (b[1] == 0x4b) && (b[2] == 0x03) && (b[3] == 0x04))
			return true;
	return false;
}

//�ж�centraldirectory����־Ϊ0x02014b50
public static boolean isCentralHeader(byte[] b){
	if(b.length >= 4)
		if((b[0] == 0x50) && (b[1] == 0x4b) && (b[2] == 0x01) && (b[3] == 0x02))
			return true;
	return false;
}

//byte����תint��	offsetΪƫ������lenΪ�ֽ���
public static int byteArrayToInt(byte[] b, int offset, int len) {
    int value = 0;
    for (int i = 0; i < len; ++i) {
        int shift = i * 8;
        value += (b[i + offset] & 0x000000FF) << shift;
    }
    return value;
}

//����Zip�ļ���	
public void readZipFromStream(InputStream s,String outpath) throws UnsupportedEncodingException, IOException{
	
	int len = 30;						//header���ļ���ƫ����Ϊ30
//	long start = 0;
	byte[] b1 = new byte[30];

	String encode;
	s.read(b1, 0, len);
//		start += len;
	if(isHeader(b1)){
		isStore = false;
		int size = byteArrayToInt(b1, 18, 4);//ѹ�����ļ���С
		int fnsize = byteArrayToInt(b1, 26, 2);//�ļ����
		int extra = byteArrayToInt(b1, 28, 2);//��չ���
		byte[] name = new byte[fnsize];
		s.read(name);					//��ȡ�ļ���
//			start += fnsize;
		String filename;
//			if((b1[7] & 0x08) != 0)
//				encode = "UTF-8";
//			else
//				encode = "GBK";
		if(b1[8] == 0 && b1[9] == 0)
			isStore = true;
		filename = new String(name, "GBK");
		System.out.println(filename);
		System.out.println("�ļ���С��"+size);
//			start += extra;
		len = extra + size;
		s.skip(extra);
		len = 30;
		//String findname="index.jsp";     //ָ���ļ�
		if(size > 0){
			InputStream is = getInputStream(0, size,s);
			File fout = new File(outpath + filename);
			if(!fout.getParentFile().exists())
				fout.getParentFile().mkdirs();
			FileOutputStream fileOut = new FileOutputStream(outpath + filename); 
			int readedBytes;
			byte[] b2 = new byte[1024];
			 while(( readedBytes = is.read(b2) ) != -1){ 
                    fileOut.write(b2 , 0 , readedBytes ); 
                }  
            
            fileOut.close(); 	
            is.close(); 
		//System.out.println("1111");
//			start += size;
	}
	
	else{
		if(isCentralHeader(b1)){
			s.close();
		}
	}
		//System.out.println("2222");
}System.out.println("23333");
}

	
public InputStream getInputStream(long off, long sz, InputStream is) throws IOException{
	BoundedInputStream bis = new BoundedInputStream(off, sz, is);
	if(this.isStore)
		return bis;
	bis.addDummy();
	final Inflater inflater = new Inflater(true);
	return new InflaterInputStream(bis, inflater){
        @Override
        public void close() throws IOException {
            super.close();
            inflater.end();
        }
    };
}

public class BoundedInputStream extends InputStream {
    private long remaining;
    private long loc;
    private boolean addDummyByte = false;
    InputStream s;

    BoundedInputStream(final long start, final long remaining, InputStream is) throws IOException {
        this.remaining = remaining;
        loc = start;
        this.s = is;
        s.skip(loc);
    }

    @Override
    public int read() throws IOException {
        if (remaining-- <= 0) {
            if (addDummyByte) {
                addDummyByte = false;
                return 0;
            }
            return -1;
        }
        synchronized (s) {
//            s.skip(loc++);
            return s.read();
        }
    }

    @Override
    public int read(final byte[] b, final int off, int len) throws IOException {
        if (remaining <= 0) {
            if (addDummyByte) {
                addDummyByte = false;
                b[off] = 0;
                return 1;
            }
            return -1;
        }

        if (len <= 0) {
            return 0;
        }

        if (len > remaining) {
            len = (int) remaining;
        }
        int ret = -1;
        synchronized (s) {
//            s.skip(loc);
            ret = s.read(b, off, len);
        }
        if (ret > 0) {
            loc += ret;
            remaining -= ret;
        }
        return ret;
    }

    /**
     * Inflater needs an extra dummy byte for nowrap - see
     * Inflater's javadocs.
     */
    void addDummy() {
        addDummyByte = true;
    }
}
}
