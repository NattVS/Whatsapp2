package models.headers;

public class HeaderServerMessage extends HeaderServer {
    private String type;
    private String sender;
    private String group;
    private Integer DataLenght;

    public HeaderServerMessage(){
        super();
    }

    public HeaderServerMessage(String type, String sender, String group,int dataLenght) {
        super();
        this.type = type;
        this.sender = sender;
        this.group = group;
        this.DataLenght = dataLenght;
    }

    public Integer getDataLenght() {
        return DataLenght;
    }

    public void setDataLenght(Integer dataLenght) {
        DataLenght = dataLenght;
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