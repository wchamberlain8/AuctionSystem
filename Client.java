import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

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

        SealedObject sealedItem = server.getSpec(n);  //call the server function "getSpec()"

        FileInputStream fileIn = new FileInputStream("keys/testkey.aes");   //open the file containing the key
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
        SecretKey key = (SecretKey) objIn.readObject();   //read the key
        objIn.close();

        Cipher cipher = Cipher.getInstance("AES");  //initialise the cipher to decrypt
        cipher.init(Cipher.DECRYPT_MODE, key);

        AuctionItem item = (AuctionItem) sealedItem.getObject(cipher); //decrypt the SealedObject into AuctionItem

        System.out.println("Item ID: " + n);      //printing out the AuctionItem's details
        System.out.println("Name: " + item.getName());
        System.out.println("Description: " + item.getDescription());
        System.out.println("Highest Bid: " + item.getHighestBid());
      }
      
      catch(Exception e){
        System.out.println("Exeption: ");
        e.printStackTrace();
      }
    }
}