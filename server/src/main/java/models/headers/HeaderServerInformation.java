package models.headers;

public class HeaderServerInformation extends HeaderServer {
    private Integer status;
    private String error;

    public HeaderServerInformation(Integer status, String error) {
        super();
        this.status = status;
        this.error = error;
    }

    // Getters y setters
    public Integer getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "HeaderServerInformation{" +
                "status='" + status + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
