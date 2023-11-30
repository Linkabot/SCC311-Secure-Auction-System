    import java.rmi.RemoteException;
    import java.rmi.registry.LocateRegistry;
    import java.rmi.registry.Registry;
    import java.rmi.server.UnicastRemoteObject;


    public class Frontend implements Auction {

    private static Registry registry;
    private Auction primaryReplica;
    private int PrimaryReplicaId;
    private Update rep;


    public Frontend(){
        super();
    }

    public NewUserInfo newUser(String email) throws RemoteException{
        try {
            if((primaryReplica != null) && (rep.isAlive())){
                NewUserInfo user = primaryReplica.newUser(email);
                return user;
            } 
            else{
                PrimaryReplicaId = getPrimaryReplicaID();
                NewUserInfo user = primaryReplica.newUser(email);
                return user;
            }
        } catch (Exception e) {
            while (true){
                PrimaryReplicaId = getPrimaryReplicaID();
                NewUserInfo user = primaryReplica.newUser(email);
                return user;
            }
        }
    }

    public byte[] challenge(int userID) throws RemoteException{

        try {
            byte[] signature = primaryReplica.challenge(userID);
            return signature;
        } catch (Exception e) {
            while (true){
                PrimaryReplicaId = getPrimaryReplicaID();
                byte[] signature = primaryReplica.challenge(userID);
                return signature;
            }
        }
    }

    public boolean authenticate(int userID, byte signature[]) throws RemoteException{
        try {
            if(primaryReplica.authenticate(userID, signature)){
                return true;
            }
            return false;
        } catch (Exception e) {
            while(true){
                PrimaryReplicaId = getPrimaryReplicaID();
                if(primaryReplica.authenticate(userID, signature)){
                    return true;
                }
                return false;
            }
        }
    }

    public AuctionItem getSpec(int itemID) throws RemoteException{
        try {
            AuctionItem item = primaryReplica.getSpec(itemID);
            return item;
        } catch (Exception e) {
            while (true){
                PrimaryReplicaId = getPrimaryReplicaID();
                AuctionItem item = primaryReplica.getSpec(itemID);
                return item;
            }
        }
    }

    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException{
        try {
            int itemID = primaryReplica.newAuction(userID, item);
            return itemID;
        } catch (Exception e) {
            while(true){
                PrimaryReplicaId = getPrimaryReplicaID();
                int itemID = primaryReplica.newAuction(userID, item);
                return itemID;
            } 
        }
    }

    public AuctionItem[] listItems() throws RemoteException{
        try {
            AuctionItem[] items = primaryReplica.listItems();
            return items;
        } catch (Exception e) {
            while(true){
                PrimaryReplicaId = getPrimaryReplicaID();
                AuctionItem[] items = primaryReplica.listItems();
                return items;
            }
        }
    }

    public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException{
        try {
            AuctionCloseInfo winner = primaryReplica.closeAuction(userID, itemID);
            return  winner;
        } catch (Exception e) {
            while(true){
                PrimaryReplicaId = getPrimaryReplicaID();
                AuctionCloseInfo winner = primaryReplica.closeAuction(userID, itemID);
                return  winner;
            }
        }
    }

    public boolean bid(int userID, int itemID, int price) throws RemoteException{
        try {
            if (primaryReplica.bid(userID, itemID, price)){
                return true;
            }
            return false;
        } catch (Exception e) {
            while(true){
                PrimaryReplicaId = getPrimaryReplicaID();
                if (primaryReplica.bid(userID, itemID, price)){
                    return true;
                }
                return false;
            }
        }
    }

    public int getPrimaryReplicaID() throws RemoteException{   
        try {
            registry = LocateRegistry.getRegistry();
            String names [] = registry.list();

            for (String name : names){
                if (name.contains("Replica")){
                    rep = (Update) registry.lookup(name);
                    try {
                        if (rep.isAlive()){
                            primaryReplica = (Auction) registry.lookup(name);
                            PrimaryReplicaId = primaryReplica.getPrimaryReplicaID();
                            return PrimaryReplicaId;
                        }
                    } catch (Exception e) {
                        primaryReplica = null;
                        registry.unbind(name);
                    }        
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }  
        return 0;
    }

    public static void main(String[] args) {
        try {
            //Create Front end Server
            Frontend s = new Frontend();
            String name = "FrontEnd";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } 
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}