import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class FrontEnd implements Auction{

    private int primaryReplicaID = 1; //hard coded for now (default primary replica ID)
    private int[] replicaIDs = {1, 2, 3}; //same here TODO: Change this

    @Override
    public int register(String email) throws RemoteException {
        return connectToPrimaryReplica().register(email);
       
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        return connectToPrimaryReplica().getSpec(itemID);
        
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        return connectToPrimaryReplica().newAuction(userID, item);
       
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        return connectToPrimaryReplica().listItems();
      
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        return connectToPrimaryReplica().closeAuction(userID, itemID);
       
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        return connectToPrimaryReplica().bid(userID, itemID, price);
        
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        return primaryReplicaID;
       
    }

    //Helper function to connect to the primary replica using the getPrimaryReplicaID method
    public Auction connectToPrimaryReplica(){
        
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            
            int primaryReplicaID = getPrimaryReplicaID();
            Auction primary = (Auction) registry.lookup("Replica" + primaryReplicaID);

            return primary;
        } 
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
        return null;
    }

    //method to choose a primary replica?

    public static void main(String[] args) {
        try {
            FrontEnd frontEndServer = new FrontEnd(); // creating a server instance
            String name = "FrontEnd";

            Auction stub = (Auction) UnicastRemoteObject.exportObject(frontEndServer, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);

            System.out.println("FrontEnd server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
    
}
