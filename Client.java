import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

public class Client {

  private static PrivateKey clientPrivateKey;
  private static PublicKey clientPublicKey;
  private static PublicKey serverPublicKey;

  // helper function to create a new AuctionSaleItem (as we are not allowed to have a constructor etc.)
  public static AuctionSaleItem createSaleItem(String name, String description, int reservePrice) {

    AuctionSaleItem newItem = new AuctionSaleItem();
    newItem.name = name;
    newItem.description = description;
    newItem.reservePrice = reservePrice;

    return newItem;
  }

  private static void generateKeyPair(){
    try{
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); // initialise the key generator
      keyGen.initialize(2048);

      KeyPair keyPair = keyGen.generateKeyPair(); // generate and split the key pair
      PrivateKey clientPrivateKey = keyPair.getPrivate();
      PublicKey clientPublicKey = keyPair.getPublic();

      Client.clientPrivateKey = clientPrivateKey; // save the two keys to the client
      Client.clientPublicKey = clientPublicKey;

    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  //Function that fully authenticates the client with the server, verifying the server's response, and returns a token for use
  private static String authenticateClient(int userID, Auction server) throws Exception {
      String clientChallenge = "clientChallenge" + userID;  //create client challenge

      ChallengeInfo challengeInfo = server.challenge(userID, clientChallenge); //call challenge server function
  
      byte[] response = challengeInfo.response;
      Signature verifyServerSig = Signature.getInstance("SHA256withRSA"); //set up signature to verify the server's response
      verifyServerSig.initVerify(serverPublicKey);  //setup to use the server's public key
      verifyServerSig.update(clientChallenge.getBytes());
  
      if (verifyServerSig.verify(response) == false) {
        System.out.println("Server's reponse could not be verified");
        return null;
      }
  
      Signature clientSignature = Signature.getInstance("SHA256withRSA"); //set up signature to sign the server's challenge
      clientSignature.initSign(Client.clientPrivateKey);
      clientSignature.update(challengeInfo.serverChallenge.getBytes());
      byte[] signedChallenge = clientSignature.sign(); //sign the server's challenge

      System.out.println(Base64.getEncoder().encodeToString(signedChallenge));
  
      TokenInfo tokenInfo = server.authenticate(userID, signedChallenge);
      if (tokenInfo == null) {
        System.out.println("Authentication failed");
        return null;
      }
  
      return tokenInfo.token; //return the actual token string
  
    }
  
    public static void register(Auction server, String[] args) throws RemoteException {

      generateKeyPair(); //generate the client's key pair on registration

      int userID = server.register(args[1], clientPublicKey);
      System.out.println("Successfully registered. Your userID is: " + userID + " you will need this to use system functions.");
    }
  
    public static void newAuction(Auction server, String[] args) throws Exception {
      String token = authenticateClient(Integer.parseInt(args[1]), server); //generate token

      AuctionSaleItem item = createSaleItem(args[2], args[3], Integer.parseInt(args[4]));
      int itemID = server.newAuction(Integer.parseInt(args[1]), item, token);

      System.out.println("Successfully created new auction. Your itemID is: " + itemID + " you will need this to use system functions.");
  }

  public static void list(Auction server, String[] args) throws Exception {

    String token = authenticateClient(Integer.parseInt(args[1]), server); //generate token

    AuctionItem[] auctionArray = server.listItems(Integer.parseInt(args[1]), token); //get the list of items

    for (AuctionItem auctionItem : auctionArray) {        //print out the items
      System.out.println("ItemID: " + auctionItem.itemID);
      System.out.println("Name: " + auctionItem.name);
      System.out.println("Description: " + auctionItem.description);
      System.out.println("Highest Bid: " + auctionItem.highestBid);
    }
  }

  public static void bid(Auction server, String[] args) throws Exception{

    String token = authenticateClient(Integer.parseInt(args[1]), server); //generate token

    boolean check = server.bid(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), token); //call server function
    if (check) {
      System.out.println("Successfully placed bid.");
    }
    else {
      System.out.println("Failed to place bid.");
    }
  }

  public static void close(Auction server, String[] args) throws Exception {

    String token = authenticateClient(Integer.parseInt(args[1]), server); //generate token

    AuctionResult result = server.closeAuction(Integer.parseInt(args[1]), Integer.parseInt(args[2]), token); //call server function

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

    String token = authenticateClient(Integer.parseInt(args[1]), server); //generate token

    AuctionItem retrievedItem = server.getSpec(Integer.parseInt(args[1]), Integer.parseInt(args[2]), token);
    System.out.println("Name: " + retrievedItem.name);
    System.out.println("Description: " + retrievedItem.description);
    System.out.println("Highest Bid: " + retrievedItem.highestBid);
  }

  public static void usage(){
      System.out.println("Usage: java Client <command> <args>");
      System.out.println("Available commands: register, new, list, bid, close, getSpec");
      System.out.println("\nExample usage:");
      System.out.println("register: java Client register <email>");
      System.out.println("new: java Client new <userID> <name> \"<description>\" <reservePrice>");
      System.out.println("list: java Client list <userID>");
      System.out.println("bid: java Client bid <userID> <itemID> <bid>");
      System.out.println("close: java Client close <userID> <itemID>");
      System.out.println("getSpec: java Client getSpec <userID> <itemID>");
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

      //build + save the server's public key to a variable
      byte[] keyInBytes = Files.readAllBytes(Paths.get("keys/server_public.key"));
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyInBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      serverPublicKey = keyFactory.generatePublic(keySpec);

      //loop to take in user input

      Scanner input = new Scanner(System.in);
      while(true){
        System.out.println("Type 'usage' to see available commands");
        System.out.println("Type 'exit' to exit the program");
        System.out.println("Enter a command: ");
        String userInput = input.nextLine();
        String[] inputArgs = userInput.split(" ");

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