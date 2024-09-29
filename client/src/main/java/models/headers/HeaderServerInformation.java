package models.headers;

public class HeaderServerInformation extends HeaderServer {
    private Integer status;
    private String error;
    private String message;

    public HeaderServerInformation() {
        super();
    }

    public HeaderServerInformation(Integer status, String error, String message) {
        super();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
