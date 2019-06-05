package mapreduce;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.security.*; 

public class EncryptFile {
	private static final String password ="xidian320";
    public static void main(String[] args) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, FileNotFoundException, IOException {
        // TODO code application logic here
    	 KeyGenerator kgen = KeyGenerator.getInstance("AES");// 创建AES的Key生产者
         SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
         random.setSeed(password.getBytes());
         kgen.init(128, random);
         SecretKey secretKey = kgen.generateKey();// 根据用户密码，生成一个密钥
         byte[] enCodeFormat = secretKey.getEncoded();// 返回基本编码格式的密钥
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");// 转换为AES专用密钥
			Cipher aesCipher = Cipher.getInstance("AES");// 创建密码器
			aesCipher.init(Cipher.ENCRYPT_MODE, key);// 初始化为加密模式的密码器

       
        byte b1 = 127;
        byte b2 = 112;
        byte[] cleartext = new byte[2];
        cleartext[0] = b1;
        cleartext[1] = b2;
        System.out.println("Plaintext: " + cleartext);
        System.out.println("Key: " + key);
        
        // Encrypt the cleartext
        byte[] ciphertext = aesCipher.doFinal(cleartext);
        System.out.println("Ciphertext: " + ciphertext[0] + " " + ciphertext[1]);
        
        
        // Initialize the same cipher for decryption
        aesCipher.init(Cipher.DECRYPT_MODE, key);

        // Decrypt the ciphertext
        byte[] cleartext1 = aesCipher.doFinal(ciphertext);
        
        System.out.println("Ciphertext decrypted: " + cleartext1[0] + " " + cleartext1[1]);
        
        FileReader fpin;
        FileWriter fpout;
        FileWriter fpout2;
        byte[] plaintext;
        String s, aux, aux2;
 //       fpin = new FileReader("E:/MapReduce/census_1990.txt");
        fpin = new FileReader("E:/test/原文件.txt");
        fpout = new FileWriter("E:/test/Ecensus.txt");
        fpout2 = new FileWriter("E:/test/Dcensus.txt");
        
        BufferedReader br = new BufferedReader(fpin);
        BufferedWriter bw = new BufferedWriter(fpout);
        BufferedWriter bw2 = new BufferedWriter(fpout2);


            s = br.readLine();
            if(s!=null) {
                do{
                	String [] split=s.split("	");
              	for(int i=0;i<split.length;i++){
                		aesCipher.init(Cipher.ENCRYPT_MODE, key);
                        ciphertext = aesCipher.doFinal(split[i].replace("\"", "").getBytes("UTF-8"));
                        aux =  JAES.parseByte2HexStr(ciphertext);
                        if(i==split.length-1){
                        bw.write(aux + "\r\n");}
                        else{
                        	bw.write(aux + "	");
                        }
                        aesCipher.init(Cipher.DECRYPT_MODE, key);
                        ciphertext = JAES.parseHexStr2Byte(aux);
                        plaintext = aesCipher.doFinal(ciphertext);
                        aux = new String(plaintext, "UTF-8");
                        bw2.write(aux+"	");
               	}
                	bw2.newLine();
                    s = br.readLine();
                }while(s!=null);
                bw.flush();
                bw2.flush();
            }
    
    }
    
}