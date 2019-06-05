package shuffle_in_the_middle_solution;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.io.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class GCM {
	public static Key createKey(){
		KeyGenerator kg;
		try {
			kg = KeyGenerator.getInstance("AES");
			kg.init(128);  //初始化密钥生成器，AES要求密钥长度为128位、192位、256位
			SecretKey secretKey=kg.generateKey();
			SecretKey key=new SecretKeySpec(secretKey.getEncoded(),"AES");
			return key;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}
	public static byte[] encrypt(String context,String password){	
		try {
//			KeyGenerator kg = KeyGenerator.getInstance("AES");
//			kg.init(128);  //初始化密钥生成器，AES要求密钥长度为128位、192位、256位
//			SecretKey secretKey=kg.generateKey();
//			SecretKey key=new SecretKeySpec(secretKey.getEncoded(),"AES");
			KeyGenerator kgen = KeyGenerator.getInstance("AES");// 创建AES的Key生产者
	        kgen.init(128, new SecureRandom(password.getBytes()));
	        SecretKey secretKey = kgen.generateKey();// 根据用户密码，生成一个密钥
	        byte[] enCodeFormat = secretKey.getEncoded();// 返回基本编码格式的密钥
	        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");// 转换为AES专用密钥
			Cipher cipher= Cipher.getInstance("AES/GCM/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] iv = cipher.getIV(); 
	        assert iv.length == 12;
	        byte[]  encryptData = cipher.doFinal(context.getBytes());
	        assert encryptData.length == context.getBytes().length + 16; 
	        byte[] message = new byte[12 + context.getBytes().length + 16]; 
	        System.arraycopy(iv, 0, message, 0, 12);
	        System.arraycopy(encryptData, 0, message, 12, encryptData.length);
	        return message;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}  
    public static byte[] decrypt(byte[] message,String password) {
    	
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");// 创建AES的Key生产者
	        kgen.init(128, new SecureRandom(password.getBytes()));
	        SecretKey secretKey = kgen.generateKey();// 根据用户密码，生成一个密钥
	        byte[] enCodeFormat = secretKey.getEncoded();// 返回基本编码格式的密钥
	        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");// 转换为AES专用密钥
	        System.out.println(key);
			Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
			if (message.length < 12 + 16) throw new IllegalArgumentException();
            GCMParameterSpec params = new GCMParameterSpec(128, message, 0, 12);
            cipher.init(Cipher.DECRYPT_MODE, key, params);
            byte[]  decryptData = cipher.doFinal(message, 12, message.length - 12);
            return decryptData;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
         
    }
    public static void main(String[] args) {
    	String content = "2013-1-3";
    	System.out.println("加密前："+content);
//    	Key k =GCM.createKey();
//    	System.out.println(k);
    	String k="Xidian321";
    	byte[] encrypt1 = GCM.encrypt(content,k);
    	byte[] encrypt2 = GCM.encrypt(content,k);
    	byte[] encrypt3 = GCM.encrypt(content,k);
    	System.out.println("加密后："+Base64.encodeBase64String(encrypt1));
    	System.out.println("加密后："+Base64.encodeBase64String(encrypt2));
    	System.out.println("加密后："+Base64.encodeBase64String(encrypt3));
    	Text t1 =new Text(Base64.encodeBase64String(encrypt1));
    	System.out.println(t1.toString());
    	byte[] decode1 = GCM.decrypt(Base64.decodeBase64(t1.toString()),k);
    	System.out.println("解密后："+new String(decode1));
    	Text t2 =new Text(Base64.encodeBase64String(encrypt2));
    	System.out.println(t2.toString());
    	byte[] decode2 = GCM.decrypt(Base64.decodeBase64(t2.toString()),k);
    	System.out.println("解密后："+new String(decode2));
    	Text t3 =new Text(Base64.encodeBase64String(encrypt3));
    	System.out.println(t3.toString());
    	byte[] decode3 = GCM.decrypt(Base64.decodeBase64(t3.toString()),k);
    	System.out.println("解密后："+new String(decode3));
//    	  try {
//              // read file content from file
//              StringBuffer sb= new StringBuffer("");
//              FileReader reader = new FileReader("E:\\MapReduce\\trip_jan.txt");
//              BufferedReader br = new BufferedReader(reader);
//              String str = null;
//              while((str = br.readLine()) != null) {
//            	   String[] sp=str.split("	");
//            	   byte[] encrypt = GCM.encrypt(sp[5],k);
//            	   sb.append(Base64.encodeBase64String(encrypt)+"	");
//                   sb.append("/n");
//              }
//             
//              br.close();
//              reader.close();
//              // write string to file
//              FileWriter writer = new FileWriter("E:\\MapReduce\\Entrip_jan.txt");
//              BufferedWriter bw = new BufferedWriter(writer);
//              bw.write(sb.toString());
//              bw.close();
//              writer.close();
//        }
//        catch(FileNotFoundException e) {
//                    e.printStackTrace();
//              }
//              catch(IOException e) {
//                    e.printStackTrace();
//              }
       
    }
}
