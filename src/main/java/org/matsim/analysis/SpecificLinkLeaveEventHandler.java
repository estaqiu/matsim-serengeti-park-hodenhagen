package org.matsim.analysis;

import java.util.Map;
import java.util.HashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;


public class SpecificLinkLeaveEventHandler implements LinkLeaveEventHandler {

    private static final Id<Link> linkOfInterest = Id.createLinkId("7232382780000f");
    private final Map<String, Integer> volumes = new HashMap<>();

    //Map<String, Integer> getVolumes() {
    //    return volumes;
    //}

    @Override
    public void handleEvent(LinkLeaveEvent event) {

        if (event.getLinkId().equals(linkOfInterest)) {
            String key = getKey(event.getTime());
            // int currentCount = volumes.get(key);
            // int newCount = currentCount + 1;
            // volumes.put(key,newCount);

            //shorter version
            volumes.merge(key,1,Integer::sum);

            System.out.println("Time: " + event.getTime() + "s, Hour: " + key + ", Count: " + volumes.get(key));
        }

    }

    private String getKey(double time) {
        return Integer.toString((int) (time/3600));

    }

    public void exportToCSV(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Hour,Count"); // Header
            writer.newLine();

            for (Map.Entry<String, Integer> entry : volumes.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }

            System.out.println("CSV file saved: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
