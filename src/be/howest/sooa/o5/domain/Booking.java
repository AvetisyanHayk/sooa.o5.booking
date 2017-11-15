package be.howest.sooa.o5.domain;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Hayk
 */
public class Booking {

    private final long id;
    String name;
    private final RoomType roomType;
    private final Set<RoomOption> roomOptions = new TreeSet<>();

    public Booking(long id, String name, RoomType roomType) {
        this.id = id;
        this.name = name;
        this.roomType = roomType;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public Set<RoomOption> getRoomOptions() {
        return Collections.unmodifiableSet(roomOptions);
    }
    
    public void addRoomOption(RoomOption roomOption) {
        if (roomOption != null) {
            roomOptions.add(roomOption);
        }
    }
}
