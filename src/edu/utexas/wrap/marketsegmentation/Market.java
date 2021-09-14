/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap.marketsegmentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.net.BasicDemographic;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.io.FrictionFactorFactory;

/**A grouping of people who make trips according to the same behavior model
 * 
 * This class defines, for a group of people, the demographics associated with
 * the various TravelSurveyZones, the FrictionFactors used in modeling their
 * trip distribution, and the Purposes for which they travel.
 * 
 * @author William
 *
 */
public class Market {
	private Properties props;
	private Map<String,BasicPurpose> basicPurposes;
	private Collection<SurrogatePurpose> dummyPurposes;
	private Map<String,Demographic> basicDemos;
	private Map<String,FrictionFactorMap> frictionFactors;
	private String name;
	private Project parent;
	private Path source;

	public Market(String name, Path marketFile, Project parent) throws IOException {
		this.name = name;
		source = marketFile;
		this.parent = parent;
		props = new Properties();
		loadProperties();

	}

	private void loadData() throws IOException {
		Map<Integer,TravelSurveyZone> zoneIDs = parent.getZones();
		basicDemos = loadDemographics(zoneIDs);
		frictionFactors = loadFrictionFunctions();
		basicPurposes = loadBasicPurposes(zoneIDs);
		dummyPurposes = loadSurrogatePurposes(zoneIDs);
	}

	public void loadProperties() throws IOException {
		props.clear();
		props.load(Files.newInputStream(source));
		loadData();
	}

	private Collection<SurrogatePurpose> loadSurrogatePurposes(Map<Integer,TravelSurveyZone> zoneIDs) {
		// TODO Auto-generated method stub
		String names = props.getProperty("purposes.dummies.ids");
		
		if (names == null) return Collections.<SurrogatePurpose>emptySet();
		return Stream.of(names.split(","))
		.map(id -> {
			try {
				return new SurrogatePurpose(this,getDirectory().resolve(props.getProperty("purposes."+id+".file")),zoneIDs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toSet());
	}

	/**Generate a Stream of ODProfiles for each Purpose associated with this Market
	 *
	 */
//	public Collection<ODProfile> getODProfiles(Collection<ODMatrix> matrices) {
//		return Stream.concat(
//				basicPurposes.values().stream(),
//				dummyPurposes.stream())
//				.flatMap(Purpose::getODProfiles);
//	}

	private Map<String,BasicPurpose> loadBasicPurposes(
			Map<Integer,TravelSurveyZone> zones
			) throws IOException {

		String ids = props.getProperty("purposes.ids");
		if (ids == null || ids.isBlank()) return Collections.emptyMap();
		return Stream.of(ids.split(","))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								id -> {
									try {
										return new BasicPurpose(id,
												getDirectory().resolve(props.getProperty("purposes."+id+".file")),
												this,
												zones);
									} catch (IOException e1) {
										System.err.println("Error while reading purpose file: "+id);
										return null;
									}
								}

								)
						);
	}

	private Map<String,Demographic> loadDemographics(Map<Integer, TravelSurveyZone> zones) {
		//TODO improve error handling here
		String ids = props.getProperty("demographics.ids");
		if (ids == null || ids.isBlank()) return Collections.emptyMap();
		return Stream.of(ids.split(","))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								name -> {
									Path path = getDirectory().resolve(props.getProperty("demographics."+name+".file"));
									try {
										return new BasicDemographic(name,path, zones);
									} catch (IOException e) {
										System.err.println("Error while reading demographic file: "+name);
										return null;
									}
								})
						);
	}
	
	public Collection<Demographic> getDemographics(){
		return basicDemos.values();
	}

	private Map<String,FrictionFactorMap> loadFrictionFunctions() {
		String ids = props.getProperty("frictFacts.ids");
		if (ids == null || ids.isBlank()) return Collections.emptyMap();
		return Stream.of(ids.split(","))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								id -> 
								FrictionFactorFactory.readFactorFile(id,
										getDirectory().resolve(
												props.getProperty("frictFacts."+id+".file")
												)
										)
								)
						)
				;

	}


	/**Get the Demographic associated with a given id string
	 * @param id the string name for a Demographic
	 * @return the corresponding Demographic associated with this Market
	 */
	public Demographic getBasicDemographic(String id) {
		return basicDemos.get(id);
	}

	/**Get the FrictionFactorMap associated with a given id string
	 * @param id the string name for a FrictionFactorMap
	 * @return the corresponding FrictionFactorMap associated with this Market
	 */
	public FrictionFactorMap getFrictionFactor(String id) {
		return frictionFactors.get(id);
	}

	/**
	 * @return the TravelSurveyZones associated with this Market
	 */
	public Map<Integer,TravelSurveyZone> getZones() {
		return parent.getZones();
	}

	/**Get the BasicPurpose associated with a given id string
	 * @param id the string name for a BasicPurpose
	 * @return the corresponding BasicPurpose associated with this Market
	 */
	public BasicPurpose getBasicPurpose(String id) {
		return basicPurposes.get(id);
	}
	
	public String toString() {
		return name;
	}

	public Collection<FrictionFactorMap> getFrictionFunctions() {
		// TODO Auto-generated method stub
		return frictionFactors.values();
	}
	
	public Collection<BasicPurpose> getBasicPurposes(){
		return basicPurposes.values();
	}
	
	public Path getDirectory() {
		return source.getParent();
	}

	public String getDemographicFile(String demographicID) {
		return props.getProperty("demographics."+demographicID+".file");
	}

	public void setDemographicFile(String demographicName, String demographicFile) {
		props.setProperty("demographics."+demographicName+".file", demographicFile);
	}

	public String getFrictionFunctionSource(String frictFuncID) {
		return props.getProperty("frictFacts."+frictFuncID+".file");
	}

	public String getPurposeSource(String purposeID) {
		return props.getProperty("purposes."+purposeID+".file");
	}

	public void setFrictionFunctionSource(String functionID, String functionFile) {
		props.setProperty("frictFacts."+functionID+".file", functionFile);
	}

	public void setPurposeSource(String purposeID, String source) {
		props.setProperty("purposes."+purposeID+".file", source);
	}

	public void addFrictionFunction(String functionID, String functionSourceURI) {
		FrictionFactorMap newMap = FrictionFactorFactory.readFactorFile(functionID, getDirectory().resolve(functionSourceURI));
		frictionFactors.put(functionID, newMap);
		props.setProperty("frictFacts.ids", String.join(",", frictionFactors.keySet()));
		
		props.setProperty("frictFacts."+functionID+".file", functionSourceURI);
	}

	public void removeFrictionFunction(FrictionFactorMap selected) {
		frictionFactors.remove(selected.toString());
		if (!frictionFactors.isEmpty()) props.setProperty("frictFacts.ids", String.join(",", frictionFactors.keySet()));
		else props.remove("frictFacts.ids");
		
		Set<String> keys = props.stringPropertyNames();
		for (String key : keys) {
			if (key.startsWith("frictFacts."+selected.toString())) props.remove(key);
		}
	}
	

	public void addDemographic(String demographicID, String demographicSourceURI) {
		try {
			Demographic newDemo = new BasicDemographic(demographicID,getDirectory().resolve(demographicSourceURI),parent.getZones());
			basicDemos.put(demographicID, newDemo);
			props.setProperty("demographics.ids", String.join(",", basicDemos.keySet()));
			
			props.setProperty("demographics."+demographicID+".file", demographicSourceURI);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public void removeDemographic(Demographic selected) {
		basicDemos.remove(selected.toString());
		if (!basicDemos.isEmpty()) props.setProperty("demographics.ids", String.join(",", basicDemos.keySet()));
		else props.remove("demographics.ids");
		
		Set<String> keys = props.stringPropertyNames();
		for (String key : keys) {
			if (key.startsWith("demographics."+selected.toString())) props.remove(key);
				
		}
	}

	public void removePurpose(BasicPurpose selected) {
		basicPurposes.remove(selected.toString());
		if (!basicPurposes.isEmpty()) props.setProperty("purposes.ids", String.join(",", basicPurposes.keySet()));
		else props.remove("purposes.ids");
		
		Set<String> keys = props.stringPropertyNames();
		for (String key : keys) {
			if (key.startsWith("purposes."+selected.toString())) props.remove(key);
		}
	}
	

	public void addPurpose(String purposeID, String purposeSourceURI){
		try {
		Map<Integer,TravelSurveyZone> map = parent.getZones();
		BasicPurpose newPurpose = new BasicPurpose(purposeID,getDirectory().resolve(purposeSourceURI), this,map);
		basicPurposes.put(purposeID,newPurpose);
		props.setProperty("purposes.ids", String.join(",", basicPurposes.keySet()));
		
		props.setProperty("purposes."+purposeID+".file", purposeSourceURI);
		} catch (IOException e) {
			//TODO
			e.printStackTrace();
		}
	}

	public NetworkSkim getNetworkSkim(String skimID) {
		// TODO Auto-generated method stub
		return parent.getNetworkSkim(skimID);
	}
	
	public Collection<Purpose> getPurposes() {
		return Stream.concat(basicPurposes.values().stream(),dummyPurposes.stream()).collect(Collectors.toSet());
	}

	public void writeProperties() throws IOException {
		// TODO Auto-generated method stub
		props.store(Files.newOutputStream(source, StandardOpenOption.WRITE,StandardOpenOption.CREATE), null);
	}
}
