package models.headers;

public class HeaderUserMessage extends HeaderUser {
    private String method;
    private String recipient;
    private String message;

    public HeaderUserMessage(String type, String method, String recipient,int dataLenght) {
        super();
        super.type = type;
        this.method = method;
        this.recipient = recipient;
        super.dataLenght = dataLenght;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
