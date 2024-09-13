package models;

public class Client {

    private String username;
    private String IP;
    private int PORT;

    public Client(String username, String IP, int PORT) {
        this.username = username;
        this.IP = IP;
        this.PORT = PORT;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String newName) {
        this.username = newName;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String iP) {
        IP = iP;
    }

    public int getPORT() {
        return PORT;
    }

    public void setPORT(int pORT) {
        PORT = pORT;
    }

}
