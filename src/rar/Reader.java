package rar;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;

import sun.net.www.protocol.http.HttpURLConnection;
import rar.Extract;
import rar.Reader;

import com.github.junrar.exception.RarException;
import com.github.junrar.exception.RarException.RarExceptionType;
import com.github.junrar.rarfile.AVHeader;
import com.github.junrar.rarfile.BaseBlock;
import com.github.junrar.rarfile.BlockHeader;
import com.github.junrar.rarfile.CommentHeader;
import com.github.junrar.rarfile.EAHeader;
import com.github.junrar.rarfile.EndArcHeader;
import com.github.junrar.rarfile.FileHeader;
import com.github.junrar.rarfile.MacInfoHeader;
import com.github.junrar.rarfile.MainHeader;
import com.github.junrar.rarfile.MarkHeader;
import com.github.junrar.rarfile.ProtectHeader;
import com.github.junrar.rarfile.SignHeader;
import com.github.junrar.rarfile.SubBlockHeader;
import com.github.junrar.rarfile.UnixOwnersHeader;
import com.github.junrar.rarfile.UnrarHeadertype;

public class Reader {
	
	static int count=0;
	
	public static List<FileHeader> read(String URL) throws ClientProtocolException, IOException, RarException {
		String Url = URL;
//		String filename = "word2.txt";
		URL url = new URL(Url);
		HttpURLConnection huc = (HttpURLConnection)url.openConnection();
		
		RandomAccessFile raf = new RandomAccessFile("F:/ls", "rw");
		
		long length = huc.getContentLength();//得到url指定资源的字节数
		long pos=0;
		
		System.out.println(length);
		System.out.println(pos);
		
		List<BaseBlock> headers=readHeaders(length, pos, Url,raf);
		raf.close();
		
		System.out.println("Finish");
		List<FileHeader> list = new ArrayList<FileHeader>();
		
		for (BaseBlock block : headers) {
			if (block.getHeaderType().equals(UnrarHeadertype.FileHeader)) {
				list.add((FileHeader) block);
			}
		}
		return list;
	}
	
	private static List<BaseBlock> readHeaders(long fileLength, long pos, String url,RandomAccessFile raf) throws IOException, RarException {
		MarkHeader markHead = null;
		MainHeader newMhd = null;
		List<BaseBlock> headers = new ArrayList<BaseBlock>();
		//int currentHeaderIndex = 0;
		int toRead = 0;
		
		
		
		long position = pos;
		
		//httpget.addHeader("Range", "bytes="+position+"-"+(position+BaseBlock.BaseBlockSize-1));

		while (true) {
			//int size = 0;
			long newpos = 0;
			byte[] baseBlockBuffer = new byte[BaseBlock.BaseBlockSize];

			

			// Weird, but is trying to read beyond the end of the file
			if (position >= fileLength) {
				break;
			}

			// logger.info("\n--------reading header--------");
			
			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
			CloseableHttpClient httpclient = httpClientBuilder.build();
			HttpGet httpget = new HttpGet(url);
			BasicHttpContext context = new BasicHttpContext();
			httpget.addHeader("Range", "bytes="+position+"-"+(position+BaseBlock.BaseBlockSize-1));
			CloseableHttpResponse response = httpclient.execute(httpget, context);
			BufferedInputStream in = new BufferedInputStream(response.getEntity().getContent());
			in.read(baseBlockBuffer);
			httpclient.close();
			raf.seek(position);
			raf.write(baseBlockBuffer);
			position+=BaseBlock.BaseBlockSize;
			//size = rof.readFully(baseBlockBuffer, BaseBlock.BaseBlockSize);
			/*if (size == 0) {
				break;
			}*/
			BaseBlock block = new BaseBlock(baseBlockBuffer);

			block.setPositionInFile(position-BaseBlock.BaseBlockSize);
			
			System.out.println(count++);
			switch (block.getHeaderType()) {

			case MarkHeader:
				markHead = new MarkHeader(block);
				if (!markHead.isSignature()) {
					throw new RarException(RarException.RarExceptionType.badRarArchive);
				}
				headers.add(markHead);
				// markHead.print();
				System.out.println("markheader");
				break;

			case MainHeader:
				toRead = block.hasEncryptVersion() ? MainHeader.mainHeaderSizeWithEnc
						: MainHeader.mainHeaderSize;
				byte[] mainbuff = new byte[toRead];
				
				httpClientBuilder = HttpClientBuilder.create();
				httpclient = httpClientBuilder.build();
				httpget = new HttpGet(url);
				context = new BasicHttpContext();
				httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
				response = httpclient.execute(httpget, context);
				in = new BufferedInputStream(response.getEntity().getContent());
				in.read(mainbuff);
				httpclient.close();
				raf.seek(position);
				raf.write(mainbuff);
				position+=toRead;
				//rof.readFully(mainbuff, toRead);
				
				MainHeader mainhead = new MainHeader(block, mainbuff);
				headers.add(mainhead);
				newMhd = mainhead;
				if (newMhd.isEncrypted()) {
					throw new RarException(
							RarExceptionType.rarEncryptedException);
				}
				// mainhead.print();
				System.out.println("mainhead");
				break;

			case SignHeader:
				toRead = SignHeader.signHeaderSize;
				byte[] signBuff = new byte[toRead];
				
				httpClientBuilder = HttpClientBuilder.create();
				httpclient = httpClientBuilder.build();
				httpget = new HttpGet(url);
				context = new BasicHttpContext();
				httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
				response = httpclient.execute(httpget, context);
				in = new BufferedInputStream(response.getEntity().getContent());
				in.read(signBuff);
				httpclient.close();	
				raf.seek(position);
				raf.write(signBuff);
				position+=toRead;
				//rof.readFully(signBuff, toRead);
				
				SignHeader signHead = new SignHeader(block, signBuff);
				headers.add(signHead);
				// logger.info("HeaderType: SignHeader");
				System.out.println("signheader");

				break;

			case AvHeader:
				toRead = AVHeader.avHeaderSize;
				byte[] avBuff = new byte[toRead];
				
				httpClientBuilder = HttpClientBuilder.create();
				httpclient = httpClientBuilder.build();
				httpget = new HttpGet(url);
				context = new BasicHttpContext();
				httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
				response = httpclient.execute(httpget, context);
				in = new BufferedInputStream(response.getEntity().getContent());
				in.read(avBuff);
				httpclient.close();
				raf.seek(position);
				raf.write(avBuff);
				position+=toRead;
				//rof.readFully(avBuff, toRead);
				
				AVHeader avHead = new AVHeader(block, avBuff);
				headers.add(avHead);
				// logger.info("headertype: AVHeader");
				System.out.println("avheader");
				break;

			case CommHeader:
				toRead = CommentHeader.commentHeaderSize;
				byte[] commBuff = new byte[toRead];
				
				httpClientBuilder = HttpClientBuilder.create();
				httpclient = httpClientBuilder.build();
				httpget = new HttpGet(url);
				context = new BasicHttpContext();
				httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
				response = httpclient.execute(httpget, context);
				in = new BufferedInputStream(response.getEntity().getContent());
				in.read(commBuff);
				httpclient.close();
				raf.seek(position);
				raf.write(commBuff);
				position+=toRead;
				//rof.readFully(commBuff, toRead);
				
				CommentHeader commHead = new CommentHeader(block, commBuff);
				headers.add(commHead);
				// logger.info("method: "+commHead.getUnpMethod()+"; 0x"+
				// Integer.toHexString(commHead.getUnpMethod()));
				newpos = commHead.getPositionInFile()
						+ commHead.getHeaderSize();
				position=newpos;
				//rof.setPosition(newpos);
				System.out.println("commheader");

				break;
			case EndArcHeader:

				toRead = 0;
				if (block.hasArchiveDataCRC()) {
					toRead += EndArcHeader.endArcArchiveDataCrcSize;
				}
				if (block.hasVolumeNumber()) {
					toRead += EndArcHeader.endArcVolumeNumberSize;
				}
				EndArcHeader endArcHead;
				if (toRead > 0) {
					byte[] endArchBuff = new byte[toRead];
					
					httpClientBuilder = HttpClientBuilder.create();
					httpclient = httpClientBuilder.build();
					httpget = new HttpGet(url);
					context = new BasicHttpContext();
					httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
					response = httpclient.execute(httpget, context);
					in = new BufferedInputStream(response.getEntity().getContent());
					in.read(endArchBuff);
					httpclient.close();
					raf.seek(position);
					raf.write(endArchBuff);
					position+=toRead;
					//rof.readFully(endArchBuff, toRead);
					
					endArcHead = new EndArcHeader(block, endArchBuff);
					// logger.info("HeaderType: endarch\ndatacrc:"+
					// endArcHead.getArchiveDataCRC());
				} else {
					// logger.info("HeaderType: endarch - no Data");
					endArcHead = new EndArcHeader(block, null);
				}
				headers.add(endArcHead);
				// logger.info("\n--------end header--------");
				System.out.println("endarcheader");
				return headers;

			default:
				byte[] blockHeaderBuffer = new byte[BlockHeader.blockHeaderSize];
				
				httpClientBuilder = HttpClientBuilder.create();
				httpclient = httpClientBuilder.build();
				httpget = new HttpGet(url);
				context = new BasicHttpContext();
				httpget.addHeader("Range", "bytes="+position+"-"+(position+BlockHeader.blockHeaderSize-1));
				response = httpclient.execute(httpget, context);
				in = new BufferedInputStream(response.getEntity().getContent());
				in.read(blockHeaderBuffer);
				httpclient.close();
				raf.seek(position);
				raf.write(blockHeaderBuffer);
				position+=BlockHeader.blockHeaderSize;
				//rof.readFully(blockHeaderBuffer, BlockHeader.blockHeaderSize);
				
				BlockHeader blockHead = new BlockHeader(block,
						blockHeaderBuffer);
				
				//System.out.println(blockHead.getHeaderType());
				switch (blockHead.getHeaderType()) {
				case NewSubHeader:
				case FileHeader:
					toRead = blockHead.getHeaderSize()
							- BlockHeader.BaseBlockSize
							- BlockHeader.blockHeaderSize;
					byte[] fileHeaderBuffer = new byte[toRead];
					
					httpClientBuilder = HttpClientBuilder.create();
					httpclient = httpClientBuilder.build();
					httpget = new HttpGet(url);
					context = new BasicHttpContext();
					httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
					response = httpclient.execute(httpget, context);
					in = new BufferedInputStream(response.getEntity().getContent());
					in.read(fileHeaderBuffer);
					httpclient.close();
					raf.seek(position);
					raf.write(fileHeaderBuffer);
					position+=toRead;
					//rof.readFully(fileHeaderBuffer, toRead);

					FileHeader fh = new FileHeader(blockHead, fileHeaderBuffer);
					headers.add(fh);
					newpos = fh.getPositionInFile() + fh.getHeaderSize()
							+ fh.getFullPackSize();
					
					position=newpos;
					//position+=fh.getFullPackSize();
					//rof.setPosition(newpos);
					System.out.println("fileheader");
					break;

				case ProtectHeader:
					toRead = blockHead.getHeaderSize()
							- BlockHeader.BaseBlockSize
							- BlockHeader.blockHeaderSize;
					byte[] protectHeaderBuffer = new byte[toRead];
					
					httpClientBuilder = HttpClientBuilder.create();
					httpclient = httpClientBuilder.build();
					httpget = new HttpGet(url);
					context = new BasicHttpContext();
					httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
					response = httpclient.execute(httpget, context);
					in = new BufferedInputStream(response.getEntity().getContent());
					in.read(protectHeaderBuffer);
					httpclient.close();
					raf.seek(position);
					raf.write(protectHeaderBuffer);
					position+=toRead;
					//rof.readFully(protectHeaderBuffer, toRead);
					ProtectHeader ph = new ProtectHeader(blockHead,
							protectHeaderBuffer);

					newpos = ph.getPositionInFile() + ph.getHeaderSize()
							+ ph.getDataSize();
					position=newpos;
					//rof.setPosition(newpos);
					System.out.println("protectheader");
					break;

				case SubHeader: {
					byte[] subHeadbuffer = new byte[SubBlockHeader.SubBlockHeaderSize];
					
					httpClientBuilder = HttpClientBuilder.create();
					httpclient = httpClientBuilder.build();
					httpget = new HttpGet(url);
					context = new BasicHttpContext();
					httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
					response = httpclient.execute(httpget, context);
					in = new BufferedInputStream(response.getEntity().getContent());
					in.read(subHeadbuffer);
					httpclient.close();
					raf.seek(position);
					raf.write(subHeadbuffer);
					position+=toRead;
					//rof.readFully(subHeadbuffer,SubBlockHeader.SubBlockHeaderSize);
					
					SubBlockHeader subHead = new SubBlockHeader(blockHead,
							subHeadbuffer);
					subHead.print();
					System.out.println("subheader");
					switch (subHead.getSubType()) {
					case MAC_HEAD: {
						byte[] macHeaderbuffer = new byte[MacInfoHeader.MacInfoHeaderSize];
						
						httpClientBuilder = HttpClientBuilder.create();
						httpclient = httpClientBuilder.build();
						httpget = new HttpGet(url);
						context = new BasicHttpContext();
						httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
						response = httpclient.execute(httpget, context);
						in = new BufferedInputStream(response.getEntity().getContent());
						in.read(macHeaderbuffer);
						httpclient.close();
						raf.seek(position);
						raf.write(macHeaderbuffer);
						position+=toRead;
						//rof.readFully(macHeaderbuffer,MacInfoHeader.MacInfoHeaderSize);
						
						MacInfoHeader macHeader = new MacInfoHeader(subHead,
								macHeaderbuffer);
						macHeader.print();
						headers.add(macHeader);

						break;
					}
					// TODO implement other subheaders
					case BEEA_HEAD:
						break;
					case EA_HEAD: {
						byte[] eaHeaderBuffer = new byte[EAHeader.EAHeaderSize];
						
						httpClientBuilder = HttpClientBuilder.create();
						httpclient = httpClientBuilder.build();
						httpget = new HttpGet(url);
						context = new BasicHttpContext();
						httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
						response = httpclient.execute(httpget, context);
						in = new BufferedInputStream(response.getEntity().getContent());
						in.read(eaHeaderBuffer);
						httpclient.close();
						raf.seek(position);
						raf.write(eaHeaderBuffer);
						position+=toRead;
						//rof.readFully(eaHeaderBuffer, EAHeader.EAHeaderSize);
						
						EAHeader eaHeader = new EAHeader(subHead,
								eaHeaderBuffer);
						eaHeader.print();
						headers.add(eaHeader);

						break;
					}
					case NTACL_HEAD:
						break;
					case STREAM_HEAD:
						break;
					case UO_HEAD:
						toRead = subHead.getHeaderSize();
						toRead -= BaseBlock.BaseBlockSize;
						toRead -= BlockHeader.blockHeaderSize;
						toRead -= SubBlockHeader.SubBlockHeaderSize;
						byte[] uoHeaderBuffer = new byte[toRead];
						
						httpClientBuilder = HttpClientBuilder.create();
						httpclient = httpClientBuilder.build();
						httpget = new HttpGet(url);
						context = new BasicHttpContext();
						httpget.addHeader("Range", "bytes="+position+"-"+(position+toRead-1));
						response = httpclient.execute(httpget, context);
						in = new BufferedInputStream(response.getEntity().getContent());
						in.read(uoHeaderBuffer);
						httpclient.close();
						raf.seek(position);
						raf.write(uoHeaderBuffer);
						position+=toRead;
						//rof.readFully(uoHeaderBuffer, toRead);
						
						UnixOwnersHeader uoHeader = new UnixOwnersHeader(
								subHead, uoHeaderBuffer);
						uoHeader.print();
						headers.add(uoHeader);
						
						//in.close();
						//httpclient.close();
						break;
					default:
						break;
					}

					break;
				}
				default:
					System.out.println("Unknown Header");
					//logger.warning("Unknown Header");
					throw new RarException(RarExceptionType.notRarArchive);

				}
			}
			// logger.info("\n--------end header--------");
		}
		//httpclient.close();
		return headers;
	}
	
public static void extractfile(String URL,String filename,String outpath) throws IOException, RarException{
		
		String url = URL;
		String fn = filename;
		String op = outpath;
		
		List headerlist = Reader.read(url);
		
		long localoffset = 0;
		int filenamelength = 0;
		int ExtraFieldLength = 0;
//		long CompressedSize = 0;
		
		for (int i = 0; i <headerlist.size(); i++) {
			FileHeader fileHeader = (FileHeader)headerlist.get(i);
			if(fileHeader.getFileNameString().equalsIgnoreCase(fn)){
				localoffset = fileHeader.getPositionInFile();
				filenamelength = fileHeader.getNameSize();
				ExtraFieldLength = fileHeader.getDataSize();
//				CompressedSize = fileHeader.getFullPackSize();
				break;
			}
		}
		
		long datapos = localoffset;
		long datalength = localoffset+ExtraFieldLength+filenamelength+30;

		CreateTemporaryFile(url,"F:/ls.rar",datapos,datalength);
		Extract.unzipstream("F:/ls.rar",op);
	}
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
}
