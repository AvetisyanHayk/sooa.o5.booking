package be.howest.sooa.o5.domain;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author Hayk
 */
public class RoomType extends Cost {

    private final long id;
    private final String description;

    public RoomType(long id, String description, BigDecimal price) {
        super(price);
        this.id = id;
        this.description = description;
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.description.toLowerCase(Locale.ENGLISH));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoomType other = (RoomType) obj;
        return description.equalsIgnoreCase(other.description);
    }
    
    @Override
    public String toString() {
        if (description != null) {
            if (description.length() == 1) {
                return description.toUpperCase(Locale.ENGLISH);
            }
            return description.toUpperCase(Locale.ENGLISH).charAt(0)
                    + description.toLowerCase(Locale.ENGLISH).substring(1);
        }
        return "";
    }
}
