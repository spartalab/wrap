package edu.utexas.wrap.net;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
	private Map<Integer, Map<Integer, Map<Integer, Double>>> householdsByIncomeGroupThenWorkersThenVehicles;
	private Map<Integer,Map<IndustryClass,Double>> employmentByIncomeGroupThenIndustry;
	private Map<EducationClass,Double> studentsByEducationLevel;
	
	
	public TravelSurveyZone(Node origin, int order, Map<String,Float> attrs) {
		this.origin = origin;
		this.order = order;
	}
	
	public Node getNode() {
		return origin;
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
		return householdsWithXChildren.getOrDefault(numChildren, 0.0);
	}

	public Double getHouseholdsByWorkers(int numWorkers) {
		return householdsWithXWorkers.getOrDefault(numWorkers, 0.0);
	}

	public Double getHouseholdsByIncomeGroup(int incomeGroup) {
		//TODO consider changing the keys of this map to an enumeration
		return householdsByIncomeGroup.getOrDefault(incomeGroup, 0.0);
	}

	public Double getHouseholdsByWorkersAndVehicles(int numWorkers, int numVehicles) {
		return householdsByWorkersThenVehicles.getOrDefault(numWorkers, Collections.<Integer,Double>emptyMap())
				.getOrDefault(numVehicles, 0.0);
	}

	public Double getHouseholdsByWorkersAndSize(int numWorkers, int householdSize) {
		return householdsByWorkersThenSize.getOrDefault(numWorkers, Collections.<Integer,Double>emptyMap()).getOrDefault(householdSize, 0.0);
	}
	
	public Double getEmploymentByIncomeGroupAndIndustry(int incomeGroup, IndustryClass industry) {
		return employmentByIncomeGroupThenIndustry.getOrDefault(incomeGroup, Collections.<IndustryClass,Double>emptyMap()).getOrDefault(industry, 0.0);
	}
	
	public Double getStudentsByEducationLevel(EducationClass level) {
		return studentsByEducationLevel.getOrDefault(level,0.0);
	}
	
	public void setHouseholdsByIncomeGroup(Map<Integer,Double> hhByIG) {
		householdsByIncomeGroup = hhByIG;
	}
	public void setEmploymentByIncomeGroupThenIndustry(Map<Integer,Map<IndustryClass,Double>> empByIGthenIC) {
		employmentByIncomeGroupThenIndustry = empByIGthenIC;
	}
	
	public Double getHouseholdsByIncomeGroupThenWorkersThenVehicles(int incomeGroup, int numWorkers, int numVehicles) {
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
}
