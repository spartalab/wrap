# wrap

**wrap** is a free and open-source implementation of the Four-Step Model of urban transportation demand prediction, written in Java. Originally created as an implementation of Dial's static traffic assignment Algorithm B for the Transportation Network Analysis (CE 392C) course at the University of Texas at Austin, the project has since been expanded to cover the full four-step modeling process: trip generation, trip distribution, mode choice, and traffic assignment.

This project continues to be under active development.

## Overview

The four-step model is the standard framework for regional travel demand forecasting used by metropolitan planning organizations worldwide. **wrap** implements each step:

1. **Trip Generation** — Estimates the number of trips produced by and attracted to each Travel Survey Zone (TSZ) based on demographic data (population, employment, etc.) and zone-specific generation rates.

2. **Trip Distribution** — Distributes generated trips between origin and destination zones using a doubly-constrained gravity model with configurable friction factor maps derived from network skims.

3. **Mode Choice** — Splits distributed trips among travel modes (SOV, HOV, truck classes) using trip-interchange or trip-end mode choice models.

4. **Traffic Assignment** — Loads origin-destination vehicle trips onto the transportation network, seeking User Equilibrium (UE) where no traveler can unilaterally improve their route. Implements Dial's Algorithm B for bush-based assignment with optional signalized intersection optimization.

## Project Structure

```
src/edu/utexas/wrap/
├── Project.java              # Top-level model: zones, markets, assigners, skims
├── TimePeriod.java            # Time-of-day periods (AM_PK, PM_PK, etc.)
├── wrap.java                  # JavaFX application entry point
│
├── generation/                # Step 1: Trip Generation
│   ├── TripGenerator.java         # Interface for generating demand from demographics
│   ├── ComponentTripGenerator.java # Multi-rate generator with demographic components
│   ├── GenerationRate.java         # Rate interface (per-zone or per-area-class)
│   ├── GeneralGenerationRate.java  # Uniform rate for all zones
│   └── AreaClassGenerationRate.java # Rate varies by land-use classification
│
├── balancing/                 # Production-attraction balancing
│   ├── TripBalancer.java              # Interface for PA balancing
│   ├── Attr2ProdProportionalBalancer.java  # Scale attractions to match productions
│   ├── Prod2AttrProportionalBalancer.java  # Scale productions to match attractions
│   └── Prod2AttrCopyBalancer.java          # Copy attractions to productions
│
├── distribution/              # Step 2: Trip Distribution
│   ├── TripDistributor.java          # Interface for distribution models
│   ├── GravityDistributor.java       # Doubly-constrained gravity model
│   ├── ModularGravityDistributor.java # Gravity model with external iteration control
│   ├── FrictionFactorMap.java        # Impedance function interface
│   ├── CostBasedFrictionFactorMap.java # Interpolated cost-based friction factors
│   ├── ImpedanceMatrix.java          # Pre-computed zone-to-zone impedance cache
│   ├── DistributionWeights.java      # Balancing weight interface (A/B factors)
│   └── BasicDistributionWeights.java # File-backed weight storage
│
├── modechoice/                # Step 3: Mode Choice
│   ├── Mode.java                     # Travel mode enumeration with occupancy rates
│   ├── TripInterchangeSplitter.java  # OD-pair-based mode choice interface
│   ├── TripEndSplitter.java          # Zone-level mode choice (pre-distribution)
│   └── FixedProportionSplitter.java  # Constant mode share implementation
│
├── assignment/                # Step 4: Traffic Assignment
│   ├── Assigner.java                 # Core assignment interface
│   ├── StaticAssigner.java           # Static (time-independent) assignment
│   ├── BasicStaticAssigner.java      # Main configurable assigner implementation
│   ├── AssignmentContainer.java      # Routing structure (bush, path, etc.)
│   ├── AssignmentOptimizer.java      # Route optimization interface
│   ├── AssignmentEvaluator.java      # Convergence evaluation interface
│   ├── GapEvaluator.java            # Relative gap convergence metric
│   ├── PressureFunction.java         # Signal timing optimization interface
│   ├── P0.java, WLYM.java           # Pressure function implementations
│   ├── Alexander.java/2/3           # HCM-based pressure functions
│   ├── Path.java                     # Sequential link list for path-based methods
│   ├── bush/                         # Bush-based assignment internals
│   │   ├── Bush.java                     # DAG rooted at origin zone
│   │   ├── BushBuilder.java              # Shortest-path tree construction
│   │   ├── BushMerge.java               # Multi-link merge node representation
│   │   ├── AlternateSegmentPair.java    # Flow-shifting path pairs
│   │   ├── PathCostCalculator.java      # Cached shortest/longest path costs
│   │   ├── BushReader/Writer/Forgetter  # Out-of-core bush I/O
│   │   ├── algoB/                       # Dial's Algorithm B implementation
│   │   │   ├── AlgorithmBOptimizer.java     # Per-bush optimization
│   │   │   ├── AlgorithmBEquilibrator.java  # Flow equilibration
│   │   │   └── AlgorithmBUpdater.java       # Bush topology updates
│   │   └── signalized/                  # Signal-aware assignment
│   │       └── SignalizedOptimizer.java     # Joint route/signal optimization
│   └── sensitivity/              # Sensitivity analysis (experimental)
│
├── marketsegmentation/        # Market and purpose definitions
│   ├── Market.java                # Traveler group with shared behavior model
│   ├── Purpose.java               # Full UTMS pipeline interface
│   ├── BasicPurpose.java          # Standard four-step purpose implementation
│   ├── SurrogatePurpose.java      # Pre-computed demand from external files
│   ├── IndustryClass.java         # Employment industry classification
│   ├── MarketRunner.java          # Threaded market execution for GUI
│   └── PurposeRunner.java         # Threaded purpose execution for GUI
│
├── demand/                    # Demand data structures
│   ├── DemandMap.java             # Zone-to-demand scalar mapping
│   ├── PAMap.java                 # Production-attraction totals per zone
│   ├── PAMatrix.java              # Zone-to-zone person-trip matrix
│   ├── AggregatePAMatrix.java     # All-mode PA matrix (post-distribution)
│   ├── ModalPAMatrix.java         # Mode-specific PA matrix (post-mode-choice)
│   ├── ODMatrix.java              # Vehicle-trip OD matrix
│   ├── ODProfile.java             # Multi-period OD demand with VOT
│   ├── *Provider.java             # Provider interfaces for each demand type
│   └── containers/               # Concrete demand container implementations
│
├── net/                       # Network representation
│   ├── Graph.java                 # Directed graph with forward/reverse stars
│   ├── Node.java                  # Network intersection/centroid
│   ├── Link.java                  # Abstract directed edge with delay function
│   ├── TolledBPRLink.java         # BPR volume-delay function link
│   ├── TolledEnhancedLink.java    # Conic volume-delay function link
│   ├── CentroidConnector.java     # Zone-to-network connector (uncongested)
│   ├── TravelSurveyZone.java      # Geographic demand zone
│   ├── NetworkSkim.java           # Zone-to-zone impedance matrix interface
│   ├── Demographic.java           # Zone demographic data interface
│   ├── AreaClass.java             # Land-use classification enum
│   ├── SignalizedNode.java        # Intersection with signal timing
│   ├── SignalGroup.java           # Signal phase with turning movements
│   ├── Ring.java                  # Signal ring with green share allocation
│   └── TurningMovement.java       # Intersection turning movement
│
├── util/                      # Utilities
│   ├── FibonacciHeap/Leaf.java    # Priority queue for Dijkstra's algorithm
│   ├── SPAlgorithms.java          # Shortest path algorithms
│   ├── TimeOfDaySplitter.java     # Daily-to-period demand factoring
│   ├── PassengerVehicleTripConverter.java  # Person-to-vehicle trip conversion
│   ├── *Collector.java            # Stream collectors for demand types
│   ├── io/                        # File I/O utilities
│   │   ├── GraphFactory.java          # Network file reader
│   │   ├── SkimFactory.java           # Skim computation and writing
│   │   ├── SkimLoader.java            # Skim CSV file loader
│   │   ├── ODProfileFactory.java      # OD profile/matrix file reader
│   │   ├── FrictionFactorFactory.java # Friction factor file reader
│   │   ├── ProductionAttractionFactory.java # PA data file reader
│   │   └── output/                # Output writers (CSV, binary, stream)
│   └── calc/                      # Assignment quality metrics
│       ├── BeckmannCalculator.java        # Beckmann objective function
│       ├── TotalSystemTravelTimeCalculator.java  # Total system travel time
│       └── *GapCalculator.java            # Various relative gap metrics
│
├── gui/                       # JavaFX GUI controllers
│   ├── ConfigController.java      # Main configuration window
│   ├── RunnerController.java      # Model execution controller
│   └── *Controller.java           # Dialog controllers for markets, purposes, etc.
│
└── res/                       # FXML layout files for the GUI
```

## Data Format

Projects are defined through a hierarchy of properties files:

- **`.wrp`** — Project file: defines zones, skims, markets, and assigners
- **`.wrm`** — Market file: defines demographics, friction factors, and purposes
- **`.wrpp`** — Purpose file: defines generation rates, distribution, mode choice, and time-of-day parameters
- **`.wrapr`** — Assigner file: defines network, container type, optimizer, evaluator, and convergence parameters

Sample data for the Dallas-Fort Worth and Huntsville, AL networks are included in the `data/` directory.

## Key Concepts

- **Travel Survey Zone (TSZ)**: A geographic area that serves as the basic spatial unit for demand modeling. Each zone has demographics, an area class, and connects to the network through centroid connectors.

- **Market**: A segment of the traveling population sharing common trip-making behavior (e.g., demographics, friction factors, mode preferences).

- **Purpose**: A reason for travel (e.g., home-based work, home-based shopping). Each purpose runs the full four-step pipeline independently.

- **Bush**: A directed acyclic subgraph of the network rooted at a single origin zone, used as the assignment container in Algorithm B. Each bush maintains shortest/longest path trees and flow decompositions.

- **Network Skim**: A zone-to-zone matrix of travel impedance (time, cost, or distance) computed from the assigned network. Skims feed back into trip distribution for iterative convergence.

## Building and Running

This project uses Java modules and requires:
- Java 11+
- JavaFX SDK

The main entry point is `edu.utexas.wrap.wrap`, which launches the JavaFX configuration GUI. Projects can also be loaded via command-line parameters.

## License

This software is licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for details.

## Creators
* William Alexander
* Rahul Patel
* Adam Nodjomian
* Prashanth Venkatraman

## Contributors
* Dr. Steve Boyles
* Rishabh Thakkar
* Kris Holder
* Karthik Velayutham

## Original goal statement (2017)
This project implements Algorithm B, as presented by Dial (2006) in order to create a solver for the Traffic Assignment Problem as defined by Beckmann et al. (1956). The aim for this project is to implement a time- and space-efficient framework of Algorithm B which can be studied, extended, and executed properly. We also aim to provide quality documentation and testing of our code, in order that this project will serve as a good exercise in practicing proper software development techniques. 

## \m/
             _______
           _|_______|_
          [ [_  _  _] ]
          !|| || || ||!
          !|| || || ||!
          !|| || || ||!
          [[]_[]_[]_[]]
          -------------
          |  |  .  |  |
          |  |..+..|  |
       _  |  / _?_ \  |  _
      [ ]_|_[]/   \[]_|_[ ]
      |     |(__/  )|     |
      |_____|_\___/_|_____|
     /| ..  |   ~   |  .. |\
    [___][ " ][ " ][ " ][___]
    |_______________________|
    |     |#|  |#|  |#|     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |     |#|  |#|  |#|     |
    |     | |  | |  | |     |
    |_____|#|__|#|__|#|_____|
    [_______________________]  
    |    HOOK 'EM HORNS!    |
