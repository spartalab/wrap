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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.ODProfileProvider;
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
public class Market implements ODProfileProvider {
	private Properties props;
	private Map<String,BasicPurpose> basicPurposes;
	private Collection<DummyPurpose> dummyPurposes;
	private final Collection<TravelSurveyZone> zones;
	private final Map<String,Demographic> basicDemos;
	private final Map<String,FrictionFactorMap> frictionFactors;
	public String name;

	public Market(Path marketFile, Map<Integer,TravelSurveyZone> zoneIDs) throws IOException {
		props = new Properties();
		props.load(Files.newInputStream(marketFile));
		name = marketFile.getFileName().toString();

		Path directory = marketFile.getParent().resolve(props.getProperty("dir"));

		this.zones = zoneIDs.values();
		this.basicDemos = getDemographics(directory,zoneIDs);
		this.frictionFactors = getFrictionFactors(directory);
		basicPurposes = getBasicPurposes(directory);
		dummyPurposes = getDummyPurposes(directory,zoneIDs);

	}

	private Collection<DummyPurpose> getDummyPurposes(Path directory,Map<Integer,TravelSurveyZone> zoneIDs) {
		// TODO Auto-generated method stub
		return Stream.of(props.getProperty("purposes.dummies.ids").split(","))
		.map(id -> {
			try {
				return new DummyPurpose(directory.resolve(props.getProperty("purposes."+id+".file")),zoneIDs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toSet());
	}

	/**Update each of the Market's BasicPurposes with new network cost skims
	 * 
	 * @param skims a Map from a skim ID to the most up-to-date NetworkSkim associated with the ID
	 */
	public void updateSkims(Map<String,NetworkSkim> skims) {
		basicPurposes.values().stream().forEach(purpose -> purpose.updateSkims(skims));
	}

	/**Generate a Stream of ODProfiles for each Purpose associated with this Market
	 *
	 */
	public Stream<ODProfile> getODProfiles() {
		return Stream.concat(
				basicPurposes.values().stream(),
				dummyPurposes.stream())
				.flatMap(Purpose::getODProfiles);
	}

	private Map<String,BasicPurpose> getBasicPurposes(
			Path directory
			) throws IOException {

		String ids = props.getProperty("purposes.ids");
		if (ids == null || ids.isBlank()) return Collections.emptyMap();
		return Stream.of(ids.split(","))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								id -> {
									try {
										return new BasicPurpose(directory.resolve(props.getProperty("purposes."+id+".file")),this);
									} catch (IOException e1) {
										System.err.println("Error while reading purpose file: "+id);
										return null;
									}
								}

								)
						);
	}

	private Map<String,Demographic> getDemographics(Path directory, Map<Integer, TravelSurveyZone> zones) {
		//TODO improve error handling here
		String ids = props.getProperty("demographics.ids");
		if (ids == null || ids.isBlank()) return Collections.emptyMap();
		return Stream.of(ids.split(","))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								name -> {
									Path path = directory.resolve(props.getProperty("demographics."+name+".file"));
									try {
										return new BasicDemographic(path, zones);
									} catch (IOException e) {
										System.err.println("Error while reading demographic file: "+name);
										return null;
									}
								})
						);
	}

	private Map<String,FrictionFactorMap> getFrictionFactors(Path directory) {
		String ids = props.getProperty("frictFacts.ids");
		if (ids == null || ids.isBlank()) return Collections.emptyMap();
		return Stream.of(ids.split(","))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								id -> 
								FrictionFactorFactory.readFactorFile(
										directory.resolve(
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
	public Collection<TravelSurveyZone> getZones() {
		return zones;
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
}
