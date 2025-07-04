package hr.java.production.model;

import java.math.BigDecimal;

public class Service extends Entity implements Named {
    private String serviceName;
    private BigDecimal rate;
    private Integer amount;

    @Override
    public String getName() {
        return "";
    }
}
