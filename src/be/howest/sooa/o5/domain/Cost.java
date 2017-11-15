package be.howest.sooa.o5.domain;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 *
 * @author Hayk
 */
public abstract class Cost {

    protected BigDecimal price;
    
    public Cost(BigDecimal price) {
        this.price = price;
    }

    public String getFormattedPrice() {
        return NumberFormat.getCurrencyInstance().format(price);
    }

    public BigDecimal getPrice() {
        return price;
    }
}
