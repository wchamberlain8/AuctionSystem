import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.Random;

public class AuctionServer implements Auction {

    //Create and initialise hashmaps
    private HashMap<Integer, AuctionItem> itemsMap = new HashMap<>();
    private HashMap<Integer, String> usersMap = new HashMap<>();
    private HashMap<Integer, AuctionSaleItem> userAuctionsMap = new HashMap<>();
    private HashMap<Integer, Integer> highestBidderMap = new HashMap<>();

    Random random = new Random();

    public AuctionServer() throws RemoteException {
        super();

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
    public AuctionItem getSpec(int itemID) throws RemoteException {

        if (itemsMap.isEmpty()) { // return null if the hashmap is empty
            return null;
        }

        AuctionItem item = itemsMap.get(itemID); // Get the AuctionItem from the hashmap with the relative itemID
        return item;

    }

    @Override
    public int register(String email) throws RemoteException {

        int userID = random.nextInt(100); // generate random userID
        usersMap.put(userID, email); // place into hashmap, userID being the key for each email, to access a user's email
        return userID;
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {

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
    public AuctionItem[] listItems() throws RemoteException {

        AuctionItem[] array = itemsMap.values().toArray(new AuctionItem[0]); // turn the item hashmap into an array
        return array;

    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) {

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
    public boolean bid(int userID, int itemID, int price) throws RemoteException {

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

    public int getPrimaryReplicaID() throws RemoteException{
        return 0;
    }

    public static void main(String[] args) {
        try {
            AuctionServer a1 = new AuctionServer(); // creating a server with the name 'Auction'
            String name = "Auction";

            Auction stub = (Auction) UnicastRemoteObject.exportObject(a1, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);

            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

}
