package org.matsim.analysis;

import org.matsim.core.events.EventsUtils;

public class SpecificLinkAnalysis {

    public static void main(String[] args) {

        var handler = new SpecificLinkLeaveEventHandler();
        var manager = EventsUtils.createEventsManager();
        manager.addHandler(handler);

        EventsUtils.readEvents(manager, "C:\\Users\\Qiu\\IdeaProjects\\matsim-serengeti-park-hodenhagen\\scenarios\\serengeti-park-v1.0\\output\\output-serengeti-park-v1.0-run1\\serengeti-park-v1.0-run1.output_events.xml.gz");
        handler.exportToCSV("C:\\Users\\Qiu\\Documents\\volumes_output.csv");
    }

}

