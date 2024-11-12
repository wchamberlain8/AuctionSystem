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

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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


  private void authenticateClient(int userID, Auction server, FileInputStream fileIn) throws Exception {
    String clientChallenge = "clientChallenge" + userID;
    System.out.println("TESTING WHAT IS THE CLIENTCHALLENGE " + clientChallenge);
    ChallengeInfo challengeInfo = server.challenge(userID, clientChallenge);

    byte[] response = challengeInfo.response;
    Signature verifyServerSig = Signature.getInstance("SHA256withRSA"); //set up signature to verify the server's response
    verifyServerSig.initVerify(serverPublicKey);  //uses the server's public key
    verifyServerSig.update(clientChallenge.getBytes());

    if (verifyServerSig.verify(response) == false) {
      System.out.println("Server's reponse could not be verified");
      return;
    }

    Signature clientSignature = Signature.getInstance("SHA256withRSA"); //set up signature to sign the server's challenge
    clientSignature.initSign(clientPrivateKey);
    clientSignature.update(challengeInfo.serverChallenge.getBytes());
    byte[] signedChallenge = clientSignature.sign(); //sign the server's challenge

    TokenInfo tokenInfo = server.authenticate(userID, signedChallenge);

     
  }

  public static void register(Auction server, String[] args) throws RemoteException {
    int userID = server.register(args[1], clientPublicKey);
    System.out.println("Successfully registered. Your userID is: " + userID + " you will need this to use system functions.");
  }

  public static void newAuction(Auction server, String[] args) {
    AuctionSaleItem item = createSaleItem(args[2], args[3], Integer.parseInt(args[4]));
    int itemID = server.newAuction(Integer.parseInt(args[1]), item);
    System.out.println("Successfully created new auction. Your itemID is: " + itemID + " you will need this to use system functions.");
  }

  public static void list(Auction server) {
    AuctionItem[] auctionArray = server.listItems();
    for (AuctionItem auctionItem : auctionArray) {
      System.out.println("ItemID: " + auctionItem.itemID);
      System.out.println("Name: " + auctionItem.name);
      System.out.println("Description: " + auctionItem.description);
      System.out.println("Highest Bid: " + auctionItem.highestBid);
    }
  }

  public static void bid(Auction server, String[] args) {
    boolean check = server.bid(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    if (check) {
      System.out.println("Successfully placed bid.");
    }
    else {
      System.out.println("Failed to place bid.");
    }
  }

  public static void close(Auction server, String[] args) {
    AuctionResult result = server.closeAuction(Integer.parseInt(args[1]), Integer.parseInt(args[2]));

    if (result == null) {
      System.out.println("There was a problem, either the reserve price has not yet been met, that item does not exist or the given userID did not create the auction");
    }
    else {
      System.out.println("The auction has been closed");
      System.out.println("Winning email: " + result.winningEmail);
      System.out.println("Winning bid: " + result.winningPrice);
    }
  }

  public static void getSpec(Auction server, String[] args) {
    AuctionItem retrievedItem = server.getSpec(Integer.parseInt(args[1]));
    System.out.println("Name: " + retrievedItem.name);
    System.out.println("Description: " + retrievedItem.description);
    System.out.println("Highest Bid: " + retrievedItem.highestBid);
  }

  // ******************************************************************************************************************

  // Main method to connect to the server and call functions via command line input
  public static void main(String[] args) {

    if (args.length < 1) {
      System.out.println("Usage: java Client <command> <args>");
      System.out.println("Available commands: register, new, list, bid, close, getSpec");
      System.out.println("\nExample usage:");
      System.out.println("register: java Client register <email>");
      System.out.println("new: java Client new <userID> <name> \"<description>\" <reservePrice>");
      System.out.println("list: java Client list");
      System.out.println("bid: java Client bid <userID> <itemID> <bid>");
      System.out.println("close: java Client close <userID> <itemID>");
      System.out.println("getSpec: java Client getSpec <itemID>");
    }

    try {
      String name = "Auction";
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction server = (Auction) registry.lookup(name); // find the server and connect

      generateKeyPair(); // generate a key pair for the client

      //build + save the server's public key to a variable
      byte[] keyInBytes = Files.readAllBytes(Paths.get("keys/serverPublicKey"));
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyInBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      serverPublicKey = keyFactory.generatePublic(keySpec);

      switch (args[0]) {
        case "register":
          register(server, args);
          break;

        case "new":
          newAuction(server, args);
          break;

        case "list":
          list(server);
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
      System.out.println("Exeption: ");
      e.printStackTrace();
    }
  }
}