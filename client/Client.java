  import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
public class Client {
    static public Path pathPublic = Paths.get("../keys/server_public.key").toAbsolutePath();
    static public NewUserInfo registration;
    static public String email;

    /**
     * calls Authenticate class function to verify Server Challenge function 
     * @param signature signature returned from the server challenge
     * @return will return boolean on the verification
     */
    static public Boolean check(byte[] signature){
      try {
        byte [] publicData = Files.readAllBytes(pathPublic);
        Authenticate authenticate = new Authenticate();
        if (authenticate.auth("auction", publicData, signature)){                     
            return true;
        }
        else{
            return false;
        }
      } catch (Exception e) {

      }
      return false;
    }

    /**
     * Calls Challenge class function to create a signed signature to 
     * be sent to the server
     * @param email needed for the byte array. This data will be signed by 
     * the Client private key. 
     * @param privateKey
     * Client Private key to sign the data. 
     * @return the Signature to be sent to the Server to be verified. 
     */
    static public byte[] chal(String email, byte[] privateKey){
      try {
        Challenge challenge = new Challenge();
        byte[] sig = challenge.chal(email, privateKey);
        return sig;
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    /**
     * function used for the entire log in. Calls both the check and chal functions. 
     * @param server needed to call server functions
     * @return Boolean whether the login passed or not. 
     */
    static public Boolean login(Auction server){
      try {
        byte[] sig = server.challenge(registration.userID);
            if (sig != null && sig.length > 0 ){
              //Authenticate   
              if (check(sig)){
              sig = chal(email, registration.privateKey);
                //Challenge
                if (server.authenticate(registration.userID, sig)){
                  return true;
                }
              }
            }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return false;
    }
    /**
     * Menu with all options of what the client can do with the auction
     * @param scan needed to get Client response . 
     * @return a selected number from the Client of what they want. 
     */
    static public int menu(Scanner scan){
      int selection;

      System.out.println("\n\nSelect a number you want: ");   
      System.out.println("1. Open new Auction");
      System.out.println("2. Close Auction");
      System.out.println("3. List all items");
      System.out.println("4. Make bid");
      System.out.println("5. Get the Spec of the whole item");

      selection = scan.nextInt();
      return selection; 
    }

    /**
     * Client will use this to call the server to open a new auction. 
     * @param server needed to call Server function
     * @param itemName Client will input what they want the auction item to be called
     * @param itemDescrip Client will give description of the item 
     * @param itemReservePrice Client will set the reserve price of the item 
     * @param scan needed to get client input. 
     */
    static public void openAuction(Auction server,  String itemName, String itemDescrip, int itemReservePrice, Scanner scan){
      System.out.print("Enter Item name: ");
      itemName = scan.next();
      scan.nextLine();
      System.out.print("Enter Description of Item: ");
      itemDescrip = scan.next();
      scan.nextLine();
      System.out.print("Enter the Reserve Price: ");
      itemReservePrice = scan.nextInt();
      AuctionSaleItem newItem = new AuctionSaleItem();
      newItem.name = itemName;
      newItem.description = itemDescrip;
      newItem.reservePrice = itemReservePrice;
      try {
        server.newAuction(registration.userID, newItem);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    /**
     * Client will attempt to close a specific auction. 
     * Only auctions that the user has made will be closed. 
     * @param server needed to call Server functions
     * @param itemID Client inputs Item ID of the auction they want to close
     * @param winner is needed to gain who won the Auction and what the highest bid was
     * @param scan to get Client input
     */
    static public void closeAuction(Auction server, int itemID, AuctionCloseInfo winner, Scanner scan){
      System.out.print("Enter ItemID of closing Auction: ");
      itemID = scan.nextInt();
      try {
        winner = server.closeAuction(registration.userID, itemID);
      } catch (Exception e) {
        e.printStackTrace();
      }
      //Depending on the server return statement, these messages will be sent. 
      if (winner == null){
        System.out.println("\n\nThere has been an Error");
      }
      else if ((winner.winningEmail.equals("none")) && (winner.winningPrice == 0)){
        System.out.println("Nobody bid on this item.\nAuction now closed.");
      }
      else if ((winner.winningEmail.equals("")) && (winner.winningPrice == 0)){
        System.out.println("The highest bid was below the reserve price. \n No winning Bidder");
      }
      else{
        System.out.println("The winner of the auction is: " + winner.winningEmail + "\nwith the Winning Price of: " + winner.winningPrice);
      }
    }

    /**
     * Clients use this to gain a list of all live auctions that are currently going on. 
     * @param server needed to call Server functions. 
     * @param auctionlist Where the returned auction items will be stored. 
     */
    static public void listAuctions(Auction server, AuctionItem[] auctionlist){
      try {
        System.out.println("\n\nThis is the list of AuctionItems: ");
        auctionlist = server.listItems();
      } catch (Exception e) {
        e.printStackTrace();
      }
      // prints all live auctions. 
      for(AuctionItem list : auctionlist){
      System.out.println("--------------------------------------------------------------------------------");
      System.out.println("| ID: " + list.itemID + " | NAME: " + list.name + " | DESCRIPTION: " + list.description + " | HIGHEST BID: " + list.highestBid + " |");
      System.out.println("--------------------------------------------------------------------------------");
      }
    }

    /**
     * Client uses this to make a bid on an auction in the Server
     * @param server needed to call server functions
     * @param itemID used to specify which auction the Client wants to bid on
     * @param price Client will input price of what they want to bid with. 
     * @param scan needed to get user input. 
     */
    static public void makeBid(Auction server, int itemID, int price, Scanner scan){
      System.out.print("Enter the itemID of what you want to bid for: ");
      itemID = scan.nextInt();
      System.out.print("Enter the Price you want to bid with: ");
      price = scan.nextInt();
      try {
        if (server.bid(registration.userID, itemID, price)){
          System.out.println("\n\nbid successful");
        }
        else{
          System.out.println("\n\nbid not succesfull\nEither bid was too low or Incorrect Item ID was inputted");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    /**
     * Will get the full spec of a specific item. 
     * @param server needed to call server functions
     * @param itemID specifies which item the client wants 
     * @param result is the returned auction item 
     * @param scan needed to get client input
     */
    static public void getItemSpec(Auction server, int itemID, AuctionItem result, Scanner scan){
      System.out.print("Enter the itemID of the Spec you want: ");
      itemID = scan.nextInt();
      try {
        result = server.getSpec(itemID);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (result == null){
        System.out.println("There is no item with this ID");
      }
      else{
        System.out.println("| ID: " + result.itemID + " | NAME: " + result.name + " | DESCRIPTION: " + result.description + " | HIGHEST BID: " + result.highestBid + " |");
      }
    }

    /**
     * Used for client login to create or/and retreive user information. 
     * @param server to call server function 
     * @param scan for client input 
     */
    static public void emailInput(Auction server, Scanner scan){
      System.out.println("Enter Email: ");
      email = scan.next();
      try { 
        registration = server.newUser(email);
      } catch (Exception e){
      }
    }
    public static void main(String[] args) {
        try {
            //Setting up Auction
            String name = "FrontEnd";
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction server = (Auction) registry.lookup(name);
          
            //Start scanner
            Scanner scan = new Scanner(System.in);

            //Email request
            emailInput(server, scan);
            
            //login is the authentication process. If it passes, they can continue to the auction
            if(login(server)){
              //while true so they can keep on making new requests and functions in the auction window.
                while(true){
                  String itemName = "";
                  String itemDescrip = "";
                  int itemReservePrice = 0;
                  int itemID = 0;
                  AuctionCloseInfo winner = null;
                  AuctionItem[] auctionlist = null;
                  int price = 0;
                  AuctionItem result = null;

                    int selectedNum;
                    selectedNum = menu(scan);

                    switch(selectedNum){
                        case 1: 
                          openAuction(server, itemName, itemDescrip, itemReservePrice, scan);
                          break;
                        case 2:
                          closeAuction(server, itemID, winner, scan);                         
                          break;

                        case 3:
                          listAuctions(server, auctionlist);
                          break;
                        case 4:
                          makeBid(server, itemID, price, scan);
                          break;

                        case 5:        
                          getItemSpec(server, itemID, result, scan);
                          break;
                        default:
                    }
                }
            }                       
        } catch (Exception e) {
          e.printStackTrace();
        }    
    }
}