import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client{

  //helper function to create a new AuctionSaleItem (as we are not allowed to have a constructor etc.)
  public static AuctionSaleItem createSaleItem(String name, String description, int reservePrice){

      AuctionSaleItem newItem = new AuctionSaleItem();
      newItem.name = name;
      newItem.description = description;
      newItem.reservePrice = reservePrice;
  
      return newItem;
    }
  
    public static void main(String[] args) {

      if(args.length <1){
        System.out.println("Usage: java Client <command> <args>");
        System.out.println("Available commands: register, new, list, bid, close, getSpec");
        System.out.println("\nExample usage:");
        System.out.println("register: java Client register <email>");
        System.out.println("new: java Client new <userID> <name> <description> <reservePrice>");
        System.out.println("list: java Client list");
        System.out.println("bid: java Client bid <userID> <itemID> <bid>");
        System.out.println("close: java Client close <userID> <itemID>");
        System.out.println("getSpec: java Client getSpec <itemID>");
      }

      try{
        String name = "Auction";
        Registry registry = LocateRegistry.getRegistry("localhost");
        Auction server = (Auction) registry.lookup(name);   //find the server and connect
  
        switch (args[0]) {
          case "register":
            int userID = server.register(args[1]);
            System.out.println("Successfully registered. Your userID is: " + userID + "you will need this to use system functions.");
            break;

          case "new":
            AuctionSaleItem item = createSaleItem(args[2], args[3], Integer.parseInt(args[4]));
            int itemID = server.newAuction(Integer.parseInt(args[1]), item);
            System.out.println("Successfully created new auction. Your itemID is: " + itemID + "you will need this to use system functions.");
            break;

          case "list":
            AuctionItem[] auctionArray = server.listItems();
            for (AuctionItem auctionItem : auctionArray) {
              System.out.println("ItemID: " + auctionItem.itemID + " Name: " + auctionItem.name + " Description: " + auctionItem.description + " Highest Bid: " + auctionItem.highestBid);
            }
            break;

          case "bid":
            boolean check = server.bid(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));

            if(check){
              System.out.println("Successfully placed bid.");
            }
            else{
              System.out.println("Failed to place bid.");
            }
            break;

          case "close":
            server.closeAuction(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            break;

          case "getSpec":
            AuctionItem retrievedItem = server.getSpec(Integer.parseInt(args[1]));
            System.out.println("Name: " + retrievedItem.name);
            System.out.println("Description: " + retrievedItem.description);
            System.out.println("Highest Bid: " + retrievedItem.highestBid);
            break;
            
          default:
            break;
        }
    }
    
    catch(Exception e){
      System.out.println("Exeption: ");
      e.printStackTrace();
    }
  }
}