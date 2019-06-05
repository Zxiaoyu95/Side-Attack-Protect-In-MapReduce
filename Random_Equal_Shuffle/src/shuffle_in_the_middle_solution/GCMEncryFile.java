package shuffle_in_the_middle_solution;
import org.apache.commons.codec.binary.Base64;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
 

public class GCMEncryFile {
	private static final String password ="Xidian";
	private static final String JAES = null;
    public static void main(String[] args) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, FileNotFoundException, IOException {
        // TODO code application logic here
    	KeyGenerator kgen = KeyGenerator.getInstance("AES");// 创建AES的Key生产者
        kgen.init(128, new SecureRandom(password.getBytes()));
        SecretKey secretKey = kgen.generateKey();// 根据用户密码，生成一个密钥
        byte[] enCodeFormat = secretKey.getEncoded();// 返回基本编码格式的密钥
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");// 转换为AES专用密钥
		Cipher gcmcipher= Cipher.getInstance("AES/GCM/PKCS5Padding");
		gcmcipher.init(Cipher.ENCRYPT_MODE, key);
		
		String context="nihao";
		byte[] iv = gcmcipher.getIV(); 
        assert iv.length == 12;
       
        System.out.println("Plaintext: " + context);
        System.out.println("Key: " + key);
        
        // Encrypt the context
        byte[]  encryptData = gcmcipher.doFinal(context.getBytes());
        assert encryptData.length == context.getBytes().length + 16; 
        byte[] message = new byte[12 + context.getBytes().length + 16]; 
        System.arraycopy(iv, 0, message, 0, 12);
        System.arraycopy(encryptData, 0, message, 12, encryptData.length);
        System.out.println("Ciphertext: " + Base64.encodeBase64String(message));
        
        // Initialize the same cipher
        GCMParameterSpec params = new GCMParameterSpec(128, message, 0, 12);
        try {
			gcmcipher.init(Cipher.DECRYPT_MODE, key, params);
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

        // Decrypt the message
        if (message.length < 12 + 16) throw new IllegalArgumentException();
        try {
			gcmcipher.init(Cipher.DECRYPT_MODE, key, params);
		} catch (InvalidAlgorithmParameterException e1) {
			e1.printStackTrace();
		}
        byte[]  decryptData = gcmcipher.doFinal(message, 12, message.length - 12);
        System.out.println("Ciphertext decrypted: "+new String(decryptData));
        
        FileReader fpin;
        FileWriter fpout;
        FileWriter fpout2;
        String s, aux, aux2;
        
        fpin = new FileReader("E:/MRRData/data/census_1990.txt");
        //fpin = new FileReader("E:/test/原文件.txt");
        fpout = new FileWriter("E:/MRRData/data/GCMcensus.txt");
        fpout2 = new FileWriter("E:/test/DGCMcensus.txt");
        
        BufferedReader br = new BufferedReader(fpin);
        BufferedWriter bw = new BufferedWriter(fpout);
        BufferedWriter bw2 = new BufferedWriter(fpout2);


            s = br.readLine();
            if(s!=null) {
                do{
                	String [] split=s.split("	");
              	for(int i=0;i<split.length;++i){
              		gcmcipher.init(Cipher.ENCRYPT_MODE, key);
              		byte[] new_encryptData = gcmcipher.doFinal(split[i].replace("\"", "").getBytes("UTF-8"));
              		byte[] new_message = new byte[12 + split[i].replace("\"", "").getBytes().length + 16];
              		System.arraycopy(iv, 0, new_message, 0, 12);
                    System.arraycopy(new_encryptData, 0, new_message, 12, new_encryptData.length);
                    aux =  Base64.encodeBase64String(new_message);
                    if(i == split.length-1){
                       bw.write(aux.trim() + "\r\n");
                      }
                    else{
                       bw.write(aux.trim() +"	");
                        }
                
//                     GCMParameterSpec new_params = new GCMParameterSpec(128, new_message, 0, 12);
//                     if (new_message.length < 12 + 16) throw new IllegalArgumentException();
//                     try {
//							gcmcipher.init(Cipher.DECRYPT_MODE, key, new_params);
//						} catch (InvalidAlgorithmParameterException e) {
//							e.printStackTrace();
//						}
//                        byte[] Enresult  = Base64.decodeBase64(aux);
//                        byte[] new_decryptData = gcmcipher.doFinal(Enresult, 12, Enresult.length - 12);
//                        aux2 = new String(new_decryptData, "UTF-8");
//                        bw2.write(aux2+"	");
               	}
                	bw2.newLine();
                    s = br.readLine();
                }while(s!=null);
                bw.flush();
                bw2.flush();
                bw.close();
            }
    
    }
    
}
