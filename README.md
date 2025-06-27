# Droning On - Drone-Based Parcel Delivery Simulation

## Overview

**Droning On** is a drone-based parcel delivery simulation system developed by Delivering Solutions Inc. (DS). It models the behaviour of a fleet of delivery drones operating from a dispatch centre to suburban homes, simulating parcel deliveries in compliance with Australian Government regulations.

The system provides a realistic framework for testing drone delivery logistics, including drone movement, parcel handling, and access management within a suburban road layout.

---

## Project Description

The Droning On system simulates a dispatch centre controlling multiple drones delivering parcels throughout a mapped suburb. Each drone follows a strict counterclockwise route along roads mapped as linked structures, delivering parcels by navigating to the specified address and returning to the dispatch centre.

Key features of the simulation include:

- **Drone Movement & Navigation:** Drones fly exclusively over mapped roads for safety, moving step-by-step along a linked structure representing the suburb’s layout. Only one drone can occupy a given location at a time, but simultaneous entry and exit are allowed.

- **Parcel Delivery Workflow:** Drones cycle through defined states: waiting for dispatch, traveling to suburb entry, moving to delivery address, delivering parcel, exiting suburb, returning to dispatch, and recharging.

- **Parcel Types:** The system distinguishes fragile and heavy parcels, each affecting drone speed and delivery priority.

- **Contention Management:** The dispatch centre manages access requests for locations, resolving contention based on drone priority and movement order to optimize flow and minimize delays.

- **Enhanced Fragile Parcel Handling:** Drones delivering fragile parcels travel at half speed (simulated by moving every second time-step) during outbound transit and delivery, ensuring safer handling.

- **Priority Access for Drones:** The system grants location access based on priority categories:
  1. Drones delivering fragile parcels
  2. Drones delivering heavy parcels
  3. Drones nearest the requested location (south of it)
  4. Other drones

  This ensures drones with higher delivery urgency or limited range get preferential treatment.

- **Improved Contention Resolution:** Access requests are cached and processed in reverse trip order (clockwise) each time-step, allowing drones to move into locations being vacated in the same cycle, reducing wait times.

- **Simulation Time-Stepping:** The entire system operates on discrete time-steps, with drones and dispatch centre “ticked” each step to perform actions sequentially.

- **Logging and Statistics:** The system logs detailed activity data and generates statistics such as average delivery times, supporting performance analysis.

---

## Suburb Layout and Drone Behaviour

The simulation models a suburb grid of streets and houses, with drones flying counterclockwise along roads, entering the suburb from the dispatch centre, delivering parcels by reaching the specific street and house number, then exiting and returning.

Drones handle heavy and fragile parcels differently to reflect real-world constraints, influencing their transit speed and delivery process.

---

## System Design Highlights

- **State Machine Driven Drones:** Each drone's behaviour is modelled as a state machine, progressing through the delivery cycle logically and safely.

- **Linked Structure Road Map:** The suburb’s roads are modelled as linked locations supporting safe navigation and concurrency controls.

- **Dynamic Priority Management:** The dispatch centre dynamically prioritizes drones based on parcel type and position to optimize delivery efficiency and safety.

- **Extensibility:** The design allows future updates to prioritization logic and parcel handling without major system changes.

---
## Running the Project

### Requirements

- Java 11 or higher
- Gradle (provided with project)
- IntelliJ IDEA recommended

### Build and Run

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd drone
   '''
  2. Build the project using Gradle:
  ```bash
  ./gradlew build
  ```
 3.  Run the game:
```bash
./gradlew run
```
## Project Structure
- app/src/main/java: Source code including game logic and GUI.
- app/src/main/resources: Properties files and image assets.
- app/src/test: Automated tests and test resources.
- Driver.java: Entry point of the application.

## Contributors
- Kerui Huang
- Ariff Fikri Bin Mohd Farris
- Himank Bassi
- University of Melbourne SWEN30006 Teaching Team (Question Provider)
---
## Contributors
- Kerui Huang
- Ariff Fikri Bin Mohd Farris
- Himank Bassi
- University of Melbourne SWEN30006 Teaching Team (Question Provider)
---
