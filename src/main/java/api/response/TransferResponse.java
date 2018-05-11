package api.response;

import api.enums.ResponseError;

public class TransferResponse {
    private boolean done;
    private ResponseError error;

    public TransferResponse(boolean done, ResponseError error) {
        this.done = done;
        this.error = error;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public ResponseError getError() {
        return error;
    }

    public void setError(ResponseError error) {
        this.error = error;
    }
}
