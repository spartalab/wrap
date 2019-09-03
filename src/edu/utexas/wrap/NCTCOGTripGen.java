package edu.utexas.wrap;

import java.io.DataInputStream;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import edu.utexas.wrap.generation.AreaSpecificTripGenerator;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.marketsegmentation.ChildSegment;
import edu.utexas.wrap.marketsegmentation.EducationClass;
import edu.utexas.wrap.marketsegmentation.IncomeGroupIndustrySegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.IndustryClass;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.marketsegmentation.StudentSegment;
import edu.utexas.wrap.marketsegmentation.WorkerHouseholdSizeSegment;
import edu.utexas.wrap.marketsegmentation.WorkerSegment;
import edu.utexas.wrap.marketsegmentation.WorkerVehicleSegment;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;

public class NCTCOGTripGen {

	public static void main(String[] args) {
		Graph g = null;
		
		//Get all the market segmentations
		Collection<MarketSegment> childSegs = getChildSegments();
		Collection<MarketSegment> wkrVehSegs = getWorkerVehicleSegments();
		Collection<MarketSegment> wkrHhsSegs = getWorkerHouseholdSizeSegments();
		Collection<MarketSegment> collegeSegs = getCollegeSegments();
		Collection<MarketSegment> igIndSegs = getIncomeGroupIndustryTypeSegments();
		
		//Read production and attraction rates for each trip purpose
		Map<MarketSegment,Double> hbwProdRates = getRates(wkrVehSegs,null);
		Map<MarketSegment,Double> hbshopProdRates = getRates(wkrHhsSegs,null);
		Map<MarketSegment,Double> hbcollProdRates = getRates(wkrVehSegs,null);
		Map<MarketSegment,Double> hbcollAttrRates = getRates(collegeSegs,null);
		Map<MarketSegment,Double> hbk12ProdRates = getRates(childSegs,null);
//		Map<MarketSegment,Double> hbk12AttrRates = getRates(null,null);
		Map<MarketSegment,Double> hbsreProdRates = getRates(childSegs,null);
		Map<MarketSegment,Double> hbpboProdRates = getRates(childSegs,null);
		
		Map<MarketSegment,Map<AreaClass,Double>> hbwAttrRates = getAreaRates(igIndSegs,null);
		Map<MarketSegment,Map<AreaClass,Double>> hbshopAttrRates = getAreaRates(igIndSegs,null);
		Map<MarketSegment,Map<AreaClass,Double>> hbsreAttrRates = getAreaRates(igIndSegs,null);
		Map<MarketSegment,Map<AreaClass,Double>> hbpboAttrRates = getAreaRates(igIndSegs,null);

		
		//Get the area type factors for each trip purpose
		Map<AreaClass,Double> hbshopProdFactors = getFactors(null);
		Map<AreaClass,Double> hbcollProdFactors = getFactors(null);
		Map<AreaClass,Double> hbk12ProdFactors = getFactors(null);
		Map<AreaClass,Double> hbsreProdFactors = getFactors(null);
		Map<AreaClass,Double> hbpboProdFactors = getFactors(null);
		
		Map<AreaClass,Double> hbwAttrFactors = getFactors(null);
		
		
		//Build production and attraction generators for each trip purpose
		BasicTripGenerator hbwProd = new BasicTripGenerator(g, hbwProdRates);
		AreaSpecificTripGenerator hbwAttr = new AreaSpecificTripGenerator(g, hbwAttrRates);
		
		BasicTripGenerator hbshopProd = new BasicTripGenerator(g, hbshopProdRates);
		AreaSpecificTripGenerator hbshopAttr = new AreaSpecificTripGenerator(g, hbshopAttrRates);
		
		BasicTripGenerator hbcollProd = new BasicTripGenerator(g, hbcollProdRates);
		BasicTripGenerator hbcollAttr = new BasicTripGenerator(g, hbcollAttrRates);
		
		BasicTripGenerator hbk12Prod = new BasicTripGenerator(g, hbk12ProdRates);
		//TODO figure out k12 attractions
//		BasicTripGenerator hbk12Attr = new BasicTripGenerator(g, hbk12AttrRates);
		
		BasicTripGenerator hbsreProd = new BasicTripGenerator(g, hbsreProdRates);
		AreaSpecificTripGenerator hbsreAttr = new AreaSpecificTripGenerator(g, hbsreAttrRates);
		
		BasicTripGenerator hbpboProd = new BasicTripGenerator(g, hbpboProdRates);
		AreaSpecificTripGenerator hbpboAttr = new AreaSpecificTripGenerator(g, hbpboAttrRates);
		
		//Generate productions
		wkrVehSegs.parallelStream().collect(Collectors.toMap(Function.identity(),segment -> hbwProd.generate(segment)));
		wkrHhsSegs.parallelStream().collect(Collectors.toMap(Function.identity(), segment -> hbshopProd.generateAndScale(segment, hbshopProdFactors)));
		wkrVehSegs.parallelStream().collect(Collectors.toMap(Function.identity(), segment -> hbcollProd.generateAndScale(segment, hbcollProdFactors)));
		childSegs.parallelStream().collect(Collectors.toMap(Function.identity(), segment -> hbk12Prod.generateAndScale(segment,hbk12ProdFactors)));
		childSegs.parallelStream().collect(Collectors.toMap(Function.identity(), segment -> hbsreProd.generateAndScale(segment,hbsreProdFactors)));
		childSegs.parallelStream().collect(Collectors.toMap(Function.identity(), segment -> hbpboProd.generateAndScale(segment, hbpboProdFactors)));
	
		//Generate attractions
		igIndSegs.parallelStream().collect(Collectors.toMap(Function.identity(), segment -> hbwAttr.generateAndScale(segment, hbwAttrFactors)));
		igIndSegs.parallelStream().collect(Collectors.toMap(Function.identity(), segment -> hbshopAttr.generate(segment)));
		collegeSegs.parallelStream().collect(Collectors.toMap(Function.identity(), segment -> hbcollAttr.generate(segment)));
		//TODO K12 attractions
		igIndSegs.parallelStream().collect(Collectors.toMap(Function.identity(), segment -> hbsreAttr.generate(segment)));
		igIndSegs.parallelStream().collect(Collectors.toMap(Function.identity(), segment -> hbpboAttr.generate(segment)));
		
		
	}
	
	private static Map<MarketSegment, Double> getRates(Collection<MarketSegment> segments, DataInputStream input) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}
	
	private static Map<MarketSegment,Map<AreaClass,Double>> getAreaRates(Collection<MarketSegment> segments, DataInputStream input){
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}
	
	private static Map<AreaClass,Double> getFactors(DataInputStream input){
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	public static Collection<MarketSegment> getChildSegments(){
		return IntStream.range(0,4).mapToObj(i -> new ChildSegment(i)).collect(Collectors.toSet());
	}
	
	public static Collection<MarketSegment> getWorkerSegments(){
		return IntStream.range(0, 4).mapToObj(workers -> new WorkerSegment(workers)).collect(Collectors.toSet());
	}
	
	public static Collection<MarketSegment> getIncomeGroupSegments(){
		return IntStream.range(1, 5).mapToObj(ig -> new IncomeGroupSegment(ig)).collect(Collectors.toSet());
	}
	
	public static Collection<MarketSegment> getWorkerVehicleSegments(){
		return IntStream.range(0, 4).mapToObj(workers -> 
			IntStream.range(0, 4).mapToObj(vehicles -> 
				new WorkerVehicleSegment(workers, vehicles)))
			.flatMap(Function.identity()).collect(Collectors.toSet());
	}
	
	public static Collection<MarketSegment> getWorkerHouseholdSizeSegments(){
		return IntStream.range(0,4).mapToObj(workers ->
			IntStream.range(1, 5).mapToObj(hhSize ->
				new WorkerHouseholdSizeSegment(workers,hhSize)))
			.flatMap(Function.identity()).collect(Collectors.toSet());
	}
	
	public static Collection<MarketSegment> getCollegeSegments(){
		return EnumSet.of(EducationClass.COLLEGE,EducationClass.UNIVERSITY).parallelStream().map(sch -> new StudentSegment(sch)).collect(Collectors.toSet());
	}
	
	public static Collection<MarketSegment> getIncomeGroupIndustryTypeSegments(){
		return IntStream.range(1, 5).mapToObj(ig ->
			Stream.of(IndustryClass.values()).map(ind ->
				new IncomeGroupIndustrySegment(ig,ind)))
			.flatMap(Function.identity()).collect(Collectors.toSet());
	}
	
}
