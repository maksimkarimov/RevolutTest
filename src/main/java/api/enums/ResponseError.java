package api.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ResponseError {

    NOT_ENOUGH_MONEY(1, "Not enough money for transfer"),
    ACCOUNT_NOT_FOUND(2, "Account not found"),
    INSUFFICIENT_DATA(3, "Not all required parameters are filled in"),
    TRANSFER_ACCOUNTS_EQUALS(4, "You can't transfer money to the same account which you are charged"),
    TRANSFER_LESS_THEN_ZERO(5, "You can't transfer zero or less"),
    TRANSACTION_ROLLEDBACK(10, "Transaction rolled back");

    private int code;
    private String message;

    ResponseError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
