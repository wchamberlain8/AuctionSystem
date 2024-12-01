import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class FrontEnd implements Auction{

    private static int MAX_NUMBER_OF_REPLICAS = 10;
    private static int primaryReplicaID = -1;
    private static List<Integer> replicaIDs = new ArrayList<>();
    
    @Override
    public int register(String email) throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().register(email);
        
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().getSpec(itemID);
        
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().newAuction(userID, item);
        
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().listItems();
        
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().closeAuction(userID, itemID);
        
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        lookForReplicas();
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

    private static void choosePrimaryReplica(){
        if(!replicaIDs.isEmpty()){
            primaryReplicaID = replicaIDs.get(0);
            System.out.println("NEW PRIMARY CHOSEN = Replica" + primaryReplicaID);
        }
        else{
            primaryReplicaID = -1;
        }
    }
        
    private static void lookForReplicas(){
            
        for(int i =1; i <= MAX_NUMBER_OF_REPLICAS; i++){
                
            String name = "Replica" + i;
            System.out.println("ATTEMPTING LOOKUP FOR " + name);
    
            try {
                Registry registry = LocateRegistry.getRegistry("localhost");
                ReplicaInterface replica = (ReplicaInterface) registry.lookup(name);
                
                if(!replicaIDs.contains(i)){
                    replicaIDs.add(i);
                    System.out.println(name + " is live and added to the list");
                }

                if(primaryReplicaID == -1){
                    primaryReplicaID = i;
                    System.out.println(name + " has been set as primary replica");
                }

                if(replicaIDs.contains(i)){
                    System.out.println(name + " is live and already is in the list");
                }

            }
            catch (Exception e) {

                if(replicaIDs.contains(i)){
                    replicaIDs.remove(Integer.valueOf(i));
                    System.out.println(name + " has been removed from the list of replicas");
                }

                if(primaryReplicaID == i){
                    System.out.println("Primary replica has been killed, choosing a new one...");
                    choosePrimaryReplica();
                }
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
            lookForReplicas();
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
    
}
