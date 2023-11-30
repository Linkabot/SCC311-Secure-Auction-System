import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

class Challenge{
    private String line;
    private byte[] privateKey;

    /**
    * The Challenge class is used for the 5-step Authentication. 
    * Data is signed with a private key and will later be verified from the 
    * Autenticate class with the corresponding public key
    */
    public Challenge(){
    }

    /**
    * @param line String that gets converted into a byte array for 
    * the signature. 
    * @param privateKey Private key that will be used to to sign data 
    * @return will return the signed byte array signature or Null if the process 
    * failled. 
    */
    public byte[] chal(String line, byte[] privateKey){
        try {
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privateKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey pvt = kf.generatePrivate(ks);
    
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(pvt);
    
            sign.update(line.getBytes());
            byte[] signature = sign.sign();
            return signature;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getLine() {
        return line;
    }
    public byte[] getPrivateKey() {
        return privateKey;
    }
}

class Authenticate{
    private String line;
    private byte[] publicKey;

    /**
    * The Authenticate class is used in the 5-step authentication. 
    * This will use the public key corresponding to the private key 
    * that was used to sign the signature and verify the signatures 
    * byte array
    */
    public Authenticate(){
    }
    /**
    * @param line This should be the same String that was used to 
    * challenge this Authentication. This is what will be verified.
    * @param publicKey The corressponding public key to the private 
    * key used to challenge this authentication. 
    * @param signature The Signature signed with the private key that 
    * needs to be verified. 
    * @return will return a Boolean of whether the verification was 
    * true or false. 
    */
    public Boolean auth(String line, byte[] publicKey, byte [] signature){
        try {
            X509EncodedKeySpec ksi = new X509EncodedKeySpec(publicKey);
            KeyFactory kfi = KeyFactory.getInstance("RSA");
            PublicKey pub = kfi.generatePublic(ksi);
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(pub);
            sign.update(line.getBytes());
            if (sign.verify(signature)){ 
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getLine() {
        return line;
    }

    public byte[] getpublicKey() {
        return publicKey;
    }
}

public class Replica implements Auction, Update{

    // Paths to the servers public and private keys 
    public Path pathPrivate = Paths.get("../keys/server_private.key").toAbsolutePath();
    public Path pathPublic = Paths.get("../keys/server_public.key").toAbsolutePath();
    // ID to replica

    // Hashmaps needed to store User, Item and Auction Information. 
    HashMap<Integer, User> userNum = new HashMap<>();
    HashMap<String, User> userEmail = new HashMap<>();
    // all items and auctions in an array are live items and auctions. 
    HashMap<Integer, allAuctions> auctions = new HashMap<>();
    HashMap<Integer, AuctionItem> allItems = new HashMap<>();
    
    // Count used so every item and user has a different ID
    int userCount = 1;
    int itemCount = 1;

    private int replicaID = 0;
    private String replicaName = "";

    public Replica(int n, String name) {
        super();
        replicaID = n;
        replicaName = name;
        try {
            updateReplica();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateReplica()throws RemoteException{
        try {
            Registry registry = LocateRegistry.getRegistry();
            String names [] = registry.list();

            for (String name : names){
                if ((name.contains("Replica")) && !(name.equals(replicaName))){
                    Update rep = (Update) registry.lookup(name);
                    try {
                        if (rep.isAlive()){
                            userNum = rep.getUserNum();
                            userEmail = rep.getUserEmail();
                            auctions = rep.getAuction();
                            allItems = rep.getAllItems();
                            userCount = rep.getUserCount();
                            itemCount = rep.getItemCount();
                            break;
                        }
                    } catch (Exception e) {
                        registry.unbind(name);
                    }      
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException{
        return replicaID;
    }
    @Override
    public HashMap<Integer, User> getUserNum() throws RemoteException{
        return userNum;
    }
    @Override
    public HashMap<String, User> getUserEmail() throws RemoteException{
        return userEmail;
    }
    @Override
    public HashMap<Integer, allAuctions> getAuction() throws RemoteException{
        return auctions;
    }
    @Override
    public HashMap<Integer, AuctionItem> getAllItems() throws RemoteException{
        return allItems;
    }
    @Override
    public int getUserCount() throws RemoteException{
        return userCount;
    }
    @Override
    public int getItemCount() throws RemoteException{
        return itemCount;
    }
    @Override
    public void setUserNum(HashMap<Integer, User> userN) throws RemoteException{
        userNum = userN;
    }

    @Override
    public void setUserEmail(HashMap<String, User> userE) throws RemoteException{
        userEmail = userE;
    }

    @Override
    public void setAuction(HashMap<Integer, allAuctions> a) throws RemoteException{
        auctions = a;
    }

    @Override
    public void setAllItems(HashMap<Integer, AuctionItem> allI) throws RemoteException{
        allItems = allI;
    }

    @Override
    public void setUserCount(int count) throws RemoteException{
        userCount = count;
    }

    @Override
    public void setItemCount(int count) throws RemoteException{
        itemCount = count;
    }

    @Override
    public boolean isAlive()throws RemoteException{
        return true;
    }
  
    //returns user information
    public NewUserInfo getUserInfomaton(String email){
        User userInfo = userEmail.get(email);
        return userInfo.getUserInfo();
    }

    // Checks Server to see if the email exists. 
    public boolean userValidation(String email){
        if(userEmail.containsKey(email)){
            return false;
        }    
        return true;
    }

    
    /**
    * returns the entire information of a specific item that 
    * using item ID. 
    */
    public AuctionItem getSpec(int itemID) throws RemoteException{
        allAuctions auctionInfo = auctions.get(itemID);
        if (auctionInfo == null){
            return null;
        }

        AuctionItem object = auctionInfo.getItem();
        return object;
    }

    /**
    * Clients will input their email before being allowed
    * accsess to use the auctions service. 
    * If the email is new, the server will create private 
    * and public keys and store them along with a user ID. 
    * If email already exists, the server will return the 
    * user information. 
    */
    public synchronized NewUserInfo newUser(String email) throws RemoteException{ 
        NewUserInfo newUserInfo = new NewUserInfo();
        // checks if email exists. 
        if (userValidation(email.trim())){
            try {
                // key generation for new user
                Generation keys = new Generation();
                keys.keyMaker(); 
                newUserInfo.publicKey = keys.getPub().getEncoded();               
                newUserInfo.privateKey = keys.getPriv().getEncoded();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            newUserInfo.userID = userCount;
            userCount += 1;

            // store user into Server. 
            userNum.put(newUserInfo.userID, new User(email, newUserInfo));
            userEmail.put(email.trim(), new User(email, newUserInfo));

            try {
                Registry registry = LocateRegistry.getRegistry();
                String names [] = registry.list();

                for (String name : names){
                    if ((name.contains("Replica")) && !(name.equals(replicaName))){
                        Update rep = (Update) registry.lookup(name);
                        try {
                            if (rep.isAlive()){
                                rep.setUserEmail(userEmail);
                                rep.setUserNum(userNum);
                                rep.setUserCount(userCount);
                            }
                        } catch (Exception e) {
                            registry.unbind(name);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return newUserInfo;
        }
        else {
            NewUserInfo user = getUserInfomaton(email.trim());
            return user;
        }
    }

    /**
    * Clients call this to make a new Auction. 
    * User ID to record the Seller of the Auction. 
    * Clients will input the name of the item and description of the item. 
    * Item id is set by the count, and the highest bid starts at 0
    */
    public synchronized int newAuction(int userID, AuctionSaleItem item) throws RemoteException{
        AuctionItem newItem = new AuctionItem();

        int id = itemCount;
        itemCount += 1;

        newItem.itemID = id;
        newItem.name = item.name;
        newItem.description = item.description;
        newItem.highestBid = 0; 
        
        //stored in an all items array and auctions array 
        allItems.put(newItem.itemID, newItem);
        auctions.put(newItem.itemID, new allAuctions(userID, newItem, item.reservePrice));

        try {
            Registry registry = LocateRegistry.getRegistry();
            String names [] = registry.list();
            for (String name : names){
                if ((name.contains("Replica")) && !(name.equals(replicaName))){
                    Update rep = (Update) registry.lookup(name);
                    try {
                        if (rep.isAlive()){
                            rep.setAllItems(allItems);
                            rep.setAuction(auctions);
                            rep.setItemCount(itemCount);
                        }
                    } catch (Exception e) {
                        registry.unbind(name);
                    }
                }   
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newItem.itemID;
    }
    /**
    * returns all items from the items array 
    */
    public AuctionItem[] listItems() throws RemoteException{
        AuctionItem [] auctionList = allItems.values().toArray(new AuctionItem[auctions.size()]);
        return auctionList;
    }

    /**
    * Attemps to close an Auction
    * If the User ID matches the auction user ID, then it can be closed. 
    * If not, the Auction will stay live and return Null to the client. 
    * The server will then check if anyone has bid on the item and if 
    * the highest bid has a higher value then the reserved price. 
    * If they pass these two checks, the auction and item will be removed 
    * from the arrays and the function will return the Winning email and Price. 
    * If the checks do not pass, this function will return empty winners and a price of 0. 
    * The auction will still be closed. 
    */
    public synchronized AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException{
        AuctionCloseInfo winner = new AuctionCloseInfo();
        
        allAuctions auction = auctions.get(itemID);
        if(auction == null){
            return null;
        }

        // Correct seller check
        if((auction.getSeller() == userID)){ 
            // Check for if a Buyer exists 
            if(auction.getBuyer() != 0){
                // Checks if highest bid is higher then reserve price.
                if(auction.getItem().highestBid > auction.getReservePrice()){
                    winner.winningEmail = userNum.get(auction.getBuyer()).getEmail();
                    winner.winningPrice = auction.getItem().highestBid;  
                }    
                else{
                    winner.winningEmail = "";
                    winner.winningPrice = 0;
                }                      
            }
            else {
                winner.winningEmail = "none";
                winner.winningPrice = 0;
            }
            // remove item from both arrays 
            auctions.remove(itemID);
            allItems.remove(itemID);
            try {
                Registry registry = LocateRegistry.getRegistry();
                String names [] = registry.list();
                for (String name : names){
                    if ((name.contains("Replica")) && !(name.equals(replicaName))){
                        Update rep = (Update) registry.lookup(name);
                        try {
                            if (rep.isAlive()){
                                rep.setAllItems(allItems);
                                rep.setAuction(auctions);
                            }
                        } catch (Exception e) {
                            registry.unbind(name);
                        }    
                    }   
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return winner;
        }
        else {
            return null;
        }
    }

    // Clients can bid for items.
    // Server checks if the bid is higher then the price given. 
    // If it is, the current buyer will be updated and the highest bid will be changed.
    // If the check doesnt go through, the function will return false. 
    public synchronized boolean bid(int userID, int itemID, int price) throws RemoteException{
        allAuctions auction = auctions.get(itemID);
        AuctionItem currentItem = allItems.get(itemID);
        if (auction == null){
            return false;
        }
        else{
            if(auction.getItem().highestBid < price){
                auction.setBuyer(userID);
                auction.getItem().highestBid = price;
                currentItem.highestBid = price;
                try {
                    Registry registry = LocateRegistry.getRegistry();
                    String names [] = registry.list();
                    for (String name : names){
                        if ((name.contains("Replica")) && !(name.equals(replicaName))){
                            Update rep = (Update) registry.lookup(name);
                            try {
                                if (rep.isAlive()){
                                    rep.setAllItems(allItems);
                                    rep.setAuction(auctions);
                                }
                            } catch (Exception e) {
                                registry.unbind(name);
                            }    
                        }   
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }    
        }   
        return false;
    }

    /**
    * Uses Challenge class to return signed signautre. 
    * The Server will use the String "auction" to be verified.
    * The Server will also use its private key to sign the data. 
    */
    @Override
    public byte[] challenge(int userID) throws RemoteException {
        byte[] privateData;
        try {
            privateData = Files.readAllBytes(pathPrivate);
            Challenge challenge = new Challenge();
            byte[] sig = challenge.chal("auction", privateData);
            return sig;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
    * This will use the Authenticate class to verify the Clients signature.
    * UserID needed to retreive public key and email.
    * Signature needed so the server can verify it. 
    * returns a Boolean depending on the verification outcome. 
    */
    @Override
    public boolean authenticate(int userID, byte[] signature) throws RemoteException {
        try {
            byte [] publicData = userNum.get(userID).getUserInfo().publicKey;
            Authenticate authenticate = new Authenticate();
            if (authenticate.auth(userNum.get(userID).getEmail(), publicData, signature)){                      
                return true;
            }
            else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        try {

            if (args.length < 1) {
                System.out.println("Usage: java Client n");
                return;
            }
         
            int n = Integer. parseInt(args[0]);
            String name = "Replica" + n;
            Replica s = new Replica(n, name);   

            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
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