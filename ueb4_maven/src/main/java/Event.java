
public class Event implements Comparable<Event> {

    private long time;
    private EventType type;

    public Event(long time, EventType type) {
        super();
        this.time = time;
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    @Override
    public int compareTo(Event otherEvent) {

        if (this.getTime() == otherEvent.getTime()) {
            return 0;
        }
        if (this.getTime() > otherEvent.getTime()) {
            return 1;
        }

        return -1;
    }


}
