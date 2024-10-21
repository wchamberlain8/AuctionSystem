import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Encryption {
    
    //Function to generate a key using the @KeyGenerator class
    private static SecretKey keyGen(String algorithm, int keySize) throws NoSuchAlgorithmException{
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();
    }

    public static void main(String[] args) throws Exception{
        
        SecretKey key = keyGen("AES", 128);
        
        FileOutputStream fileOut = new FileOutputStream("keys/testKey.aes"); //Save the key to a file
        byte[] keyInBytes = key.getEncoded();
        fileOut.write(keyInBytes);
        fileOut.close();

    }

    

}
