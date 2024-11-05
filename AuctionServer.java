import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class AuctionServer implements Auction {

    private HashMap<Integer, AuctionItem> itemsMap = new HashMap<>();
    private HashMap<Integer, String> usersMap = new HashMap<>();
    private HashMap<Integer, AuctionSaleItem> userAuctionsMap = new HashMap<>();
    private HashMap<Integer, Integer> highestBidderMap = new HashMap<>();
    private int userIDCount = 100;
    private int auctionIDCount = 1000;

    public AuctionServer() throws RemoteException{
        super();
    
    }

    //Helper function to create a new AuctionItem since constructor not allowed
    public AuctionItem createItem(int itemID, String name, String description, int highestBid){
        AuctionItem item = new AuctionItem();
        item.itemID = itemID;
        item.name = name;
        item.description = description;
        item.highestBid = highestBid;

        return item;
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {

        if(itemsMap.isEmpty()){    //return null if the hashmap is empty
            return null;
        }

        AuctionItem item = itemsMap.get(itemID); //Get the AuctionItem from the hashmap with the relative itemID
        return item;
       
    }

    // task 2 methods below!

    @Override
    public int register(String email) throws RemoteException{
        userIDCount++;
        usersMap.put(userIDCount, email);
        return userIDCount;
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException{
        
        auctionIDCount++;
        userAuctionsMap.put(userID, item);
        
        //create new AuctionItem from AuctionSaleItem info

        AuctionItem newItem = createItem(auctionIDCount, item.name, item.description, 0);
        itemsMap.put(auctionIDCount, newItem);

        return auctionIDCount;

    }

    @Override
    public AuctionItem[] listItems() throws RemoteException{

        AuctionItem[] array = itemsMap.values().toArray(new AuctionItem[0]);
        return array;

    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID){

        AuctionItem item = itemsMap.get(itemID);
        if(item == null){
            System.out.println("AuctionItem of that itemID does not exist");
            return null;
        }
        
        if (userAuctionsMap.containsKey(userID) && item.itemID == itemID) {

            AuctionSaleItem saleItem = userAuctionsMap.get(userID);

            if(item.highestBid >= saleItem.reservePrice){
                AuctionResult result = new AuctionResult();
                int winningUserID = highestBidderMap.get(itemID);
                result.winningEmail = usersMap.get(winningUserID);
                result.winningPrice = item.highestBid;


                itemsMap.remove(itemID);
                return result;
            }
            else{
                System.out.println("Reserve price not met");
                return null;
            }

        } 
        else{
            System.out.println("UserID does not match the given AuctionItem's itemID");
            return null;
        }

    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException{
            
            AuctionItem item = itemsMap.get(itemID);
            if(item == null){
                System.out.println("AuctionItem of that itemID does not exist");
                return false;
            }
    
            if(price > item.highestBid){
                item.highestBid = price;
                highestBidderMap.put(userID, item.itemID);
                return true;
            }
            else{
                System.out.println("Bid is not higher than the current highest bid");
                return false;
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
