package org.matsim.analysis;

import java.net.Inet4Address;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.GeoFileReader;
import org.opengis.feature.simple.SimpleFeature;

public class ModeShareHandler implements PersonDepartureEventHandler, PersonArrivalEventHandler {

    private final Map<Id<Person>, String> personToMode = new HashMap<>();
    private final Map<String, Integer> modeCounts = new HashMap<>();
    private final Geometry mitteGeometry;
    private final Map<Id<Person>, Boolean> personStartedInMitte = new HashMap<>();
    private final Network network;
    private final CoordinateTransformation transformation;



    public ModeShareHandler(Geometry mitteGeometry) {
        this.mitteGeometry = loadMitteGeometry("C:\\Users\\Qiu\\Downloads\\Berlin_Bezirksgrenzen_-2292027283764261881\\Berlin_Bezirke.shp");
        this.network = network;
        this.transformation = TransformationFactory.getCoordinateTransformation("EPSG:31468", "EPSG:3857");
    }

    //var shapeFileName = "C:\\Users\\Qiu\\Downloads\\Berlin_Bezirksgrenzen_-2292027283764261881\\Berlin_Bezirke.shp";

    @Override
    public void handleEvent(PersonDepartureEvent personDepartureEvent) {
        Id<Person> personId = personDepartureEvent.getPersonId();
        String mode = personDepartureEvent.getLegMode();
        //var departureTime = personDepartureEvent.getTime();
        //var personId = personDepartureEvent.getPersonId();
        //personToDepartureTime.put(personId, departureTime);

        boolean isInMitte = isInMitte(personDepartureEvent.getLinkId());
        personStartedInMitte.put(personId,isInMitte);

        if (isInMitte) {
            personToMode.put(personId,mode);
        }

    }

    @Override
    public void handleEvent(PersonArrivalEvent personArrivalEvent) {
        Id<Person> personId = personArrivalEvent.getPersonId();

        boolean arrivedInMitte = isInMitte(personArrivalEvent.getLinkId());

        if (personStartedInMitte.getOrDefault(personId, false) || arrivedInMitte) {
            String mode = personToMode.get(personId);
            modeCounts.put(mode,modeCounts.getOrDefault(mode,0) + 1);
        }

        personToMode.remove(personId);
        personStartedInMitte.remove(personId);
    }

    @Override
    public void reset(int iteration) {
        personToMode.clear();
        modeCounts.clear();
        personStartedInMitte.clear();
    }

    public void printModeShare() {
        int totalTrips = modeCounts.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("Mode Share for Mitte:");
        for (var entry: modeCounts.entrySet()) {
            String mode = entry.getKey();
            int count = entry.getValue();
            double percentage = (count * 100.0) / totalTrips;
            System.out.println(String.format(mode + ": " + String.format("%.2f", percentage) + "% (" + count + "trips)"));
        }
    }

    private Geometry loadMitteGeometry(String shapeFilePath) {

        List<SimpleFeature> features = GeoFileReader.getAllFeatures(shapeFilePath);
        return features.stream()
            .filter(feature -> feature.getAttribute("Gemeinde_s").equals("001"))
            .map(feature -> (Geometry) feature.getDefaultGeometry())
            .findFirst()
            .orElse(null);

    }

    private boolean isInMitte(Id<Link> linkId) {
        Link link = network.getLinks().get(linkId);
        if (link == null) return false;

        var coord = link.getCoord();
        var transformedCoord = transformation.transform(coord);
        Point point = MGC.coord2Point(transformedCoord);

        return mitteGeometry.contains(point);
    }

}
