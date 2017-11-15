package be.howest.sooa.o5.domain;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Hayk
 */
public class Booking {

    private long id;
    private String name;
    private RoomType roomType;
    private Set<RoomOption> roomOptions = new TreeSet<>();

    public Booking() {
        this(null, null);
    }

    public Booking(long id, String name) {
        this(id, name, null);
    }

    public Booking(String name, RoomType roomType) {
        this(0L, name, roomType);
    }

    public Booking(long id, String name, RoomType roomType) {
        this.id = id;
        this.name = name;
        this.roomType = roomType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Set<RoomOption> getRoomOptions() {
        return Collections.unmodifiableSet(roomOptions);
    }

    public void setRoomOptions(Set<RoomOption> roomOptions) {
        this.roomOptions = roomOptions;
    }

    public void setRoomOptions(List<RoomOption> roomOptions) {
        this.roomOptions.clear();
        addAllRoomOptions(roomOptions);
    }

    public void addRoomOption(RoomOption roomOption) {
        if (roomOption != null) {
            roomOptions.add(roomOption);
        }
    }
    
    public int getOptionCount() {
        return (!hasOptions()) ? 0 : roomOptions.size();
    }

    public boolean hasOptions() {
        return roomOptions != null && !roomOptions.isEmpty();
    }

    public void addAllRoomOptions(List<RoomOption> roomOptions) {
        if (roomOptions != null) {
            this.roomOptions.addAll(roomOptions);
        }
    }

    public String getFormattedRoomPrice() {
        return (roomType == null)
                ? NumberFormat.getCurrencyInstance().format(getRoomPrice())
                : roomType.getFormattedPrice();
    }

    public BigDecimal getRoomPrice() {
        return (roomType == null) ? BigDecimal.ZERO : roomType.getPrice();
    }

    public String getFormattedPrice() {
        return NumberFormat.getCurrencyInstance().format(getPrice());
    }

    public BigDecimal getPrice() {
        BigDecimal price = BigDecimal.ZERO;
        if (roomType != null) {
            price = price.add(roomType.getPrice());
        }
        if (roomOptions != null && !roomOptions.isEmpty()) {
            for (RoomOption roomOption : roomOptions) {
                price = price.add(roomOption.getPrice());
            }
        }
        return price;
    }
}
