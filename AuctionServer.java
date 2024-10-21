import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import javax.crypto.SealedObject;

public class AuctionServer implements Auction {

    private HashMap<Integer, AuctionItem> items;

    public AuctionServer() throws RemoteException{
        super();
        items = new HashMap<>();
        items.put(1, new AuctionItem(1, "Car", "null", 1000));
        items.put(2, new AuctionItem(2, "PC", "null", 500)); 
    }

    @Override
    public SealedObject getSpec(int itemID) throws RemoteException {
        AuctionItem item = items.get(itemID);
    }

    public static void main(String[] args) {
        try {
            AuctionServer a1 = new AuctionServer();
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
