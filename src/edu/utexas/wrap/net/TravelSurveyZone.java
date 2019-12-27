package edu.utexas.wrap.net;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.utexas.wrap.marketsegmentation.CollegeType;
import edu.utexas.wrap.marketsegmentation.EducationClass;
import edu.utexas.wrap.marketsegmentation.IndustryClass;

public class TravelSurveyZone {
	private final Node origin;
	private final int order;
//	private RegionalAreaAnalysisZone parent;
	private AreaClass ac;
	
	//Demographic data
	private Map<Integer,Double> householdsWithXChildren, householdsWithXWorkers;
	private Map<Integer,Double> householdsByIncomeGroup;
	private Map<Integer,Map<Integer,Double>> householdsByWorkersThenSize, householdsByWorkersThenVehicles;
	private Map<Integer, Map<Integer, Map<Integer, Double>>> householdsByIncomeGroupThenWorkersThenVehicles,
		householdsBySizeThenWorkersThenIncomeGroup, householdsByWorkersThenIncomeGroupThenChildren;
	private Map<Integer,Map<IndustryClass,Double>> employmentByIncomeGroupThenIndustry;
	private Map<EducationClass,Double> studentsByEducationLevel;
	private Map<CollegeType, Double> collegesByType;
	
	
	public TravelSurveyZone(Node origin, int order, Map<String,Float> attrs) {
		this.origin = origin;
		this.order = order;
	}
	
	public Node getNode() {
		return origin;
	}
	
	public int getID() {
		return origin.getID();
	}
	
	@Override
	public String toString() {
		return "Zone "+this.getID();
	}
	
//	public void setRAA(RegionalAreaAnalysisZone parent) {
//		this.parent = parent;
//	}
//	
//	public RegionalAreaAnalysisZone getRAA() {
//		return parent;
//	}

	public int getOrder() {
		return order;
	}
	
	public AreaClass getAreaClass() {
		return ac;
	}
	
	public void setAreaClass(AreaClass klass) {
		ac = klass;
	}

	public Double getHouseholdsByChildren(int numChildren) {
		if (householdsWithXChildren == null) {
			System.err.println("No child data loaded for TSZ "+this.origin.getID());
			return 0.0;
		}
		return householdsWithXChildren.getOrDefault(numChildren, 0.0);
	}

	public Double getHouseholdsByWorkers(int numWorkers) {
		if (householdsWithXWorkers == null) {
			System.err.println("No worker data loaded for TSZ "+this.origin.getID());
			return 0.0;
		}
		return householdsWithXWorkers.getOrDefault(numWorkers, 0.0);
	}

	public Double getHouseholdsByIncomeGroup(int incomeGroup) {
		//TODO consider changing the keys of this map to an enumeration
		if (householdsByIncomeGroup == null) {
			System.err.println("No income data loaded for TSZ "+this.origin.getID());
			return 0.0;
		}
		return householdsByIncomeGroup.getOrDefault(incomeGroup, 0.0);
	}

	public Double getHouseholdsByWorkersAndVehicles(int numWorkers, int numVehicles) {
		if (householdsByWorkersThenVehicles == null) {
			if (householdsByIncomeGroupThenWorkersThenVehicles != null) 
				return householdsByIncomeGroupThenWorkersThenVehicles.values().parallelStream()
						.mapToDouble(map -> map.get(numWorkers).get(numVehicles)).sum();
			System.err.println("No worker-vehicle data loaded for TSZ "+this.origin.getID());
			return 0.0;
		}
		return householdsByWorkersThenVehicles.getOrDefault(numWorkers, Collections.<Integer,Double>emptyMap())
				.getOrDefault(numVehicles, 0.0);
	}
	
	public void setHouseholdsByWorkersAndVehicles(Map<Integer, Map<Integer, Double>> hhByWkrThenVehs) {
		householdsByWorkersThenVehicles = hhByWkrThenVehs;
	}

	public Double getHouseholdsByWorkersAndSize(int numWorkers, int householdSize) {
		if (householdsByWorkersThenSize == null) {
			if (householdsBySizeThenWorkersThenIncomeGroup != null) {
				return householdsBySizeThenWorkersThenIncomeGroup
						.getOrDefault(householdSize, Collections.<Integer,Map<Integer,Double>>emptyMap())
						.getOrDefault(numWorkers, Collections.<Integer,Double>emptyMap())
						.values().parallelStream().mapToDouble(Double::doubleValue).sum();
			}
//			System.err.println("No worker-HH size data loaded for TSZ "+this.origin.getID());
			return 0.0;
		}
		return householdsByWorkersThenSize.getOrDefault(numWorkers, Collections.<Integer,Double>emptyMap()).getOrDefault(householdSize, 0.0);
	}
	
	public Double getEmploymentByIncomeGroupAndIndustry(int incomeGroup, IndustryClass industry) {
		if (employmentByIncomeGroupThenIndustry == null) {
			System.err.println("No employment-industry data loaded for TSZ "+this.origin.getID());
			return 0.0;
		}
		return employmentByIncomeGroupThenIndustry.getOrDefault(incomeGroup, Collections.<IndustryClass,Double>emptyMap()).getOrDefault(industry, 0.0);
	}
	
	public Double getStudentsByEducationLevel(EducationClass level) {
		if (studentsByEducationLevel == null) {
			System.err.println("No student data loaded for TSZ "+this.origin.getID());
			return 0.0;
		}
		return studentsByEducationLevel.getOrDefault(level,0.0);
	}
	
	public void setHouseholdsByIncomeGroup(Map<Integer,Double> hhByIG) {
		householdsByIncomeGroup = hhByIG;
	}
	public void setEmploymentByIncomeGroupThenIndustry(Map<Integer,Map<IndustryClass,Double>> empByIGthenIC) {
		employmentByIncomeGroupThenIndustry = empByIGthenIC;
	}
	
	public Double getHouseholdsByIncomeGroupThenWorkersThenVehicles(int incomeGroup, int numWorkers, int numVehicles) {
		if (householdsByIncomeGroupThenWorkersThenVehicles == null) return 0.0;
		return householdsByIncomeGroupThenWorkersThenVehicles.get(incomeGroup).get(numWorkers).get(numVehicles);
	}

	public void setHouseholdsByIncomeGroupThenWorkersThenVehicles(int ig, int wkr, int veh, double val) {
		synchronized(this) {
			if (householdsByIncomeGroupThenWorkersThenVehicles == null) {
				householdsByIncomeGroupThenWorkersThenVehicles = new HashMap<Integer, Map<Integer,Map<Integer,Double>>>();
			}
			householdsByIncomeGroupThenWorkersThenVehicles
			.computeIfAbsent(ig, k -> new HashMap<Integer,Map<Integer,Double>>())
			.computeIfAbsent(wkr, k -> new HashMap<Integer,Double>())
			.put(veh, val);
		}
	}

	public double getEmploymentByIndustry(IndustryClass industry) {
		return employmentByIncomeGroupThenIndustry.values().parallelStream().mapToDouble(map -> map.get(industry)).sum();
	}


	public double getCollegesByType(CollegeType colType) {
		return collegesByType.getOrDefault(colType, 0.0);
	}

	public void setHouseholdsByChildren(Map<Integer, Double> hhByChild) {
		householdsWithXChildren = hhByChild;
	}
	
	public synchronized void setHouseholdsBySizeThenWorkersThenIncomeGroup(int size, int numWorkers, int incomeGrp, double val) {
		if (householdsBySizeThenWorkersThenIncomeGroup == null) householdsBySizeThenWorkersThenIncomeGroup = new ConcurrentHashMap<Integer,Map<Integer,Map<Integer, Double>>>();
		householdsBySizeThenWorkersThenIncomeGroup.putIfAbsent(size, new ConcurrentHashMap<Integer, Map<Integer,Double>>());
		householdsBySizeThenWorkersThenIncomeGroup.get(size).putIfAbsent(numWorkers, new ConcurrentHashMap<Integer,Double>());
		householdsBySizeThenWorkersThenIncomeGroup.get(size).get(numWorkers).put(incomeGrp, val);
	}

	public Double getHouseholdsByIncomeGroupThenChildren(int incomeGroup, int numChildren) {
		// TODO Auto-generated method stub
		if (householdsByWorkersThenIncomeGroupThenChildren == null) return 0.0;
		return householdsByWorkersThenIncomeGroupThenChildren.values().parallelStream().mapToDouble(map -> map.get(incomeGroup).get(numChildren)).sum();
	}
	
	public synchronized void setHouseholdsByWorkersThenIncomeGroupThenChildren(int workers, int incomeGrp, int children, double val) {
		if (householdsByWorkersThenIncomeGroupThenChildren == null) householdsByWorkersThenIncomeGroupThenChildren = new ConcurrentHashMap<Integer, Map<Integer,Map<Integer, Double>>>();
		householdsByWorkersThenIncomeGroupThenChildren.putIfAbsent(workers, new ConcurrentHashMap<Integer, Map<Integer,Double>>());
		householdsByWorkersThenIncomeGroupThenChildren.get(workers).putIfAbsent(incomeGrp, new ConcurrentHashMap<Integer,Double>());
		householdsByWorkersThenIncomeGroupThenChildren.get(workers).get(incomeGrp).put(children, val);
	}

	public Double getHouseholdsByIncomeGroupThenWorkersThenSize(int incomeGrp, int workers, int hhSize) {
		if (householdsBySizeThenWorkersThenIncomeGroup != null) {
			return householdsBySizeThenWorkersThenIncomeGroup
					.getOrDefault(hhSize, Collections.<Integer,Map<Integer,Double>>emptyMap())
					.getOrDefault(workers, Collections.<Integer,Double>emptyMap())
					.getOrDefault(incomeGrp,0.0);
		}
		return 0.0;
	}
}
