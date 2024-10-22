import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class AuctionServer implements Auction {

    private HashMap<Integer, AuctionItem> items;

    public AuctionServer() throws RemoteException{
        super();
        items = new HashMap<>();
        items.put(1, new AuctionItem(1, "Car", "Barely works but looks good!", 1000)); //Adding in hardcoded values to the list of items
        items.put(2, new AuctionItem(2, "PC", "Mid-spec PC for sale, very dusty", 500));
        items.put(3, new AuctionItem(3, "Robot", "Actually R2-D2", 5000));  
    }

    private static SecretKey keyGen(String algorithm, int keySize) throws NoSuchAlgorithmException{

        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();

    }

    private static void saveKeyInBytes(SecretKey key) throws Exception{

        FileOutputStream fileOut = new FileOutputStream("keys/testKey.aes"); //Save the key to a file
        byte[] keyInBytes = key.getEncoded();
        fileOut.write(keyInBytes);
        fileOut.close();

    }


    @Override
    public SealedObject getSpec(int itemID) throws RemoteException {

        if(items.isEmpty()){    //return null if the hashmap is empty
            return null;
        }

        AuctionItem item = items.get(itemID); //Get the AuctionItem from the hashmap with the relative itemID

        try{
            // FileInputStream fileIn = new FileInputStream("keys/testKey.aes");   //opening the file
        
            // byte[] keyInBytes = fileIn.readAllBytes();
            // SecretKey key = new SecretKeySpec(keyInBytes, "AES");  //read and rebuild the key
            // fileIn.close();

            SecretKey key = keyGen("AES", 128);
            saveKeyInBytes(key);

            Cipher cipher = Cipher.getInstance("AES");      //generating the cipher  
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            SealedObject sealedItem = new SealedObject(item, cipher);    //encrypting the item into a SealedObject
            return sealedItem;
        }
        catch(Exception e){
            throw new RemoteException();
        }
    }
        

    public static void main(String[] args) {
        try {
            AuctionServer a1 = new AuctionServer(); //creating a server with the name 'Auction'
            String name = "Auction";

            Auction stub = (Auction) UnicastRemoteObject.exportObject(a1, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);

            System.out.println("Server ready");
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
  }
}
    
}
