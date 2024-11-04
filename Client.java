import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client{

  //helper function to print out the menu
  public static void printMenu(){
      System.out.println("Auction System");
      System.out.println("1) Create a New Auction");
      System.out.println("2) Close an Auction");
      System.out.println("3) List All Current Live Auctions");
      System.out.println("4) Get Specific Information of an Auction");
      System.out.println("5) Bid on an Auction");
      System.out.println("6) Exit");
      System.out.println("Please enter your selection [1-6]");
    }

  //helper function to create a new AuctionSaleItem (as we are not allowed to have a constructor etc.)
  public static AuctionSaleItem createSaleItem(){
      Scanner input = new Scanner(System.in);
      AuctionSaleItem newItem = new AuctionSaleItem();
  
      System.out.println("What is the name of the new Auction item? ");
      newItem.name = input.nextLine();
  
      System.out.println("What is the description of the new Auction item? ");
      newItem.description = input.nextLine();
  
      System.out.println("What is the reserve price of the new Auction item? ");
      newItem.reservePrice = input.nextInt();
  
      return newItem;
    }
  
    public static void main(String[] args) {
      try{
        String name = "Auction";
        Registry registry = LocateRegistry.getRegistry("localhost");
        Auction server = (Auction) registry.lookup(name);   //find the server and connect
  
        // task 2 functionallity below!
  
        Scanner input = new Scanner(System.in);
        System.out.println("Register for the Auction System by entering your email: ");
        String email = input.nextLine();
        int userID = server.register(email);
        System.out.println("Registered! Your userID is " + userID);
      
        while(true){
          printMenu();
          int userInput = input.nextInt();
          
          switch (userInput) {
            case 1:
            AuctionSaleItem newItem = createSaleItem();
            server.newAuction(userID, newItem);
          break;
      
        default:
          break;
      }
    }
        
        
    }
    
    catch(Exception e){
      System.out.println("Exeption: ");
      e.printStackTrace();
    }
  }
}