package be.howest.sooa.o5.domain;

/**
 *
 * @author Hayk
 */
public class BookingValidation {
    
    private String message;
    
    public BookingValidation(Booking booking) {
        if (booking.getRoomType() == null) {
            message = "Room Type may not be empty.";
        }
        if (booking.getName() == null) {
            message += "Customer name may not be empty";
        }
    }

    public String getMessage() {
        return message;
    }
    
    public boolean isValid() {
        return message == null;
    }
}
