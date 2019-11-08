package edu.utexas.wrap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import edu.utexas.wrap.balancing.Prod2AttrProportionalBalancer;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.Combiner;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughPAMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.IndustryClass;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.*;
import edu.utexas.wrap.util.io.*;

public class wrapHNW {

    public static void main(String[] args) {
        try{
            //Market segmentation
            Collection<MarketSegment> segmentsAfterGeneration = IntStream.range(1,5).parallel().mapToObj(ig -> new IncomeGroupSegment(ig)).collect(Collectors.toSet());

            long ms;
            long nms;
            Graph graph = readNetworkData(args);

            //TODO need to add command line argument for the prodRates

            //Perform trip generation
            Map<MarketSegment, PAMap> hbMaps = NCTCOGTripGen.tripGeneratorHNW(graph);

            //Perform trip balancing

            balance(graph, hbMaps);


            //Peak/off-peak splitting
            Map<MarketSegment,Map<TimePeriod,PAMap>> timeMaps = pkOpSplitting(hbMaps,segmentsAfterGeneration);

            //Perform trip distribution
            Map<MarketSegment, Map<TimePeriod, AggregatePAMatrix>> aggMtxs = tripDistribution(segmentsAfterGeneration, graph, timeMaps);


            System.out.print("Performing matrix aggregation... ");
            //FIXME study how this should actually behave - PK and OP splitting doesn't just get combined back together
            Map<MarketSegment,AggregatePAMatrix> aggCombinedMtxs = aggMtxs.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry -> Combiner.combineAggregateMatrices(graph, entry.getValue().values())));


            System.out.print("Performing mode choice splitting... ");
            //Perform mode choice splitting
            ms = System.currentTimeMillis();
            Map<MarketSegment, Collection<ModalPAMatrix>> modalMtxs = modeChoice(segmentsAfterGeneration, aggCombinedMtxs);
            nms = System.currentTimeMillis();
            System.out.println(""+(nms-ms)/1000.0+" s");


            System.out.print("Performing PA-to-OD matrix conversion... ");
            //PA to OD splitting by time of day
            ms = System.currentTimeMillis();
            Map<TimePeriod, Map<Mode, ODMatrix>> ods = paToODConversion(modalMtxs);
            nms = System.currentTimeMillis();
            System.out.println(""+(nms-ms)/1000.0+" s");


            writeODs(ods);

            //Combine off-peak matrices and output to file
//			Path outputFile = null;
//			ODMatrix offPeak = Combiner.combineODMatrices(
//					ods.entrySet().parallelStream()
//					.filter(entry ->
//						!entry.getKey().equals(TimePeriod.MORN_PK) &&
//						!entry.getKey().equals(TimePeriod.AFTERNOON_PK))
//					.map(entry -> entry.getValue()));
//			offPeak.write(outputFile);

            System.out.println("Completed successfully");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void writeODs(Map<TimePeriod, Map<Mode, ODMatrix>> ods) {
        //TODO determine output files
        Map<TimePeriod,Map<Mode,Path>> outputODPaths = new HashMap<TimePeriod,Map<Mode,Path>>();
//		outputODPaths.put(TimePeriod.AM_PK, Paths.get("morningPeak.csv"));
//		outputODPaths.put(TimePeriod.PM_PK, Paths.get("eveningPeak.csv"));

        System.out.print("Writing OD matrices... ");
        //Write to file AM and PM peak OD matrices
        long ms = System.currentTimeMillis();

        ods.entrySet().parallelStream()
                .filter(todEntry ->
//				todEntry.getKey().equals(TimePeriod.AM_PK) ||
                                todEntry.getKey().equals(TimePeriod.PM_PK)
                )
                .forEach(todEntry -> todEntry.getValue().entrySet().parallelStream()
                        .forEach(modeEntry ->
                                modeEntry.getValue().write(outputODPaths.get(todEntry.getKey()).get(modeEntry.getKey()))
                        )
                );

        long nms = System.currentTimeMillis();
        System.out.println(""+(nms-ms)/1000.0+" s");
    }

    private static Map<MarketSegment, FrictionFactorMap> readFrictionFactorMaps(
            Collection<MarketSegment> afterPASegments, Graph graph) throws IOException {
        float[][] skim = SkimFactory.readSkimFile(new File("../../nctcogFiles/PKNOHOV.csv"), false, graph);
        Map<MarketSegment, FrictionFactorMap> ffmaps = new ConcurrentHashMap<MarketSegment, FrictionFactorMap>();
        String[] ff_files = {
                "../../nctcogFiles/FFactorHBW_INC1 PK.csv",
                "../../nctcogFiles/FFactorHBW_INC2 PK.csv",
                "../../nctcogFiles/FFactorHBW_INC3 PK.csv"};

        afterPASegments.parallelStream().forEach(seg -> { // This could be made a lot cleaner...
                    int idx = 0;
                    while(idx < ff_files.length && !ff_files[idx].contains(((IncomeGroupSegment) seg).getIncomeGroup() + "")){
                        idx++;
                    }
                    try {
                        ffmaps.put(seg, FrictionFactorFactory.readFactorFile(new File(ff_files[idx]), true, skim));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        return ffmaps;
    }

    private static void balance(Graph g, Map<MarketSegment, PAMap> timeMaps) {
        System.out.print("Performing trip balancing... ");
        long ms = System.currentTimeMillis();

        Prod2AttrProportionalBalancer balancer = new Prod2AttrProportionalBalancer(null);
        timeMaps.values().parallelStream().forEach(map -> balancer.balance(map));

        long nms = System.currentTimeMillis();
        System.out.println(""+(nms-ms)/1000.0+" s");
    }

    private static Map<MarketSegment, Map<TimePeriod, PAMap>> pkOpSplitting(Map<MarketSegment, PAMap> maps,
                                                                            Collection<MarketSegment> segments) throws IOException {
        System.out.print("Performing peak-offpeak splitting... ");
        Map<MarketSegment,Double> pkRates = PeakFactory.readPkOPkSplitRates(new File("../../nctcogFiles/pkOffPkSplits.csv"), true, segments); // pkOffPkSplits.csv
        return maps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey,
                entry -> {
                    double pkRate = pkRates.get(entry.getKey());
                    PAMap pkMap = new FixedMultiplierPassthroughPAMap(entry.getValue(),pkRate);
                    PAMap opMap = new FixedMultiplierPassthroughPAMap(entry.getValue(),1.0-pkRate);
                    Map<TimePeriod,PAMap> ret = new HashMap<TimePeriod,PAMap>(3,1.0f);
                    ret.put(TimePeriod.AM_PK, pkMap);
                    ret.put(TimePeriod.EARLY_OP, opMap);
                    return ret;
                }));
    }
    private static Map<MarketSegment, Map<TimePeriod,AggregatePAMatrix>> tripDistribution(Collection<MarketSegment> segments, Graph g,
                                                                                          Map<MarketSegment, Map<TimePeriod, PAMap>> timeMaps) throws IOException {

        System.out.print("Reading friction factor maps... ");
        //Create FF Maps for each segment
        long ms = System.currentTimeMillis();
        Map<MarketSegment, FrictionFactorMap> ffm = readFrictionFactorMaps(segments, g);
        long nms = System.currentTimeMillis();

        System.out.println(""+(nms-ms)/1000.0+" s");
        System.out.print("Performing trip distribution... ");

        return timeMaps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry ->
                entry.getValue().entrySet().parallelStream()
                        .collect(Collectors.toMap(Entry::getKey, inner -> {
                            TripDistributor distributor = new GravityDistributor(g,ffm.get(entry.getKey()));
                            return distributor.distribute(inner.getValue());
                        }))
        ));
    }

    private static Map<MarketSegment, Collection<ModalPAMatrix>> modeChoice(Collection<MarketSegment> segments,
                                                                            Map<MarketSegment, AggregatePAMatrix> aggMtxs) throws IOException {
        Map<MarketSegment,Map<Mode,Double>> modeShares = ModeFactory.readModeShares(new File("../../nctcogFiles/modeChoiceSplits.csv"), segments); // ModeChoiceSplits.csv
        TripInterchangeSplitter mc = new FixedProportionSplitter(modeShares);
        return aggMtxs.entrySet().parallelStream()
                .collect(Collectors.toMap(Entry::getKey,
                        entry -> mc.split(entry.getValue(),entry.getKey())
                                .collect(Collectors.toSet())));
    }

    private static Map<TimePeriod, Map<Mode,ODMatrix>> paToODConversion(Map<MarketSegment, Collection<ModalPAMatrix>> modalMtxs) throws IOException {



        System.out.print("Reading modal shares and occupancy rates... ");
        //Mode choice inputs
        long ms = System.currentTimeMillis();
        Map<Mode,Double> occupancyRates = ModeFactory.readOccRates(new File("../../nctcogFiles/modalOccRates.csv"), true); // modalOccRates.csv
        long nms = System.currentTimeMillis();
        System.out.println(""+(nms-ms)/1000.0+" s");


        System.out.print("Reading time-of-day rates... ");
        //TOD splitting inputs
        ms = System.currentTimeMillis();
        Map<MarketSegment, Map<TimePeriod,Double>> depRates = TimePeriodRatesFactory.readDepartureFile(new File("../../nctcogFiles/TODfactors.csv"), modalMtxs.keySet()), //TODFactors.csv
                arrRates = TimePeriodRatesFactory.readArrivalFile(new File("../../nctcogFiles/TODfactors.csv"), modalMtxs.keySet()); //TODFactors.csv
        nms = System.currentTimeMillis();
        System.out.println(""+(nms-ms)/1000.0+" s");


        return Stream.of(TimePeriod.values()).parallel()
                .collect(Collectors.toMap(Function.identity(), tp -> {

                    //Convert using TOD splitting
                    Stream<ODMatrix> tpODs = modalMtxs.entrySet().parallelStream()
                            .flatMap(entry -> {
                                DepartureArrivalConverter converter = new DepartureArrivalConverter(depRates.get(entry.getKey()).get(tp),arrRates.get(entry.getKey()).get(tp));
                                return entry.getValue().parallelStream().map(mtx -> converter.convert(mtx, occupancyRates.get(mtx.getMode())));
                            });

                    //Combine across income groups 1,2,3 and vehicle ownership
                    //FIXME this loses market segmentation
//			return tpODs.collect(Collectors.partitioningBy(od -> od.getMode().equals(Mode.SINGLE_OCC), od -> Combiner.combineODMatrices(od));
                    return tpODs.collect(Collectors.groupingBy(ODMatrix::getMode, new ODMatrixCollector<ODMatrix>()));
                }));
//		return null;
    }
}
