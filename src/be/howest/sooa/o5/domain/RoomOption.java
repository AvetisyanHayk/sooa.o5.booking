package be.howest.sooa.o5.domain;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author Hayk
 */
public class RoomOption extends Cost implements Comparable<RoomOption> {
    
    private final long id;
    private final String description;
    
    public RoomOption(long id, String description, BigDecimal price) {
        super(price);
        this.id = id;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
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
        final RoomOption other = (RoomOption) obj;
        return description.equalsIgnoreCase(other.description);
    }
    
    @Override
    public String toString() {
        return description + " (" + getFormattedPrice() + ")";
    }

    @Override
    public int compareTo(RoomOption other) {
        return description.compareTo(other.description);
    }
}
