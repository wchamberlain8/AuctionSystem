import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {

  // helper function to create a new AuctionSaleItem (as we are not allowed to have a constructor etc.)
  public static AuctionSaleItem createSaleItem(String name, String description, int reservePrice) {

    AuctionSaleItem newItem = new AuctionSaleItem();
    newItem.name = name;
    newItem.description = description;
    newItem.reservePrice = reservePrice;

    return newItem;
  }
  
    public static void register(Auction server, String[] args) throws RemoteException {

      int userID = server.register(args[1]);
      System.out.println("Successfully registered. Your userID is: " + userID + " you will need this to use system functions.");
    }
  
    public static void newAuction(Auction server, String[] args) throws Exception {

      AuctionSaleItem item = createSaleItem(args[2], args[3], Integer.parseInt(args[4]));
      int itemID = server.newAuction(Integer.parseInt(args[1]), item);

      System.out.println("Successfully created new auction. Your itemID is: " + itemID + " you will need this to use system functions.");
  }

  public static void list(Auction server, String[] args) throws Exception {

    AuctionItem[] auctionArray = server.listItems(); //get the list of items

    if(auctionArray == null){
      System.out.println("Could not authenticate Client");
    }

    for (AuctionItem auctionItem : auctionArray) {        //print out the items
      System.out.println("ItemID: " + auctionItem.itemID);
      System.out.println("Name: " + auctionItem.name);
      System.out.println("Description: " + auctionItem.description);
      System.out.println("Highest Bid: " + auctionItem.highestBid);
    }
  }

  public static void bid(Auction server, String[] args) throws Exception{

    boolean check = server.bid(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])); //call server function
    if (check) {
      System.out.println("Successfully placed bid.");
    }
    else {
      System.out.println("Failed to place bid.");
    }
  }

  public static void close(Auction server, String[] args) throws Exception {

    AuctionResult result = server.closeAuction(Integer.parseInt(args[1]), Integer.parseInt(args[2])); //call server function

    if (result == null) {
      System.out.println("There was a problem, either the reserve price has not yet been met, that item does not exist or the given userID did not create the auction");
    }
    else {
      System.out.println("The auction has been closed");  //print out the result
      System.out.println("Winning email: " + result.winningEmail);
      System.out.println("Winning bid: " + result.winningPrice);
    }
  }

  public static void getSpec(Auction server, String[] args) throws Exception{

    AuctionItem retrievedItem = server.getSpec(Integer.parseInt(args[2]));

    System.out.println("Name: " + retrievedItem.name);
    System.out.println("Description: " + retrievedItem.description);
    System.out.println("Highest Bid: " + retrievedItem.highestBid);
  }

  public static void usage(){
      System.out.println("Usage: <command> <args>");
      System.out.println("Available commands: register, new, list, bid, close, getSpec");
      System.out.println("\nExample usage:");
      System.out.println("register <email>");
      System.out.println("new <userID> <name> \"<description>\" <reservePrice>");
      System.out.println("list <userID>");
      System.out.println("bid <userID> <itemID> <bid>");
      System.out.println("close <userID> <itemID>");
      System.out.println("getSpec <userID> <itemID>");
  }

  public static void processCommand(Auction server, String[] args){
    
    try {
      switch (args[0]) {
        case "register":
          register(server, args);
          break;

        case "new":
          newAuction(server, args);
          break;

        case "list":
          list(server, args);
          break;

        case "bid":
          bid(server, args);
          break;

        case "close":
          close(server, args);
          break;

        case "getSpec":
          getSpec(server, args);
          break;

        default:
          break;
      }
    } 
    catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  // ******************************************************************************************************************

  // Main method to connect to the server and take in user input
  public static void main(String[] args) {

    //Connect to the server
    try {
      String name = "Auction";
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction server = (Auction) registry.lookup(name); // find the server and connect

      //loop to take in user input
      Scanner input = new Scanner(System.in);
      while(true){
        System.out.println("\nType 'usage' to see available commands, type 'exit' to exit the program");
        System.out.println("Enter a command: ");
        String userInput = input.nextLine();
        String[] inputArgs = userInput.split(" ");
        System.out.println("\n");

        if(inputArgs[0].equals("exit")){
          break;
        }
        else if(inputArgs[0].equals("usage")){
          usage();
        }
        else{
          processCommand(server, inputArgs);
        }
      }
      input.close();
    }

    catch (Exception e) {
      System.out.println("Exeption: ");
      e.printStackTrace();
    }
  }
}