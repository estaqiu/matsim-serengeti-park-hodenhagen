package org.matsim.analysis;

import org.matsim.core.events.EventsUtils;

public class ModeShareAnalysis {

    public static void main(String[] args) {

        var handler = new ModeShareHandler();
        var manager = EventsUtils.createEventsManager();
        manager.addHandler(handler);

        EventsUtils.readEvents(manager, "C:\\Users\\Qiu\\Downloads\\berlin-v5.5.3-1pct.output_events.xml.gz");
    }
}
