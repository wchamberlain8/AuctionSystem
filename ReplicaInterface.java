import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface ReplicaInterface extends Remote{

    public int register(String email) throws RemoteException;

    public AuctionItem getSpec(int itemID) throws RemoteException;
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException;
    public AuctionItem[] listItems() throws RemoteException;
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException;
    public boolean bid(int userID, int itemID, int price) throws RemoteException;
    public int getPrimaryReplicaID() throws RemoteException;

    public void updateState(HashMap<Integer, AuctionItem> itemsMap, HashMap<Integer, String> usersMap, HashMap<Integer, AuctionSaleItem> userAuctionsMap, HashMap<Integer, Integer> highestBidderMap) throws RemoteException;
    public void setPrimaryReplicaID(int newID) throws RemoteException;
    public void updateReplicaIDList(List<Integer> replicaIDs) throws RemoteException;
    public void syncNewReplica(String name) throws RemoteException;
    
}
