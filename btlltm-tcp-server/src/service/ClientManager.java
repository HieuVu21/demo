package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author admin
 */
public class ClientManager {
    ArrayList<Client> clients;
    private Map<String, String> activeCalls;
    public ClientManager() {
        clients = new ArrayList<>();
        activeCalls = new HashMap<>();
    }

    public boolean add(Client c) {
        if (!clients.contains(c)) {
            clients.add(c);
            return true;
        }
        return true;
    }
    public void addActiveCall(String user1, String user2) {
        activeCalls.put(user1, user2);
        activeCalls.put(user2, user1);
    }

    public void removeActiveCall(String user1, String user2) {
        activeCalls.remove(user1);
        activeCalls.remove(user2);
    }

    public boolean remove(Client c) {
        if (clients.contains(c)) {
            clients.remove(c);
            return true;
        }
        return false;
    }

    public Client find(String username) {
        for (Client c : clients) {
            if (c.getLoginUser()!= null && c.getLoginUser().equals(username)) {
                return c;
            }
        }
        return null;
    }

    public void broadcast(String msg) {
        clients.forEach((c) -> {
            c.sendData(msg);
        });
    }

    public void  sendToAClient (String username, String msg) {
        clients.forEach((c) -> {
            if (c.getLoginUser().equals(username)) {
                c.sendData(msg);
            }
        });
    }

    public int getSize() {
        return clients.size();
    }

    public String getListUseOnline () {
        String result = "success;" + String.valueOf(clients.size()) + ";";
        for(int i = 0; i < clients.size(); i++) {
            result += clients.get(i).getLoginUser() + ";";
        }
        return result;
    }
}
