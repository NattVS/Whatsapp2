package models.headers;

public class HeaderUserMessage {
    private String type;
    private String method;
    private String recipient;

    public HeaderUserMessage(String type, String method, String recipient) {
        this.type = type;
        this.method = method;
        this.recipient = recipient;
    }

    // Getters y setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }


    @Override
    public String toString() {
        return "HeaderUserMessage{" +
                "type='" + type + '\'' +
                ", method='" + method + '\'' +
                ", recipient='" + recipient + '\'' +
                '}';
    }
}
