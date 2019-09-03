package edu.utexas.wrap;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODPassthroughMatrix;
import edu.utexas.wrap.demand.containers.ModalFixedMultiplierPassthroughMatrix;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.marketsegmentation.VehicleSegmenter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.util.DepartureArrivalConverter;

public class NCTCOGPAtoOD {

	public static void main(String[] args) {

		EnumSet<Mode> carModes = EnumSet.of(Mode.SINGLE_OCC, Mode.HOV_2, Mode.HOV_3);
		EnumSet<Mode> truckModes = EnumSet.of(Mode.HVY_TRUCK, Mode.MED_TRUCK);
		
		//Input matrices
		Map<MarketSegment,Map<Mode,ModalPAMatrix>> hbw = null;
		Map<MarketSegment,Map<Mode,ModalPAMatrix>> hnw = null;
		Map<Mode,ModalPAMatrix> nhw = null;
		Map<Mode,ModalPAMatrix> nhnw = null;
		Map<Mode,ModalPAMatrix> truck = null;
		
		//Departure and arrival rates
		Map<MarketSegment,Map<TimePeriod,Double>> hbwDepRates = getRatesBySegment(null, hbw.keySet());
		Map<MarketSegment,Map<TimePeriod,Double>> hbwArrRates = getRatesBySegment(null, hbw.keySet());
		Map<MarketSegment,Map<TimePeriod,Double>> hnwDepRates = getRatesBySegment(null, hnw.keySet());
		Map<MarketSegment,Map<TimePeriod,Double>> hnwArrRates = getRatesBySegment(null, hnw.keySet());
		Map<TimePeriod,Double> nhwDepRates = getRates(null);
		Map<TimePeriod,Double> nhwArrRates = getRates(null);
		Map<TimePeriod,Double> nhnwDepRates = getRates(null);
		Map<TimePeriod,Double> nhnwArrRates = getRates(null);
		Map<TimePeriod,Double> medTruckDepRates = getRates(null);
		Map<TimePeriod,Double> medTruckArrRates = getRates(null);
		Map<TimePeriod,Double> hvyTruckDepRates = getRates(null);
		Map<TimePeriod,Double> hvyTruckArrRates = getRates(null);
		
		//Occupancy rates: SINGLE_OCC and trucks should be 1, HOV_2 should be 0.5, all others undefined
		Map<Mode,Double> hbwOccupancy = getOccupancies(null, carModes);
		Map<Mode,Double> hnwOccupancy = getOccupancies(null, carModes);
		Map<Mode,Double> nhbOccupancy = getOccupancies(null, carModes);
		Map<Mode,Double> truckOccupancy = getOccupancies(null, truckModes);
		
		
		//Get home-based work OD matrices
		for (MarketSegment segment : hbw.keySet()) {
			EnumSet<Mode> modes = segment instanceof VehicleSegmenter && ((VehicleSegmenter) segment).getNumberOfVehicles() < 1 ?
					modes = EnumSet.of(Mode.HOV_2, Mode.HOV_3) : carModes;
					
			Map<Mode,ModalPAMatrix> modalMatrices = hbw.get(segment);
			Map<TimePeriod,Double> depRates = hbwDepRates.get(segment);
			Map<TimePeriod,Double> arrRates = hbwArrRates.get(segment);
			
			mapToODMatrices(hbwOccupancy, modalMatrices, depRates, arrRates, modes);
			
		}
		
		//Get home-based non-work OD matrices
		for (MarketSegment segment : hnw.keySet()) {
			EnumSet<Mode> modes = segment instanceof VehicleSegmenter && ((VehicleSegmenter) segment).getNumberOfVehicles() < 1 ?
				modes = EnumSet.of(Mode.HOV_2, Mode.HOV_3) : carModes;
			
			Map<Mode,ModalPAMatrix> modalMatrices = hnw.get(segment);
			Map<TimePeriod,Double> depRates = hnwDepRates.get(segment);
			Map<TimePeriod,Double> arrRates = hnwArrRates.get(segment);
			
			mapToODMatrices(hnwOccupancy, modalMatrices, depRates, arrRates, modes);
		}
		
		//Get non-home-based work OD matrices
		mapToODMatrices(nhbOccupancy,nhw,nhwDepRates,nhwArrRates,carModes);
		
		//Get non-home-based non-work OD matrices
		mapToODMatrices(nhbOccupancy,nhnw,nhnwDepRates,nhnwArrRates,carModes);
		
		//Get med truck OD matrices
		mapToODMatrices(truckOccupancy,truck,medTruckDepRates,medTruckArrRates,EnumSet.of(Mode.MED_TRUCK));
		
		//Get hvy truck OD matrices
		mapToODMatrices(truckOccupancy,truck,hvyTruckDepRates,hvyTruckArrRates,EnumSet.of(Mode.HVY_TRUCK));
	}

	private static Map<Mode, Double> getOccupancies(DataInputStream input, Collection<Mode> modes) {
		return modes.stream().collect(Collectors.toMap(Function.identity(), mode -> {
			try {
				return input.readDouble();
			} catch (IOException e) {
				e.printStackTrace();
				return 0.0;
			}
		}));
	}

	private static Map<TimePeriod, Double> getRates(DataInputStream input) {
		return Stream.of(TimePeriod.values()).collect(Collectors.toMap(Function.identity(), tp -> {
			try {
				return input.readDouble();
			} catch (IOException e) {
				e.printStackTrace();
				return 0.0;
			}
		}));
	}

	private static Map<MarketSegment, Map<TimePeriod, Double>> getRatesBySegment(DataInputStream input, Collection<MarketSegment> segments) {
		return segments.stream().collect(Collectors.toMap(Function.identity(), segment -> getRates(input)));
	}

	private static Map<TimePeriod,Map<Mode,ODMatrix>> mapToODMatrices(Map<Mode, Double> occupancyRates, Map<Mode, ModalPAMatrix> modalMatrices,
			Map<TimePeriod, Double> depRates, Map<TimePeriod, Double> arrRates, EnumSet<Mode> modes) {
		return Stream.of(TimePeriod.values()).parallel().collect(Collectors.toMap(Function.identity(), timePeriod ->{
			DepartureArrivalConverter converter = new DepartureArrivalConverter(depRates.get(timePeriod),arrRates.get(timePeriod));

			return modes.parallelStream().collect(Collectors.toMap(Function.identity(), mode ->{
			//convert based on departure/arrival rates, multiply by occupancy factors
			ModalPAMatrix matrix = modalMatrices.get(mode);
			Double occRate = occupancyRates.get(mode);
			return new ODPassthroughMatrix(new ModalFixedMultiplierPassthroughMatrix(occRate,converter.convert(matrix)));
			}));
		}));
	}

}
