package models.headers;

public class HeaderServerMessage extends HeaderServer {
    private String type;
    private String sender;
    private String group;

    public HeaderServerMessage(String type, String sender, String group) {
        super();
        this.type = type;
        this.sender = sender;
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "HeaderServerMessage{" +
                "type='" + type + '\'' +
                ", sender='" + sender + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}