package drone;
import java.util.*;

class DispatchCentre implements Simulation.Tickable {
    final int numdrones;
    public final int timeToSuburb;
    public final int weightThreshold;
    final Queue<Parcel> waitingForDelivery;
    final Set<Drone> drones;
    final Map<Location,List<Drone>> requests = new LinkedHashMap<>();

    public boolean someItems() {
        return !waitingForDelivery.isEmpty();
    }

    DispatchCentre(Suburb suburb, int timeToSuburb, int numdrones, int weightThreshold) {
        this.timeToSuburb = timeToSuburb; // Distance away suburb is from dispatch centre
        this.weightThreshold = weightThreshold;
        this.numdrones = numdrones;
        waitingForDelivery = new LinkedList<>();
        drones = new HashSet<>();
        for (int i = 0; i < numdrones; i++) drones.add(new Drone(this, suburb));
        Simulation.register(this);

        // Add "backwards paths" ordering of locations to request map
        for (Location l = suburb.backAvenue; l != null; l = l.getRoad(Suburb.Direction.SOUTH)) {
            requests.put(l, new ArrayList<>());
        }
        for (int i = 0; i < suburb.NUMSTREETS; i++) {
            Location e = suburb.streets[i];
            for (int j = 0; j < suburb.NUMHOUSES; j++) e = e.getRoad(Suburb.Direction.EAST);
            for (int j = 0; j < suburb.NUMHOUSES; j++) {
                e = e.getRoad(Suburb.Direction.WEST);
                requests.put(e, new ArrayList<>());
            }
        }
        Location e = suburb.outAvenue;
        while (e.getRoad(Suburb.Direction.SOUTH) != null) e = e.getRoad(Suburb.Direction.SOUTH);
        for (Location l = e; l != null; l = l.getRoad(Suburb.Direction.NORTH)) {
            requests.put(l, new ArrayList<>());
        }
        // System.out.println(requests.keySet());
    }

    void arrive(List<Parcel> parcels) {
        for (Parcel parcel : parcels) {
            waitingForDelivery.add(parcel);
            String s = "Arrived: " + parcel;
            System.out.println(s);
            Simulation.logger.logEvent("%5d: %s\n", Simulation.now(), s);        }
    }

    public boolean isHeavy(Parcel parcel) {
        return parcel.myWeight() > weightThreshold;
    }

    public void requestDispatch(Drone drone) {
        if (!waitingForDelivery.isEmpty()) {
            drone.dispatch(waitingForDelivery.remove());
            drones.remove(drone);
        } else {
            drones.add(drone);  // Track waiting drones
        }
    }

    public boolean allDronesBack() { return drones.size() == numdrones; }

    //  Simple access accepting any request for a free location
    void requestAccess(Drone drone, Location location) {

        requests.get(location).add(drone); //Adds drone to the location in the map as a cached request
        //if (location.drone == null) drone.grantAccess(location); // First in first served, Original Code
    }

    // Task 3 priority update
    private Drone getHighestPriorityDrone(List<Drone> candidates, Location target) {
        Drone best = null;
        int bestPrio = Integer.MAX_VALUE;

        for (Drone d : candidates) {
            int prio;
            // 1. Fragile?
            if (d.isHadFragileParcel()) {
                prio = 1;
            }
            // 2. Heavy? (call instance method)
            else if (d.isHadHeavyParcel()) {
                prio = 2;
            }
            // 3. South-of target?
            else if (d.getLocation() != null
                    && d.getLocation().getRoad(Suburb.Direction.SOUTH) == target) {
                prio = 3;
            }
            // 4. Everyone else
            else {
                prio = 4;
            }
            if (best == null || prio < bestPrio) {
                best = d;
                bestPrio = prio;
            }
        }

        return best;
    }

    public void tick() {
        // Ticked after all drones
        // Iterates through the location to find which drones requests the location
        for (Location location: requests.keySet()) {
            List<Drone> drones = requests.get(location);
            //Checks if any drones request the location
            if (!drones.isEmpty()) {
                //Gives access to the first drone
//                Drone drone = drones.getFirst(); //Use task 2
                Drone drone = getHighestPriorityDrone(drones, location);//Task 3

                if (location.drone == null) drone.grantAccess(location);
            }
        }
        // Clear all requests after handling
        for (List<Drone> drones : requests.values()) {
            drones.clear();
        }
    }
}


