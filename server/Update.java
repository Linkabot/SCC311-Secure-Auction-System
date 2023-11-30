import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface Update extends Remote {
    public void setUserNum(HashMap<Integer, User> userN) throws RemoteException;

    public void setUserEmail(HashMap<String, User> userE) throws RemoteException;

    public void setAuction(HashMap<Integer, allAuctions> a) throws RemoteException;

    public void setAllItems(HashMap<Integer, AuctionItem> allI) throws RemoteException;

    public void setUserCount(int count) throws RemoteException;

    public void setItemCount(int count) throws RemoteException;

    public HashMap<Integer, User> getUserNum() throws RemoteException;

    public HashMap<String, User> getUserEmail() throws RemoteException;

    public HashMap<Integer, allAuctions> getAuction() throws RemoteException;

    public HashMap<Integer, AuctionItem> getAllItems() throws RemoteException;

    public int getUserCount() throws RemoteException;

    public int getItemCount() throws RemoteException;

    public void updateReplica()throws RemoteException;

    public boolean isAlive() throws RemoteException;
}