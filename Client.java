import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client{
     public static void main(String[] args) {
      if (args.length < 1) {
        System.out.println("Usage: java Client n");
        return;
       }

      int n = Integer.parseInt(args[0]);  //get user input from command line parameter

      try{
        String name = "Auction";
        Registry registry = LocateRegistry.getRegistry("localhost");
        Auction server = (Auction) registry.lookup(name);   //find the server and connect

        // the below code is from the Coursework 1 where AES encryption was used:

        // SealedObject sealedItem = server.getSpec(n);  //call the server function "getSpec()"

        // FileInputStream fileIn = new FileInputStream("keys/testKey.aes");   //open the file containing the key
        
        // byte[] keyInBytes = fileIn.readAllBytes();        
        // fileIn.close();

        // SecretKey key = new SecretKeySpec(keyInBytes, "AES");

        // Cipher cipher = Cipher.getInstance("AES");  //initialise the cipher to decrypt
        // cipher.init(Cipher.DECRYPT_MODE, key);

        // AuctionItem item = (AuctionItem) sealedItem.getObject(cipher); //decrypt the SealedObject into AuctionItem

        AuctionItem item = server.getSpec(n);

        System.out.println("Item ID: " + n);      //printing out the AuctionItem's details
        System.out.println("Name: " + item.name);
        System.out.println("Description: " + item.description);
        System.out.println("Highest Bid: " + item.highestBid);
      }
      
      catch(Exception e){
        System.out.println("Exeption: ");
        e.printStackTrace();
      }
    }
}