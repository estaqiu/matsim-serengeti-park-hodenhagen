package org.matsim.analysis;


import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.GeoFileReader;
import org.locationtech.jts.geom.Geometry;

import java.util.stream.Collectors;

public class CountTripsBetweenAreas {

    public static void main(String[] args) {

        var shapeFileName = "C:\\Users\\Qiu\\Downloads\\Berlin_Bezirksgrenzen_-2292027283764261881\\Berlin_Bezirke.shp";
        var plansFileName = "C:\\Users\\Qiu\\Downloads\\berlin-v5.5.3-1pct.output_plans.xml.gz";
        //var tripsFileName = "C:\\Users\\Qiu\\Downloads\\berlin-v5.5.3-1pct.output_trips.csv.gz";
        var transformation = TransformationFactory.getCoordinateTransformation("EPSG:31468", "EPSG:3857");

        var features = GeoFileReader.getAllFeatures(shapeFileName);

        var geometries1 = features.stream()
                .filter(simpleFeature -> simpleFeature.getAttribute("Gemeinde_s").equals("001"))
                .map(simpleFeature -> (Geometry) simpleFeature.getDefaultGeometry())
                .collect(Collectors.toList());

        var mitteGeometry = geometries1.get(0);

        var geometries2 = features.stream()
                .filter(simpleFeature -> simpleFeature.getAttribute("Gemeinde_s").equals("002"))
                .map(simpleFeature -> (Geometry) simpleFeature.getDefaultGeometry())
                .collect(Collectors.toList());

        var FKGeometry = geometries2.get(0);

        var population = PopulationUtils.readPopulation(plansFileName);
        //var trips = TripStructureUtils.getTrips()

        var counter = 0;
        var trip_counter = 0;

        for (Person person : population.getPersons().values()) {

            var plan = person.getSelectedPlan();
            var activities = TripStructureUtils.getActivities(plan, TripStructureUtils.StageActivityHandling.ExcludeStageActivities);
            //var trips = TripStructureUtils.getTrips(plan, TripStructureUtils.StageActivityHandling.ExcludeStageActivities);
            var trips = TripStructureUtils.getTrips(plan.getPlanElements(),  activityType -> activityType.contains("interaction"));

            for (TripStructureUtils.Trip trip : trips) {

                var activity_orig = trip.getOriginActivity();
                var activity_dest = trip.getDestinationActivity();

                var originCoord = transformation.transform(activity_orig.getCoord());
                var destinationCoord = transformation.transform(activity_dest.getCoord());

                var originPoint = MGC.coord2Point(originCoord);
                var destinationPoint = MGC.coord2Point(destinationCoord);

                if (mitteGeometry.contains(originPoint) && FKGeometry.contains(destinationPoint)) {
                    trip_counter++;
                }

                if (mitteGeometry.contains(destinationPoint) && FKGeometry.contains(originPoint)) {
                    trip_counter++;
                }

            }


        }

        System.out.println("Number of trips between Mitte and FK are " + trip_counter);

    }

}
