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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughModalPAMatrix;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.PassengerVehicleTripConverter;
import edu.utexas.wrap.util.TimeOfDaySplitter;
import edu.utexas.wrap.util.io.ODProfileFactory;
import edu.utexas.wrap.util.io.ProductionAttractionFactory;

/**An implementation of a Purpose that allows for reading a demand
 * container from a file, then completing the remaining steps of
 * the specified model using the provided model properties. 
 * 
 * The DummyProject Properties file (*.wrd) supplies a "type" which
 * identifies at which point in the Urban Transportation Modeling
 * System this model begins. This allows for skipping preliminary
 * steps of the model that may not be able to be modeled in this
 * framework yet, but can be supplanted with demand containers
 * read from external sources.
 * 
 * @author William
 *
 */
public class SurrogatePurpose implements Purpose {
	
	private Properties props;
	private Map<Integer, TravelSurveyZone> zones;
	private Path dir;
	private String name;
	private Market parent;
	
	public SurrogatePurpose(Market parent, Path propsPath, Map<Integer, TravelSurveyZone> zones) throws IOException {
		this.parent = parent;
		props = new Properties();
		props.load(Files.newInputStream(propsPath));
		this.zones = zones;
		dir = propsPath.getParent().resolve(props.getProperty("dir"));
		name = propsPath.getFileName().toString();
	}

	/**Develop an ODProfile according to the DummyPurpose's specifications
	 * 
	 * This method reads the {@code type} property and,
	 * if it is set to {@code odProfile}, loads a profile 
	 * per Mode. That is, it then reads the property
	 * {@code odProfile.modes} and, for each valid Mode
	 * {@code foo} listed, reads an OD profile whose location
	 * is specified by the property {@code odProfile.foo.file}.
	 * The ODProfile is associated with the values of time
	 * specified by the key {@code odProfile.foo.vot.bar},
	 * where {@code bar} is a valid TimePeriod.
	 * 
	 * If the {@code type} property is set to any other value
	 * or is null, a new TimeOfDaySplitter is instantiated
	 * as in a BasicPurpose, then is used to split the daily
	 * ODMatrices provided by {@code getDailyODMatrices()}
	 *
	 */
	@Override
	public Collection<ODProfile> getODProfiles(Collection<ODMatrix> matrices) {
		// TODO Auto-generated method stub
		switch (props.getProperty("type")) {
		case "odProfile":
			return loadProfilesFromFiles();
		default:
			return timeOfDaySplitter().split(matrices);
		}	
	}

	private Collection<ODProfile> loadProfilesFromFiles() {
		// TODO Auto-generated method stub
		return Stream.of(props.getProperty("odProfile.modes").split(","))
		.map(mode -> Mode.valueOf(mode))
		.map(mode -> {
			try {
				return ODProfileFactory.readFromFile(
						dir.resolve(props.getProperty("odProfile."+mode.toString()+".file")),
						mode,
						getVOTs(mode),
						zones);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		})
		.collect(Collectors.toSet());
	}
	
	private Map<TimePeriod,Float> getVOTs(Mode mode) {
		return Stream.of(TimePeriod.values())
				.filter(tp -> props.getProperty("odProfile."+mode.toString()+".vot."+tp.toString()) != null)
				.collect(
				Collectors.toMap(
						Function.identity(), 
						tp -> Float.parseFloat(
								props.getProperty(
										"odProfile."+mode.toString()+".vot."+tp.toString()
										)
								)
						)
				);
	}
	
	private Map<TimePeriod,Float> getVOTs() {
		Map<TimePeriod,Float> ret = Stream.of(TimePeriod.values())
				.filter(tp -> props.getProperty("vot."+tp.toString()) != null)
		.collect(
				Collectors.toMap(
						Function.identity(), 
						tp->  Float.parseFloat(props.getProperty("vot."+tp.toString()))
						)
				);
		return ret;
	}
	
	/**Develop a daily ODMatrix according to the DummyPurpose's specifications
	 * 
	 * This method reads the {@code type} property and,
	 * if it is set to {@code odMatrix}, should read an
	 * ODMatrix which encapsulates a full day's demand 
	 * from a file. (As of this writing, this behavior
	 * has not yet been implemented and is left as a 
	 * placeholder)
	 * 
	 * If the {@code type} property is set to any other value
	 * or is null, a new VehicleConverter is instantiated as
	 * in a BasicPurpose, then is used to split the ModalPAMatrices
	 * provided by {@code getModalPAMatrices()}
	 *
	 */
	@Override
	public Collection<ODMatrix> getDailyODMatrices(Collection<ModalPAMatrix> matrices) {
		// TODO Auto-generated method stub
		switch (props.getProperty("type")){
		case "odMatrix":
			throw new RuntimeException("Not yet implemented");
		default:
			return vehicleConverter().convert(matrices);
		}
		
	}

	/**Develop ModalPAMatrices according to the DummyPurpose's specifications
	 * 
	 * This method reads the {@code type} property and,
	 * if it is set to {@code modalPAMatrix}, reads the
	 * property {@code modalPAMatrix.mode} to determine the
	 * relevant Mode and associates it with a PAMatrix
	 * which is read from a file located by the property
	 * {@code modalPAMatrix.file}, via the ProductionAttractionFactory's
	 * {@code readMatrix} method.
	 *
	 */
	@Override
	public Collection<ModalPAMatrix> getModalPAMatrices(AggregatePAMatrix matrix) {
		// TODO Auto-generated method stub
		switch (props.getProperty("type")) {
		case "modalPAMatrix":
			try {
				Mode mode = Mode.valueOf(props.getProperty("modalPAMatrix.mode"));
				return Set.of(
						new FixedMultiplierPassthroughModalPAMatrix(mode, 1.0f, 
								ProductionAttractionFactory.readMatrix(dir.resolve(props.getProperty("modalPAMatrix.file")), false, zones)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			throw new RuntimeException("Not yet implemented");
		default:
			return modeSplitter().split(matrix);
		}
		
	}

	/**Develop an AggregatePAMatrix according to the specifications
	 * 
	 * This method reads the {@code type} property and,
	 * if it is set to {@code aggPAMatrix}, reads an AggregatePAMatrix
	 * from the file located by the property {@code aggPAMatrix.file}.
	 * 
	 * Otherwise, it should develop a PAMap then distribute it
	 * according to the DummyPurpose's specifications. (This behavior
	 * is, as of this writing, not yet implemented and is left as a
	 * placeholder)
	 *
	 */
	@Override
	public AggregatePAMatrix getAggregatePAMatrix(PAMap map) {
		// TODO Auto-generated method stub
		switch (props.getProperty("type")) {
		case "aggPAMatrix":
			try {
				return ProductionAttractionFactory.readMatrix(dir.resolve(props.getProperty("aggPAMatrix.file")),false,zones);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		default:
			throw new RuntimeException("Not yet implemented");

		}
	}

	/**Develop a PAMap according to the specifications
	 * 
	 * This is a stub method, and it is unlikely that a PAMap would
	 * be read from a file through this manner rather than using a
	 * BasicPurpose
	 *
	 */
	@Override
	public PAMap getPAMap() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Map<Mode,Float> modeShares(){
		return Stream.of(Mode.values())
		.filter(mode -> props.containsKey("modeChoice.proportion."+mode.toString()))
		.collect(
				Collectors.toMap(
						Function.identity(),
						mode -> Float.parseFloat(props.getProperty("modeChoice.proportion."+mode.toString()))
						)
				);
	}
	
	private TripInterchangeSplitter modeSplitter() {
		
		return new FixedProportionSplitter(modeShares());
	}
	
	private PassengerVehicleTripConverter vehicleConverter() {
		return new PassengerVehicleTripConverter();
	}
	
	private Map<TimePeriod,Float> departureRates(){
		return Stream.of(TimePeriod.values())
				.filter(tp -> props.containsKey("depRate."+tp.toString()))
				.collect(
						Collectors.toMap(
								Function.identity(),
								tp -> Float.parseFloat(props.getProperty("depRate."+tp.toString()))
								)
						);
//		return ret;
	}

	private Map<TimePeriod,Float> arrivalRates(){
		return Stream.of(TimePeriod.values())
				.filter(tp -> props.containsKey("arrRate."+tp.toString()))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								tp -> Float.parseFloat(props.getProperty("arrRate."+tp.toString()))
								)
						);
	}

	private TimeOfDaySplitter timeOfDaySplitter(){
		return new TimeOfDaySplitter(departureRates(), arrivalRates(),getVOTs());
	}

	/**
	 *
	 */
	public double personTrips() {
		switch (props.getProperty("type")) {
		
		case "odMatrix":
		case "odProfile":
		case "modalODMatrix":
			return 0.0;
		default:
			AggregatePAMatrix mtx = getAggregatePAMatrix(getPAMap());
			return zones.values().stream()
					.mapToDouble(
							orig -> zones.values().stream()
							.mapToDouble(dest -> mtx.getDemand(orig, dest))
							.sum()
							).sum();
		}
	}
	
	public String toString() {
		return name;
	}

	@Override
	public Market getMarket() {
		// TODO Auto-generated method stub
		return parent;
	}

	@Override
	public Collection<TripDistributor> getDistributors() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

	@Override
	public NetworkSkim getNetworkSkim(TripDistributor distributor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FrictionFactorMap getFrictionFunction(TripDistributor distributor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		// TODO Auto-generated method stub
		return zones.values();
	}
}