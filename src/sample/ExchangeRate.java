package sample;

import java.util.Date;
import java.util.Map;

/**
 * Created by Wienio on 2017-05-25.
 */
public class ExchangeRate {
    private String base;
    private Date date;
    private Map<String, Double> rates;

    public String getBase() {
        return base;
    }

    public Date getDate() {
        return date;
    }

    public Map<String, Double> getRates() {
        return rates;
    }
}
