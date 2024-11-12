import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Random;

public class AuctionServer implements Auction {

    private HashMap<Integer, AuctionItem> itemsMap = new HashMap<>();
    private HashMap<Integer, String> usersMap = new HashMap<>();
    private HashMap<Integer, AuctionSaleItem> userAuctionsMap = new HashMap<>();
    private HashMap<Integer, Integer> highestBidderMap = new HashMap<>();
    Random random = new Random();

    public AuctionServer() throws RemoteException {
        super();

    }

    // Function to generate a KeyPair for cryptographic authentication later
    private static void generateKeyPair() throws Exception {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); // set up the key generator
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            PrivateKey serverPrivateKey = keyPair.getPrivate(); // generate and seperate the two keys
            PublicKey serverPublicKey = keyPair.getPublic();

            FileOutputStream fileOut = new FileOutputStream("keys/server_public.key"); // save the public key (in bytes)
                                                                                       // to a file
            fileOut.write(serverPublicKey.getEncoded());
            fileOut.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ChallengeInfo challenge(int userID, String clientChallenge) throws RemoteException {
        return null;
    }


    @Override
    public TokenInfo authenticate(int userID, byte[] signature) throws RemoteException {
        return null;
    }

    // Helper function to create a new AuctionItem since constructor not allowed
    public AuctionItem createItem(int itemID, String name, String description, int highestBid) {
        AuctionItem item = new AuctionItem();
        item.itemID = itemID;
        item.name = name;
        item.description = description;
        item.highestBid = highestBid;

        return item;
    }

    @Override
    public AuctionItem getSpec(int userID, int itemID, String token ) throws RemoteException {

        //TODO: CHECK TOKEN IS VALID HERE


        if (itemsMap.isEmpty()) { // return null if the hashmap is empty
            return null;
        }

        AuctionItem item = itemsMap.get(itemID); // Get the AuctionItem from the hashmap with the relative itemID
        return item;

    }

    @Override
    public int register(String email, PublicKey pkey) throws RemoteException {

        //TODO: ADD USERS PUBLIC KEY TO AN AUTHENTICATED USER HASHMAP HERE

        int userID = random.nextInt(100); // generate random userID
        usersMap.put(userID, email); // place into hashmap, userID being the key for each email, incase we need a
                                     // user's email
        return userID;
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item, String token) throws RemoteException {

        //TODO: CHECK TOKEN IS VALID HERE

        int itemID = random.nextInt(1000); // generate random itemID
        userAuctionsMap.put(userID, item); // place into hashmap, userID being the key for each AuctionSaleItem, to know
                                           // who created the auction

        // create new AuctionItem from AuctionSaleItem info

        AuctionItem newItem = createItem(itemID, item.name, item.description, 0);
        itemsMap.put(itemID, newItem); // place into hashmap, the itemID being the key for the AuctionItem, so we have
                                       // all items saved in one place

        return itemID;

    }

    @Override
    public AuctionItem[] listItems(int userID, String token) throws RemoteException {
        
        //TODO: CHECK TOKEN IS VALID HERE

        AuctionItem[] array = itemsMap.values().toArray(new AuctionItem[0]); // turn the item hashmap into an array
        return array;

    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID, String token) {

        //TODO: CHECK TOKEN IS VALID HERE

        AuctionItem item = itemsMap.get(itemID); // Check if the item exists
        if (item == null) {
            System.out.println("AuctionItem of that itemID does not exist");
            return null;
        }

        if (userAuctionsMap.containsKey(userID) && item.itemID == itemID) { // Check the user closing the auction is the
                                                                            // one who created it

            AuctionSaleItem saleItem = userAuctionsMap.get(userID);

            if (item.highestBid >= saleItem.reservePrice) { // Check if the reserve price has been met

                AuctionResult result = new AuctionResult(); // Create a new instance of an AuctionResult using details
                                                            // from various hashmaps
                int winningUserID = highestBidderMap.get(itemID);
                result.winningEmail = usersMap.get(winningUserID);
                result.winningPrice = item.highestBid;

                itemsMap.remove(itemID); // Remove the item from auction

                return result;
            } else {
                System.out.println("Reserve price not met");
                return null;
            }

        } else {
            System.out.println("UserID does not match the given AuctionItem's itemID");
            return null;
        }

    }

    @Override
    public boolean bid(int userID, int itemID, int price, String token) throws RemoteException {

        //TODO: CHECK TOKEN IS VALID HERE

        AuctionItem item = itemsMap.get(itemID); // Check that the item exists
        if (item == null) {
            System.out.println("AuctionItem of that itemID does not exist");
            return false;
        }

        if (price > item.highestBid) { // Check that the amount offered is higher than the current bid
            item.highestBid = price; // Update highest bid
            highestBidderMap.put(item.itemID, userID); // place in hashmap, showing who the highest bidder (userID) is
                                                       // for each given itemID, this overwrites the previous highest
                                                       // bidder
            return true;
        } else {
            System.out.println("Bid is not higher than the current highest bid");
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            AuctionServer a1 = new AuctionServer(); // creating a server with the name 'Auction'
            String name = "Auction";

            Auction stub = (Auction) UnicastRemoteObject.exportObject(a1, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);

            generateKeyPair(); // generate a new keyPair

            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

}
