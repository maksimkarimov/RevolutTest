package api.response;

public class TransferResponse {
    private boolean done;
    private String errorMessage;

    public TransferResponse(boolean done, String errorMessage) {
        this.done = done;
        this.errorMessage = errorMessage;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
