package edu.utexas.wrap.tests.distribution;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.distribution.ModularGravityDistributor;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.io.FrictionFactorFactory;
import edu.utexas.wrap.util.io.ProductionAttractionFactory;
import edu.utexas.wrap.util.io.SkimFactory;

class GravityTest {
	static Map<String,NetworkSkim> skims;
	static Map<Integer,TravelSurveyZone> zones;

	static double epsilon = 1;
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
				
		
		// read zones
		BufferedReader reader = Files.newBufferedReader(Paths.get("data/test/distrib/NCTCOG_zones.csv"));
		reader.readLine();
		AtomicInteger idx = new AtomicInteger(0);

		zones = reader.lines()
				.map(string -> string.split(","))
				.collect(Collectors.toMap(
						args -> Integer.parseInt(args[0]), 
						args -> new TravelSurveyZone(Integer.parseInt(args[0]),idx.getAndIncrement(),AreaClass.values()[Integer.parseInt(args[1])-1])));

		reader.close();
		
		
		// read skims
		skims = new HashMap<String,NetworkSkim>();
		skims.put("pk", SkimFactory.readSkimFile(Paths.get("data/test/distrib/skims/pk.csv"), false, zones));
		skims.put("op", SkimFactory.readSkimFile(Paths.get("data/test/distrib/skims/op.csv"), false, zones));
		skims.put("op_hbw", SkimFactory.readSkimFile(Paths.get("data/test/distrib/skims/op_hbw.csv"), false, zones));
	}
	
	private void balanceCheck(PAMap map) {
		assertEquals(
				map.getAttractors().parallelStream().mapToDouble(map::getAttractions).sum(),
				map.getProducers().parallelStream().mapToDouble(map::getProductions).sum(),
				epsilon
				);
	}

	private void conservationCheck(PAMap map, AggregatePAMatrix mtx) {
		zones.values().parallelStream().forEach(producer ->{
			assertEquals(
					zones.values().stream().mapToDouble(attractor -> mtx.getDemand(producer, attractor)).sum(),
					map.getProductions(producer),
					epsilon,
					producer.toString()
					);
		});
		
		zones.values().parallelStream().forEach(attractor ->{
			assertEquals(
					zones.values().stream().mapToDouble(producer -> mtx.getDemand(producer, attractor)).sum(),
					map.getAttractions(attractor),
					epsilon,
					attractor.toString()
					);
		});
	}
	
	private double mae(AggregatePAMatrix predicted, PAMatrix real) {
		double totAbsErr = 0.0;
		int n = 0;
		
		for (TravelSurveyZone producer : real.getZones()) {
			for (TravelSurveyZone attractor : real.getZones()) {
				totAbsErr += Math.abs(predicted.getDemand(producer, attractor) - real.getDemand(producer, attractor));
				n++;
			}
		}
		
		return totAbsErr/n;
	}
	
	private double mape(AggregatePAMatrix predicted, PAMatrix real) {
		double totPctErr = 0.0;
		int n = 0;
		
		for (TravelSurveyZone producer : real.getZones()) {
			for (TravelSurveyZone attractor : real.getZones()) {
				if (real.getDemand(producer, attractor)==0) continue;
				totPctErr += Math.abs(
						(predicted.getDemand(producer, attractor) - real.getDemand(producer, attractor))/
						real.getDemand(producer, attractor)
						);
				n++;
			}
		}

		return totPctErr/n;
	}
	
	private double mse(AggregatePAMatrix predicted, PAMatrix real) {
		double totSqErr = 0.0;
		int n = 0;
		
		for (TravelSurveyZone producer : real.getZones()) {
			for (TravelSurveyZone attractor : real.getZones()) {
				totSqErr += Math.pow(predicted.getDemand(producer, attractor) - real.getDemand(producer, attractor), 2);
				n++;
			}
		}
		
		return totSqErr/n;
	}
	
	private double mean(PAMatrix mtx) {
		double sum = 0.0;
		int n = 0;
		
		for (TravelSurveyZone producer : mtx.getZones()) {
			for (TravelSurveyZone attractor : mtx.getZones()) {
				sum += mtx.getDemand(producer,attractor);
				n++;
			}
		}
		
		return sum/n;
	}
	

	private void testSuite(FrictionFactorMap ff, PAMap map, PAMatrix real, NetworkSkim skim) {
		TripDistributor distributor = new GravityDistributor(zones.values(), skim, ff);
		AggregatePAMatrix predicted = distributor.distribute(map);
		
		balanceCheck(map);
		
		double realMean = mean(real);
//		System.out.println(realMean);
		
		conservationCheck(map, predicted);
		
		double rmse = Math.sqrt(mse(predicted, real));
		System.out.println("RMSE: "+rmse);
		
		double prmse = rmse/realMean;
		System.out.println("%RMSE: "+prmse);
		
		double mae = mae(predicted, real);
		System.out.println("MAE: "+mae);
		
		double mape = mape(predicted, real);
		System.out.println("MAPE: "+mape);
	}
	

	
	@Test
	void testHBW1PK() throws IOException {
		FrictionFactorMap ff = FrictionFactorFactory.readFactorFile(
				Paths.get("data/test/distrib/ffs/FFactorHBWRK_INC1 PK.csv")
				);
		PAMap map = ProductionAttractionFactory.readMap(
				Paths.get("data/test/distrib/paMaps/hbw_pk_ig1.csv"), 
				false, zones);
		
		PAMatrix real = ProductionAttractionFactory.readMatrix(
				Paths.get("data/test/distrib/paMatrices/hbw_pk_ig1.csv"),
				false, zones);
		NetworkSkim skim = skims.get("pk");
		
		testSuite(ff, map, real, skim);
		
	}


	@Test
	void testHBW2PK() throws IOException {
		FrictionFactorMap ff = FrictionFactorFactory.readFactorFile(
				Paths.get("data/test/distrib/ffs/FFactorHBWRK_INC2 PK.csv")
				);
		PAMap map = ProductionAttractionFactory.readMap(
				Paths.get("data/test/distrib/paMaps/hbw_pk_ig2.csv"), 
				false, zones);
		
		PAMatrix real = ProductionAttractionFactory.readMatrix(
				Paths.get("data/test/distrib/paMatrices/hbw_pk_ig2.csv"),
				false, zones);
		NetworkSkim skim = skims.get("pk");
		
		testSuite(ff, map, real, skim);	
	}
	
	@Test
	void testHBW3PK() throws IOException {
		FrictionFactorMap ff = FrictionFactorFactory.readFactorFile(
				Paths.get("data/test/distrib/ffs/FFactorHBWRK_INC3 PK.csv")
				);
		PAMap map = ProductionAttractionFactory.readMap(
				Paths.get("data/test/distrib/paMaps/hbw_pk_ig3.csv"), 
				false, zones);
		
		PAMatrix real = ProductionAttractionFactory.readMatrix(
				Paths.get("data/test/distrib/paMatrices/hbw_pk_ig3.csv"),
				false, zones);
		NetworkSkim skim = skims.get("pk");
		
		testSuite(ff, map, real, skim);	
	}
	
	@Test
	void testHBW4PK() throws IOException {
		FrictionFactorMap ff = FrictionFactorFactory.readFactorFile(
				Paths.get("data/test/distrib/ffs/FFactorHBWRK_INC4 PK.csv")
				);
		PAMap map = ProductionAttractionFactory.readMap(
				Paths.get("data/test/distrib/paMaps/hbw_pk_ig4.csv"), 
				false, zones);
		
		PAMatrix real = ProductionAttractionFactory.readMatrix(
				Paths.get("data/test/distrib/paMatrices/hbw_pk_ig4.csv"),
				false, zones);
		NetworkSkim skim = skims.get("pk");
		
		testSuite(ff, map, real, skim);
	}

	@Test
	void testHBW1OP() throws IOException {
		FrictionFactorMap ff = FrictionFactorFactory.readFactorFile(
				Paths.get("data/test/distrib/ffs/FFactorHBWRK_INC1 OP.csv")
				);
		PAMap map = ProductionAttractionFactory.readMap(
				Paths.get("data/test/distrib/paMaps/hbw_op_ig1.csv"), 
				false, zones);
		
		PAMatrix real = ProductionAttractionFactory.readMatrix(
				Paths.get("data/test/distrib/paMatrices/hbw_op_ig1.csv"),
				false, zones);
		NetworkSkim skim = skims.get("op_hbw");
		
		testSuite(ff, map, real, skim);	
	}

	@Test
	void testHBW2OP()  throws IOException {
		FrictionFactorMap ff = FrictionFactorFactory.readFactorFile(
				Paths.get("data/test/distrib/ffs/FFactorHBWRK_INC2 OP.csv")
				);
		PAMap map = ProductionAttractionFactory.readMap(
				Paths.get("data/test/distrib/paMaps/hbw_op_ig2.csv"), 
				false, zones);
		
		PAMatrix real = ProductionAttractionFactory.readMatrix(
				Paths.get("data/test/distrib/paMatrices/hbw_op_ig2.csv"),
				false, zones);
		NetworkSkim skim = skims.get("op_hbw");
		
		testSuite(ff, map, real, skim);	
	}
	
	@Test
	void testHBW3OP() throws IOException {
		FrictionFactorMap ff = FrictionFactorFactory.readFactorFile(
				Paths.get("data/test/distrib/ffs/FFactorHBWRK_INC3 OP.csv")
				);
		PAMap map = ProductionAttractionFactory.readMap(
				Paths.get("data/test/distrib/paMaps/hbw_op_ig3.csv"), 
				false, zones);
		
		PAMatrix real = ProductionAttractionFactory.readMatrix(
				Paths.get("data/test/distrib/paMatrices/hbw_op_ig3.csv"),
				false, zones);
		NetworkSkim skim = skims.get("op_hbw");
		
		testSuite(ff, map, real, skim);	
	}
	
	@Test
	void testHBW4OP()  throws IOException {
		FrictionFactorMap ff = FrictionFactorFactory.readFactorFile(
				Paths.get("data/test/distrib/ffs/FFactorHBWRK_INC4 OP.csv")
				);
		PAMap map = ProductionAttractionFactory.readMap(
				Paths.get("data/test/distrib/paMaps/hbw_op_ig4.csv"), 
				false, zones);
		
		PAMatrix real = ProductionAttractionFactory.readMatrix(
				Paths.get("data/test/distrib/paMatrices/hbw_op_ig4.csv"),
				false, zones);
		NetworkSkim skim = skims.get("op_hbw");
		
		testSuite(ff, map, real, skim);	
	}
	

//	@Test
//	void testHBSHP1() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testHBSHP2() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testHBSHP3() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testHBSHP4() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testHBSRE1() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testHBSRE2() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testHBSRE3() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testHBSRE4() {
//		fail("Not yet implemented");
//	}	
//	@Test
//	void testHBPBO1() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testHBPBO2() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testHBPBO3() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testHBPBO4() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testWRKWRK() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testWRKESH() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testWRKOTH() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testSHPSHP() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testSHPOTH() {
//		fail("Not yet implemented");
//	}
//	
//	@Test
//	void testOTHOTH() {
//		fail("Not yet implemented");
//	}

	// Run gravity model approximation trials

	private void errorReport(AggregatePAMatrix predicted, PAMatrix real, int iteration, boolean production) {
		double rmse = Math.sqrt(mse(predicted, real));
		System.out.printf("%2d-%b, %f", iteration, production, rmse);
	}

	private void iterationTrial(FrictionFactorMap ff, PAMap map, PAMatrix real, NetworkSkim skim, int maxIter) {
		// Track the progress of the PAMatrix toward convergence at each iteration of IPF.

		balanceCheck(map);

		AggregatePAMatrix predicted;
		int i = 0;

		// Initialize distributor
		ModularGravityDistributor distributor = new ModularGravityDistributor(zones.values(), skim, ff,
				map, 0.001);

		// Run gravity model fitting to convergence or close to it,
		// reporting the RMSE at each iteration
		while (!distributor.isConverged() && i < maxIter) {

			// Fit according to productions
			distributor.iterateProductions();
			errorReport(distributor.getMatrix(), real, i, true);

			// Fit according to attractions
			distributor.iterateAttractions();
			errorReport(distributor.getMatrix(), real, i, false);

			// Update step counter
			i++;
		}

		if (distributor.isConverged()) System.out.printf("Converged after %d iterations.", i);
	}


	@Test
	void iterationTrialHBW1PK() throws IOException {
		FrictionFactorMap ff = FrictionFactorFactory.readFactorFile(
				Paths.get("data/test/distrib/ffs/FFactorHBWRK_INC1 PK.csv")
		);
		PAMap map = ProductionAttractionFactory.readMap(
				Paths.get("data/test/distrib/paMaps/hbw_pk_ig1.csv"),
				false, zones);

		PAMatrix real = ProductionAttractionFactory.readMatrix(
				Paths.get("data/test/distrib/paMatrices/hbw_pk_ig1.csv"),
				false, zones);
		NetworkSkim skim = skims.get("pk");

		iterationTrial(ff, map, real, skim, 100);
	}

}
