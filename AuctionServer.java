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

    private HashMap<Integer, AuctionItem> itemsMap;
    private HashMap<Integer, String> usersMap;
    private int userIDCount = 100;
    private int auctionIDCount = 1000;

    public AuctionServer() throws RemoteException{
        super();
        itemsMap = new HashMap<>();
        usersMap = new HashMap<>();

        AuctionItem item1 = createItem(1, "Car", "Barely works", 1000);
        AuctionItem item2 = createItem(2, "PC", "Mid spec, very dusty", 500);
        itemsMap.put(item1.itemID, item1);
        itemsMap.put(item2.itemID, item2);

    }

    //Helper function to create a new AuctionItem since constructor not allowed
    public AuctionItem createItem(int itemID, String name, String description, int highestBid){
        AuctionItem item = new AuctionItem();
        item.itemID = itemID;
        item.name = name;
        item.description = description;
        item.highestBid = highestBid;

        return item;
    }

    //Function that generates a key using the @KeyGenerator class
    private static SecretKey keyGen(String algorithm, int keySize) throws NoSuchAlgorithmException{

        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();

    }

    //Function that saves a SecretKey in a binary byte array
    private static void saveKeyInBytes(SecretKey key) throws Exception{

        FileOutputStream fileOut = new FileOutputStream("keys/testKey.aes"); //Save the key to a file
        byte[] keyInBytes = key.getEncoded();
        fileOut.write(keyInBytes);
        fileOut.close();
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {

        if(itemsMap.isEmpty()){    //return null if the hashmap is empty
            return null;
        }

        AuctionItem item = itemsMap.get(itemID); //Get the AuctionItem from the hashmap with the relative itemID
        return item;

        // the below code is from the Coursework 1 where AES encryption was used

        // try{
        //     SecretKey key = keyGen("AES", 128); //genereate the key and save it to the keys folder as bytes
        //     saveKeyInBytes(key);

        //     Cipher cipher = Cipher.getInstance("AES");      //generating the cipher  
        //     cipher.init(Cipher.ENCRYPT_MODE, key);
            
        //     SealedObject sealedItem = new SealedObject(item, cipher);    //encrypting the item into a SealedObject
        //     return sealedItem;
        // }
        // catch(Exception e){
        //     throw new RemoteException();
        // }
    }

    // task 2 methods below!

    @Override
    public int register(String email) throws RemoteException{
        userIDCount++;
        usersMap.put(userIDCount, email);
        return userIDCount;
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException{
        //need to create a AuctionSaleItem through a helper function? -> can't be done here 
        //though because we're passing a AuctionSaleItem into this current method



    }

    @Override
    public AuctionItem[] listItems() throws RemoteException{

    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID){

    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException{

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
