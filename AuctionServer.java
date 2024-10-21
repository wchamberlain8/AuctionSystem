import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class AuctionServer implements Auction {

    private HashMap<Integer, AuctionItem> items;

    public AuctionServer() throws RemoteException{
        super();
        items = new HashMap<>();
        items.put(1, new AuctionItem(1, "Car", "null", 1000)); //Adding in hardcoded values to the list of items
        items.put(2, new AuctionItem(2, "PC", "null", 500)); 
    }

    @Override
    public SealedObject getSpec(int itemID) throws RemoteException {

        AuctionItem item = items.get(itemID); //Get the AuctionItem from the hashmap with the relative itemID

        try{
            FileInputStream fileIn = new FileInputStream("keys/testKey.aes");   //opening the file
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
        
            SecretKey key = (SecretKey) objIn.readObject();     //reading the key from the file
            objIn.close();

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
