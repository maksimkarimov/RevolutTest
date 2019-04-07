package api.response;

import api.enums.ResponseError;

public class TransferResponse {
    private final boolean done;
    private final ResponseError error;

    public TransferResponse(final boolean done, final ResponseError error) {
        this.done = done;
        this.error = error;
    }

    public boolean isDone() {
        return done;
    }

    public ResponseError getError() {
        return error;
    }
}
