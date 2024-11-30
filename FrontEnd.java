import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class FrontEnd implements Auction{

    private int MAX_NUMBER_OF_REPLICAS = 10;
    private int primaryReplicaID = -1;
    private List<Integer> replicaIDs = new ArrayList<>();

    @Override
    public int register(String email) throws RemoteException {
        System.out.println("LOOKING FOR REPLICAS");
        lookForReplicas();
        System.out.println("PRIMARY REPLICA IS ID OF: " );
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
    public ReplicaInterface connectToPrimaryReplica(){
        
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            
            int primaryReplicaID = getPrimaryReplicaID();
            ReplicaInterface primary = (ReplicaInterface) registry.lookup("Replica" + primaryReplicaID);
            primary.setPrimaryReplicaID(primaryReplicaID);
            primary.updateReplicaIDList(replicaIDs);

            return primary;
        } 
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
        return null;
    }

    //method to choose a primary replica?

    private void lookForReplicas(){

        for(int i =1; i <= MAX_NUMBER_OF_REPLICAS; i++){
            
            String name = "Replica" + i;

            try {
                Registry registry = LocateRegistry.getRegistry("localhost");
                ReplicaInterface replica = (ReplicaInterface) registry.lookup(name);
                
                if(!replicaIDs.contains(i)){
                    replicaIDs.add(i);
                }

                if(primaryReplicaID == -1){
                    primaryReplicaID = i;
                }

            }
            catch (Exception e) {
                // TODO: handle exception
            }

        }

    }

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
