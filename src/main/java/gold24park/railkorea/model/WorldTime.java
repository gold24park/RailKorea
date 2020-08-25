package gold24park.railkorea.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WorldTime {

    private long time = 0L;
    private String clockTime = "";
    private String label = "낮";

    public WorldTime(long time) {
        this.time = time;
        init();
    }

    private void init() {
        int hour = (int) (time / 1000) + 6; // 06시가 0이므로
        hour = hour > 23 ? hour - 24 : hour;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("a hh:00");
        clockTime = sdf.format(calendar.getTime());

        if (time >= 13000 && time < 24000) {
            label = "밤";
        }
    }

    public long getTime() {
        return time;
    }

    public String getClockTime() {
        return clockTime;
    }

    public String getLabel() {
        return label;
    }
}
