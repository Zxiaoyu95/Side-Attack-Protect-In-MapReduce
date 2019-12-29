package bloom_filter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.*;

import org.apache.hadoop.util.bloom.Filter;
import org.apache.hadoop.util.bloom.CountingBloomFilter;
import org.apache.hadoop.util.bloom.Key;

public class BF {
	static String logPath = new String("E:/test/");
//	static File input = new File(logPath + "reduceInputCollect");
//	static File output = new File(logPath + "mapOutputCollect");
	static File input = new File(logPath + "2.txt");
	static File output = new File(logPath + "1.txt");
	
	//List<File> inputFiles = new ArrayList();
	//List<File> outputFiles = new ArrayList();
	public static void main(String[] args) {
		System.out.println("start checking input");
		File currFile = new File(logPath);
//		iterateFolders(currFile);
		boolean i = processInput(input,output);
		System.out.println("check finished:" + i);
	}
	
	private static boolean processInput (File input ,File output){
		
		
		int vectorSize = (int) input.length()/3;
		int nbHash = (int) Math.ceil(-(Math.log(0.001) / Math.log(2)));
		CountingBloomFilter outputFilter = new CountingBloomFilter(vectorSize,nbHash,1);
		CountingBloomFilter inputFilter = new CountingBloomFilter(vectorSize,nbHash,1);

		InputStreamReader inputS = null;
		InputStreamReader outputS = null;
		String strin = null;
		String strout = null;
		try {
			
			inputS = new InputStreamReader(new FileInputStream(input));
			outputS = new InputStreamReader(new FileInputStream(output));
			BufferedReader bri = new BufferedReader(inputS);
			BufferedReader bro = new BufferedReader(outputS);
			byte[] keyaddin;
			while( bri.read()!= -1){
				String line = bri.readLine();
				String[] parts = line.split("\t");
				for (int i = 2; i < parts.length;i++){
					keyaddin = (parts[1]+parts[i]).getBytes();
					inputFilter.add(new Key(keyaddin));
				}
			}
			byte[] keyadd;
			String str;
			while( bro.read()!= -1){
				String line = bro.readLine();
				String[] parts = line.split("\t");
				for (int i = 1; i < parts.length;i=i+2){
					str = parts[i]+parts[i+1];
					keyadd = (str).getBytes();
					outputFilter.add(new Key(keyadd));
				}
			}
			inputS.close();
			outputS.close();
//			inputFilter.xor(outputFilter);
			strin = new String(inputFilter.toString());
			strout = new String(outputFilter.toString()); 
			System.out.println(strin);
			System.out.println(strout);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//关闭	
//		return inputFilter.equals(outputFilter);
		return strin.equals(strout);
		
	}
	private static int getFileLine(File file){ 
		long fileLength = file.length(); 
		LineNumberReader rf = null;
		int lines = 0; 
		try { 
			rf = new LineNumberReader(new FileReader(file)); 
			if (rf != null) { 				
				rf.skip(fileLength); 
				lines = rf.getLineNumber(); 
//				System.out.println(lines+1);
				rf.close(); 
			} 
		} catch (IOException e) { 
			if (rf != null) { 
				try { 
					rf.close(); 
				} catch (IOException ee) { 
				} 
			} 
		} 
		return lines+1;
	}
	
	private static void iterateFolders(File currFile){
		if (currFile.isDirectory()){
			File files[] = currFile.listFiles();
			for(File f:files){
				iterateFolders(f);
			}
		}
		else {
			if (currFile.isFile()&&currFile.getAbsolutePath().contains("input_")&&currFile.getAbsolutePath().contains("reduce(org.apache")){
				//inputFiles.add(currFile);
				try {
					IOCopier.joinFiles(input,currFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (currFile.isFile()&&currFile.getAbsolutePath().contains("output_")&&currFile.getAbsolutePath().contains("map(")){
				//outputFiles.add(currFile);
				try {
					IOCopier.joinFiles(output,currFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
}


	
class IOCopier {
	    public static void joinFiles(File destination, File source)
	            throws IOException {
	        OutputStream output = null;
	        try {
	            output = createAppendableStream(destination);
	            //for (File source : sources) {
	                appendFile(output, source);
	            //}
	        } finally {
	            IOUtils.closeQuietly(output);
	        }
	    }
	    
	    public static void joinFiles(File destination, File[] sources)
	            throws IOException {
	        OutputStream output = null;
	        try {
	            output = createAppendableStream(destination);
	            for (File source : sources) {
	                appendFile(output, source);
	            }
	        } finally {
	            IOUtils.closeQuietly(output);
	        }
	    }
	    
	    private static BufferedOutputStream createAppendableStream(File destination)
	            throws FileNotFoundException {
	        return new BufferedOutputStream(new FileOutputStream(destination, true));
	    }
	    
	    private static void appendFile(OutputStream output, File source)
	            throws IOException {
	        InputStream input = null;
	        try {
	            input = new BufferedInputStream(new FileInputStream(source));
	            IOUtils.copy(input, output);
	        } finally {
	            IOUtils.closeQuietly(input);
	        }
	    }	    
}
	
class IOUtils {
	    private static final int BUFFER_SIZE = 1024 * 4;

	    public static long copy(InputStream input, OutputStream output)
	            throws IOException {
	        byte[] buffer = new byte[BUFFER_SIZE];
	        long count = 0;
	        int n = 0;
	        while (-1 != (n = input.read(buffer))) {
	            output.write(buffer, 0, n);
	            count += n;
	        }
	        return count;
	    }

	    public static void closeQuietly(Closeable output) {
	        try {
	            if (output != null) {
	                output.close();
	            }
	        } catch (IOException ioe) {
	            ioe.printStackTrace();
	        }
	    }
	}
