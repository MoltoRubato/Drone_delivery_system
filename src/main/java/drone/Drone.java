package drone;

class Drone implements Simulation.Tickable {
    static int count = 1;
    final String id;
    final DispatchCentre dispatchCentre;
    final Suburb suburb;
    Location location;
    Parcel parcel = null;
    enum State {
        WaitingForDispatch,TransitToSuburb, TransitToDelivery, Delivering, TransitToExit, TransitToCentre, Recharge}
    State state = State.WaitingForDispatch;
    int transDuration; // Elapsed time before next transition occurs

    private boolean hadFragileParcel = false;
    private boolean hadHeavyParcel   = false;

    private boolean skipTick = true; // Alternate ticking for fragile parcels, starting with skip

    Drone(DispatchCentre dispatchCentre, Suburb suburb) {
        this.dispatchCentre = dispatchCentre;
        this.suburb = suburb;
        this.id = "D" + count++;
        location = null;
        Simulation.register(this);
    }


    private boolean shouldSkipFragileTick() {
        if (parcel != null && parcel.isFragile() && skipTick) { // skip this tick
            skipTick = false;
            return true;
        }
        skipTick = (parcel != null && parcel.isFragile()); // skip next tick
        return false;
    }

    public void tick() {
        Location nextLocation;
        switch (state) {
            case WaitingForDispatch:
                dispatchCentre.requestDispatch(this);
                break;
            case TransitToSuburb:
                if (shouldSkipFragileTick()) break;

                if (transDuration > 0) {
                    transDuration--;
                } else {
                    dispatchCentre.requestAccess(this, suburb.getEntry());
                }
                break;

            case TransitToDelivery:
                if (shouldSkipFragileTick()) break;

                // Counterclockwise: south to parcel street then east to parcel house
                nextLocation = location.getRoad(Suburb.Direction.EAST);
                // East - looking for delivery location - must be a street
                if (nextLocation == null || !((Suburb.StreetId) nextLocation.id).sameStreet(parcel.myStreet())) {
                    // not currently next to parcel address street
                    nextLocation = location.getRoad(Suburb.Direction.SOUTH);
                }
                assert nextLocation != null :
                        "Reached " + location.id + " without finding street:" + parcel.myStreet();
                dispatchCentre.requestAccess(this, nextLocation);
                break;

            case Delivering:
                if (shouldSkipFragileTick()) break;

                if (parcel == null) {
                    location.endDelivery();
                    state = State.TransitToExit;
                } else {
                    location.startDelivery();
                    Simulation.deliver(parcel);
                    parcel = null;
                }
                break;

            case TransitToExit:
                // Counterclockwise: east to Back Ave then north to exit
                nextLocation = location.getRoad(Suburb.Direction.EAST);
                if (nextLocation == null) {
                    nextLocation = location.getRoad(Suburb.Direction.NORTH);
                }
                assert nextLocation != null : "Can't go east or north from " + location.id;
                dispatchCentre.requestAccess(this, nextLocation);
                break;
            case TransitToCentre:
                if (location != null) location.departDrone();
                if (transDuration > 0) {
                    transDuration--;
                } else {
                    state = State.Recharge;
                }
                break;
            case Recharge:
                state = State.WaitingForDispatch;
                break;
        }
    }

    void dispatch(Parcel parcel) {
        assert state == State.WaitingForDispatch : id + " dispatched when not waiting for dispatch";
        this.parcel = parcel;
        this.hadFragileParcel = parcel.isFragile();
        this.hadHeavyParcel   = !parcel.isFragile() && dispatchCentre.isHeavy(parcel);

        transDuration = dispatchCentre.timeToSuburb;
        this.skipTick = true; // reset for new dispatch

        state = State.TransitToSuburb;
    }

    void grantAccess(Location location) {
        switch (state) {
            case TransitToSuburb:
                if (this.location != null) this.location.departDrone();
                location.arriveDrone(this);
                state = State.TransitToDelivery;
                break;
            case TransitToDelivery:
                if (this.location != null) this.location.departDrone();
                location.arriveDrone(this);
                if (location.id.deliveryAddress(parcel.myStreet(), parcel.myHouse())) {
                    state = State.Delivering;
                }
                break;
            case TransitToExit:
                if (this.location != null) this.location.departDrone();
                location.arriveDrone(this);
                if (location == suburb.getExit()) {
                    state = State.TransitToCentre;
                    transDuration = dispatchCentre.timeToSuburb;
                }
                break;
            default:
                assert false : id + " access granted to " + location.id + " in non-requesting state " + state;
        }
    }

    public String toString() { return id; }

    void setLocation(Location location) { this.location = location; }

    Location getLocation() { return location; }

    public boolean isHadHeavyParcel() { return hadHeavyParcel; }

    public boolean isHadFragileParcel() {
        return hadFragileParcel;
    }
}