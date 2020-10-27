package edu.utexas.wrap.marketsegmentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.PassengerVehicleTripConverter;
import edu.utexas.wrap.util.TimeOfDaySplitter;
import edu.utexas.wrap.util.io.ODProfileFactory;

public class DummyPurpose implements Purpose {
	
	private Properties props;
	private Map<Integer, TravelSurveyZone> zones;
	private Path dir;
	
	public DummyPurpose(Path propsPath, Map<Integer, TravelSurveyZone> zones) throws IOException {
		props = new Properties();
		props.load(Files.newInputStream(propsPath));
		this.zones = zones;
		dir = propsPath.getParent().resolve(props.getProperty("dir"));
	}

	@Override
	public Stream<ODProfile> getODProfiles() {
		// TODO Auto-generated method stub
		switch (props.getProperty("type")) {
		case "odProfile":
			return loadProfilesFromFiles();
		default:
			return timeOfDaySplitter().split(getDailyODMatrices());
		}	
	}

	private Stream<ODProfile> loadProfilesFromFiles() {
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
		});
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

	@Override
	public Stream<ODMatrix> getDailyODMatrices() {
		// TODO Auto-generated method stub
		switch (props.getProperty("type")){
		case "odMatrix":
			throw new RuntimeException("Not yet implemented");
		default:
			return vehicleConverter().convert(getModalPAMatrices());
		}
		
	}

	@Override
	public Stream<ModalPAMatrix> getModalPAMatrices() {
		// TODO Auto-generated method stub
		switch (props.getProperty("type")) {
		case "modalPAMatrix":
			throw new RuntimeException("Not yet implemented");
		default:
			return modeSplitter().split(getAggregatePAMatrix());
		}
		
	}

	@Override
	public AggregatePAMatrix getAggregatePAMatrix() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public PAMap getPAMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
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
		return new PassengerVehicleTripConverter(getVOT());
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
		return new TimeOfDaySplitter(departureRates(), arrivalRates());
	}

	
	
}