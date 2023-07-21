package customer.capex.enums;

public enum StatusEnum {
    
    NONE(0, "None"),
    PENDING(1, "Pending"),
    ON_HOLD(2, "On Hold"),
    REJECTED(3, "Rejected"),
    APPROVED(4, "Approved");

    private int code;
    private String status;

    StatusEnum(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public int code() {
        return code;
    }

    public String status() {
        return status;
    }

    public static StatusEnum getEnum(int code) {
        for(StatusEnum e: StatusEnum.values()) {
            if(e.code() == code) {
                return e;
            }
        }
        return StatusEnum.NONE;
    }

    public static StatusEnum getEnum(String status) {
        for(StatusEnum e: StatusEnum.values()) {
            if(e.status().equalsIgnoreCase(status)) {
                return e;
            }
        }
        return StatusEnum.NONE;
    }

}
