package zip;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.exception.ZipExceptionConstants;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.DigitalSignature;
import net.lingala.zip4j.model.EndCentralDirRecord;
import net.lingala.zip4j.model.ExtraDataRecord;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Raw;
import net.lingala.zip4j.util.Zip4jUtil;

import sun.net.www.protocol.http.HttpURLConnection;
import zip.Extract;


public class Reader {
	
	static RandomAccessFile raf;
	//read方法读取fileheadlist
	public static List read(String URL) throws IOException, ZipException{
		String Url = URL;//url
		URL url = new URL(Url);
		HttpURLConnection huc = (HttpURLConnection)url.openConnection();
		long length = huc.getContentLength();//得到url指定资源的字节数
		long pos = length - 3000;
		
		CreateTemporaryFile(Url,"F:/ls.txt",pos,(length-1));//首先提取文件尾部3000个字节的内容
		
		System.out.println(length);
		System.out.println(pos);
		
		File f = new File("F:/ls.txt"); 
		raf = new RandomAccessFile(f,"rw");
		 
		EndCentralDirRecord ecdr = readEndOfCentralDirectoryRecord(raf);//解析EndOfCentralDirectoryRecord
		long cdoffset;
		if((length-ecdr.getOffsetOfStartOfCentralDir()) < 3000){//如果核心目录的偏移量在尾部3000字节内，计算其在临时文件的偏移量
			cdoffset = 3000-22-ecdr.getSizeOfCentralDir()-ecdr.getCommentLength();
		}else{//否则重新发出http请求，读取从核心目录的偏移量开始的文件内容覆盖原来的临时文件，将偏移量置零
			CreateTemporaryFile(Url,"F:/ls.txt",ecdr.getOffsetOfStartOfCentralDir(),(length-1));
			cdoffset = 0;
		}
		
		CentralDirectory cd = readCentralDirectory(raf,ecdr,cdoffset);//解析CentralDirectory
		
		ArrayList headerlist = cd.getFileHeaders();//得到fileHeader
		return headerlist;
	}
	//通过具体的名称读取解压指定文件
	public static void extractfile(String URL,String filename,String outpath) throws IOException, ZipException{
		
		String url = URL;
		String fn = filename;
		String op = outpath;
		
		List headerlist = Reader.read(url);
		
		long localoffset = 0;
		int filenamelength = 0;
		int ExtraFieldLength = 0;
		long CompressedSize = 0;
		
		for (int i = 0; i <headerlist.size(); i++) {
			FileHeader fileHeader = (FileHeader)headerlist.get(i);
			if(fileHeader.getFileName().equalsIgnoreCase(fn)){
				localoffset = fileHeader.getOffsetLocalHeader();
				filenamelength = fileHeader.getFileNameLength();
				ExtraFieldLength = fileHeader.getExtraFieldLength();
				CompressedSize = fileHeader.getCompressedSize();
				break;
			}
		}
		
		long datapos = localoffset;
		long datalength = localoffset+CompressedSize+ExtraFieldLength+filenamelength+30;

		CreateTemporaryFile(url,"F:/ls.zip",datapos,datalength);
		Extract.unzipstream("F:/ls.zip",op);
	}
	//请求文件的指定内容，并创建本地临时文件（F:/w/ls.zip）
	//url指定的url地址ַ
	//pos
	private static void CreateTemporaryFile(String url,String name,long pos,long length) throws ClientProtocolException, IOException{
		
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpclient = httpClientBuilder.build();
		OutputStream out = new FileOutputStream(name);
		HttpGet httpget = new HttpGet(url);
		BasicHttpContext context = new BasicHttpContext();
		
		httpget.addHeader("Range", "bytes="+pos+"-"+length);
		CloseableHttpResponse response = httpclient.execute(httpget, context);
		BufferedInputStream in = new BufferedInputStream(response.getEntity().getContent());
		
		System.out.println(response.getEntity().getContentType());
		
		int count = 0;
		while((count = in.read())!= -1){//生成临时文件
			out.write(count);
		}
		
		in.close();
		out.close();
		httpclient.close();
	}

	//在临时文件里读取EndCentralDirRecord并解析
	private static EndCentralDirRecord readEndOfCentralDirectoryRecord(RandomAccessFile raf) throws ZipException {
		
	    RandomAccessFile zip4jRaf = raf;
		if (zip4jRaf == null) {
			throw new ZipException("random access file was null", ZipExceptionConstants.randomAccessFileNull);
		}
		
		try {
			byte[] ebs  = new byte[4];
			long pos = zip4jRaf.length() - InternalZipConstants.ENDHDR;
			
			EndCentralDirRecord endCentralDirRecord = new EndCentralDirRecord();
			int counter = 0;
			do {
				zip4jRaf.seek(pos--);
				counter++;
			} while ((Raw.readLeInt(zip4jRaf, ebs) != InternalZipConstants.ENDSIG) && counter <= 3000);
			
			if ((Raw.readIntLittleEndian(ebs, 0) != InternalZipConstants.ENDSIG)) {
				throw new ZipException("zip headers not found. probably not a zip file");
			}
			byte[] intBuff = new byte[4];
			byte[] shortBuff = new byte[2];			
			
			//End of central record signature
			endCentralDirRecord.setSignature(InternalZipConstants.ENDSIG);
			
			//number of this disk
			readIntoBuff(zip4jRaf, shortBuff);
			endCentralDirRecord.setNoOfThisDisk(Raw.readShortLittleEndian(shortBuff, 0));
			
			//number of the disk with the start of the central directory
			readIntoBuff(zip4jRaf, shortBuff);
			endCentralDirRecord.setNoOfThisDiskStartOfCentralDir(Raw.readShortLittleEndian(shortBuff, 0));
			
			//total number of entries in the central directory on this disk
			readIntoBuff(zip4jRaf, shortBuff);
			endCentralDirRecord.setTotNoOfEntriesInCentralDirOnThisDisk(Raw.readShortLittleEndian(shortBuff, 0));
			
			//total number of entries in the central directory
			readIntoBuff(zip4jRaf, shortBuff);
			endCentralDirRecord.setTotNoOfEntriesInCentralDir(Raw.readShortLittleEndian(shortBuff, 0));
			
			//size of the central directory
			readIntoBuff(zip4jRaf, intBuff);
			endCentralDirRecord.setSizeOfCentralDir(Raw.readIntLittleEndian(intBuff, 0));
			
			//offset of start of central directory with respect to the starting disk number
			readIntoBuff(zip4jRaf, intBuff);
			byte[] longBuff = getLongByteFromIntByte(intBuff);
			endCentralDirRecord.setOffsetOfStartOfCentralDir(Raw.readLongLittleEndian(longBuff, 0));
			
			//.ZIP file comment length
			readIntoBuff(zip4jRaf, shortBuff);
			int commentLength = Raw.readShortLittleEndian(shortBuff, 0);
			endCentralDirRecord.setCommentLength(commentLength);
			
			//.ZIP file comment 
			if (commentLength > 0) {
				byte[] commentBuf = new byte[commentLength];
				readIntoBuff(zip4jRaf, commentBuf);
				endCentralDirRecord.setComment(new String(commentBuf));
				endCentralDirRecord.setCommentBytes(commentBuf);
			} else {
				endCentralDirRecord.setComment(null);
			}
			
			return endCentralDirRecord;
		} catch (IOException e) {
			throw new ZipException("Probably not a zip file or a corrupted zip file", e, ZipExceptionConstants.notZipFile);
		}
	}
	//在临时文件里读取CentralDirRecord并解析
	private static CentralDirectory readCentralDirectory(RandomAccessFile raf,EndCentralDirRecord ecdr,long pos) throws ZipException {
		RandomAccessFile zip4jRaf = raf;
		
		if (zip4jRaf == null) {
			throw new ZipException("random access file was null", ZipExceptionConstants.randomAccessFileNull);
		}
		
		try {
			CentralDirectory centralDirectory = new CentralDirectory();
			ArrayList fileHeaderList = new ArrayList();
			
			EndCentralDirRecord endCentralDirRecord = ecdr;
			int centralDirEntryCount = endCentralDirRecord.getTotNoOfEntriesInCentralDir();
			
			
			zip4jRaf.seek(pos);
			
			byte[] intBuff = new byte[4];
			byte[] shortBuff = new byte[2];
			byte[] longBuff = new byte[8];
			
			for (int i = 0; i < centralDirEntryCount; i++) {
				FileHeader fileHeader = new FileHeader();
				
				//FileHeader Signature
				readIntoBuff(zip4jRaf, intBuff);
				int signature = Raw.readIntLittleEndian(intBuff, 0);
				if (signature != InternalZipConstants.CENSIG) {
					throw new ZipException("Expected central directory entry not found (#" + (i + 1) + ")");
				}
				fileHeader.setSignature(signature);
				
				//version made by
				readIntoBuff(zip4jRaf, shortBuff);
				fileHeader.setVersionMadeBy(Raw.readShortLittleEndian(shortBuff, 0));
				
				//version needed to extract
				readIntoBuff(zip4jRaf, shortBuff);
				fileHeader.setVersionNeededToExtract(Raw.readShortLittleEndian(shortBuff, 0));
				
				//general purpose bit flag
				readIntoBuff(zip4jRaf, shortBuff);
				fileHeader.setFileNameUTF8Encoded((Raw.readShortLittleEndian(shortBuff, 0) & InternalZipConstants.UFT8_NAMES_FLAG) != 0);
				int firstByte = shortBuff[0];
				int result = firstByte & 1;
				if (result != 0) {
					fileHeader.setEncrypted(true);
				}
				fileHeader.setGeneralPurposeFlag((byte[])shortBuff.clone());
				
				//Check if data descriptor exists for local file header
				fileHeader.setDataDescriptorExists(firstByte>>3 == 1);
				
				//compression method
				readIntoBuff(zip4jRaf, shortBuff);
				fileHeader.setCompressionMethod(Raw.readShortLittleEndian(shortBuff, 0));
				
				//last mod file time
				readIntoBuff(zip4jRaf, intBuff);
				fileHeader.setLastModFileTime(Raw.readIntLittleEndian(intBuff, 0));
				
				//crc-32
				readIntoBuff(zip4jRaf, intBuff);
				fileHeader.setCrc32(Raw.readIntLittleEndian(intBuff, 0));
				fileHeader.setCrcBuff((byte[])intBuff.clone());
				
				//compressed size
				readIntoBuff(zip4jRaf, intBuff);
				longBuff = getLongByteFromIntByte(intBuff);
				fileHeader.setCompressedSize(Raw.readLongLittleEndian(longBuff, 0));
				
				//uncompressed size
				readIntoBuff(zip4jRaf, intBuff);
				longBuff = getLongByteFromIntByte(intBuff);
				fileHeader.setUncompressedSize(Raw.readLongLittleEndian(longBuff, 0));
				
				//file name length
				readIntoBuff(zip4jRaf, shortBuff);
				int fileNameLength = Raw.readShortLittleEndian(shortBuff, 0);
				fileHeader.setFileNameLength(fileNameLength);
				
				//extra field length
				readIntoBuff(zip4jRaf, shortBuff);
				int extraFieldLength = Raw.readShortLittleEndian(shortBuff, 0);
				fileHeader.setExtraFieldLength(extraFieldLength);
				
				//file comment length
				readIntoBuff(zip4jRaf, shortBuff);
				int fileCommentLength = Raw.readShortLittleEndian(shortBuff, 0);
				fileHeader.setFileComment(new String(shortBuff));
				
				//disk number start 
				readIntoBuff(zip4jRaf, shortBuff);
				fileHeader.setDiskNumberStart(Raw.readShortLittleEndian(shortBuff, 0));
				
				//internal file attributes
				readIntoBuff(zip4jRaf, shortBuff);
				fileHeader.setInternalFileAttr((byte[])shortBuff.clone());
				
				//external file attributes
				readIntoBuff(zip4jRaf, intBuff);
				fileHeader.setExternalFileAttr((byte[])intBuff.clone());
				
				//relative offset of local header
				readIntoBuff(zip4jRaf, intBuff);
				//Commented on 26.08.2010. Revert back if any issues
				//fileHeader.setOffsetLocalHeader((Raw.readIntLittleEndian(intBuff, 0) & 0xFFFFFFFFL) + zip4jRaf.getStart());
				longBuff = getLongByteFromIntByte(intBuff);
				fileHeader.setOffsetLocalHeader((Raw.readLongLittleEndian(longBuff, 0) & 0xFFFFFFFFL));
				
				if (fileNameLength > 0) {
					byte[] fileNameBuf = new byte[fileNameLength];
					readIntoBuff(zip4jRaf, fileNameBuf);
					// Modified after user reported an issue http://www.lingala.net/zip4j/forum/index.php?topic=2.0
//					String fileName = new String(fileNameBuf, "Cp850"); 
					// Modified as per http://www.lingala.net/zip4j/forum/index.php?topic=41.0
//					String fileName = Zip4jUtil.getCp850EncodedString(fileNameBuf);
					
					String fileName = null;
					
					if (Zip4jUtil.isStringNotNullAndNotEmpty("GBK")) {
						fileName = new String(fileNameBuf, "GBK");
					} else {
						fileName = Zip4jUtil.decodeFileName(fileNameBuf, fileHeader.isFileNameUTF8Encoded());
					}
					
					if (fileName == null) {
						throw new ZipException("fileName is null when reading central directory");
					}
					
					if (fileName.indexOf(":" + System.getProperty("file.separator")) >= 0) {
						fileName = fileName.substring(fileName.indexOf(":" + System.getProperty("file.separator")) + 2);
					}
					
					fileHeader.setFileName(fileName);
					fileHeader.setDirectory(fileName.endsWith("/") || fileName.endsWith("\\"));
					
				} else {
					fileHeader.setFileName(null);
				}
				
				//Extra field
				readAndSaveExtraDataRecord(fileHeader);
				
//				if (fileHeader.isEncrypted()) {
//					
//					if (fileHeader.getEncryptionMethod() == ZipConstants.ENC_METHOD_AES) {
//						//Do nothing
//					} else {
//						if ((firstByte & 64) == 64) {
//							//hardcoded for now
//							fileHeader.setEncryptionMethod(1);
//						} else {
//							fileHeader.setEncryptionMethod(ZipConstants.ENC_METHOD_STANDARD);
//							fileHeader.setCompressedSize(fileHeader.getCompressedSize()
//									- ZipConstants.STD_DEC_HDR_SIZE);
//						}
//					}
//					
//				}
				
				if (fileCommentLength > 0) {
					byte[] fileCommentBuf = new byte[fileCommentLength];
					readIntoBuff(zip4jRaf, fileCommentBuf);
					fileHeader.setFileComment(new String(fileCommentBuf));
				}
				
				fileHeaderList.add(fileHeader);
			}
			centralDirectory.setFileHeaders(fileHeaderList);
			
			//Digital Signature
			DigitalSignature digitalSignature = new DigitalSignature();
			readIntoBuff(zip4jRaf, intBuff);
			int signature = Raw.readIntLittleEndian(intBuff, 0);
			if (signature != InternalZipConstants.DIGSIG) {
				return centralDirectory;
			}
			
			digitalSignature.setHeaderSignature(signature);
			
			//size of data
			readIntoBuff(zip4jRaf, shortBuff);
			int sizeOfData = Raw.readShortLittleEndian(shortBuff, 0);
			digitalSignature.setSizeOfData(sizeOfData);
			
			if (sizeOfData > 0) {
				byte[] sigDataBuf = new byte[sizeOfData];
				readIntoBuff(zip4jRaf, sigDataBuf);
				digitalSignature.setSignatureData(new String(sigDataBuf));
			}
			
			return centralDirectory;
		} catch (IOException e) {
			throw new ZipException(e);
		}
	}
	/**
	 * Reads buf length of bytes from the input stream to buf
	 * @param zip4jRaf
	 * @param buf
	 * @return byte array
	 * @throws ZipException
	 */
	private static byte[] readIntoBuff(RandomAccessFile zip4jRaf, byte[] buf) throws ZipException {
		try {
			if (zip4jRaf.read(buf, 0, buf.length) != -1) {
				return buf;
			} else {
				throw new ZipException("unexpected end of file when reading short buff");
			}
		} catch (IOException e) {
			throw new ZipException("IOException when reading short buff", e);
		}
	}
	/**
	 * Returns a long byte from an int byte by appending last 4 bytes as 0's
	 * @param intByte
	 * @return byte array
	 * @throws ZipException
	 */
	private static byte[] getLongByteFromIntByte(byte[] intByte) throws ZipException {
		if (intByte == null) {
			throw new ZipException("input parameter is null, cannot expand to 8 bytes");
		}
		
		if (intByte.length != 4) {
			throw new ZipException("invalid byte length, cannot expand to 8 bytes");
		}
		
		byte[] longBuff = {intByte[0], intByte[1], intByte[2], intByte[3], 0, 0, 0, 0};
		return longBuff;
	}
	/**
	 * Reads extra data record and saves it in the {@link FileHeader}
	 * @param fileHeader
	 * @throws ZipException
	 */
	private static void readAndSaveExtraDataRecord(FileHeader fileHeader) throws ZipException {
		
		if (fileHeader == null) {
			throw new ZipException("file header is null");
		}
		
		int extraFieldLength = fileHeader.getExtraFieldLength(); 
		if (extraFieldLength <= 0) {
			return;
		}
		
		fileHeader.setExtraDataRecords(readExtraDataRecords(extraFieldLength));
		
	}

	/**
	 * Reads extra data records
	 * @param extraFieldLength
	 * @return ArrayList of {@link ExtraDataRecord}
	 * @throws ZipException
	 */
	private static ArrayList readExtraDataRecords(int extraFieldLength) throws ZipException {
		
		if (extraFieldLength <= 0) {
			return null;
		}
		
		try {
			byte[] extraFieldBuf = new byte[extraFieldLength];
			raf.read(extraFieldBuf);
			
			int counter = 0;
			ArrayList extraDataList = new ArrayList();
			while(counter < extraFieldLength) {
				ExtraDataRecord extraDataRecord = new ExtraDataRecord();
				int header = Raw.readShortLittleEndian(extraFieldBuf, counter);
				extraDataRecord.setHeader(header);
				counter = counter + 2;
				int sizeOfRec = Raw.readShortLittleEndian(extraFieldBuf, counter);
				
				if ((2 + sizeOfRec) > extraFieldLength) {
					sizeOfRec = Raw.readShortBigEndian(extraFieldBuf, counter);
					if ((2 + sizeOfRec) > extraFieldLength) {
						//If this is the case, then extra data record is corrupt
						//skip reading any further extra data records
						break;
					}
				}
				
				extraDataRecord.setSizeOfData(sizeOfRec);
				counter = counter + 2;
				
				if (sizeOfRec > 0) {
					byte[] data = new byte[sizeOfRec]; 
					System.arraycopy(extraFieldBuf, counter, data, 0, sizeOfRec);
					extraDataRecord.setData(data);
				}
				counter = counter + sizeOfRec;
				extraDataList.add(extraDataRecord);
			}
			if (extraDataList.size() > 0) {
				return extraDataList;
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new ZipException(e);
		}
	}
	
}

