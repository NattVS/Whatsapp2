package models.headers;

public class CallHeader extends HeaderUser {
    private int UDP_PORT;
    private String method;
    private String caller;
    private String personBeingCalled;

    public CallHeader(String method, int UDP_PORT, String caller, String personBeingCalled) {
        super();
        this.UDP_PORT = UDP_PORT;
        super.type = "CALL_HEADER";
        this.method = method;
        this.caller = caller;
        this.personBeingCalled = personBeingCalled;
    }


    public CallHeader(String method, int UDP_PORT, String caller) {
        super();
        this.UDP_PORT = UDP_PORT;
        super.type = "CALL_HEADER";
        this.method = method;
        this.caller = caller;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getUDP_PORT() {
        return UDP_PORT;
    }

    public void setUDP_PORT(int uDP_PORT) {
        UDP_PORT = uDP_PORT;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getPersonBeingCalled() {
        return personBeingCalled;
    }

    public void setPersonBeingCalled(String personBeingCalled) {
        this.personBeingCalled = personBeingCalled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
