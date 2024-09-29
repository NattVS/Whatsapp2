package models.headers;

public abstract class HeaderUser extends Header {

    protected int dataLenght;
    protected String type;

    public HeaderUser() {
    }

    public int getDataLenght() {
        return dataLenght;
    }

    public void setDataLenght(int dataLenght) {
        this.dataLenght = dataLenght;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
