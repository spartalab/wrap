package edu.utexas.wrap.util.io.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.assignment.StaticAssigner;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.ODMatrixCollector;

public class FilePassthroughDummyAssigner implements StaticAssigner {
	final TimePeriod period;
	final Path outPath;
	final Map<ODMatrix,Float> disaggregatedMtxs;
	final Properties properties;
	final Collection<TravelSurveyZone> zones;

	public FilePassthroughDummyAssigner(Path propsFile, Map<Integer, TravelSurveyZone> zones) throws IOException {
		properties = new Properties();
		properties.load(Files.newInputStream(propsFile));
		disaggregatedMtxs = new HashMap<ODMatrix,Float>();
		period = TimePeriod.valueOf(properties.getProperty("timePeriod"));
		outPath = Paths.get(properties.getProperty("outputFile"));

		Files.createDirectories(outPath.getParent());
		this.zones = zones.values();
	}

	@Override
	public void run() {
		System.out.println("Aggregating ODMatrices");



		try {
			BufferedWriter stream = Files.newBufferedWriter(outPath, StandardOpenOption.CREATE);
			write(stream);
			stream.close();
			System.out.println(period.toString() + ": Output finished");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(BufferedWriter stream) {
		// TODO Auto-generated method stub
		Map<Mode, Map<Float, ODMatrix>> ods = aggregateMtxs();
		System.out.println("Streaming " + period.toString());
		zones.stream().forEach(orig -> 
		zones.stream().forEach(dest -> {
			String line = orig.getID() + ","
					+ dest.getID() + ","
					+ ods.get(Mode.SINGLE_OCC).get(0.8f).getDemand(orig,dest) + ","
					+ ods.get(Mode.SINGLE_OCC).get(1.7f).getDemand(orig,dest) + ","
					+ ods.get(Mode.HOV).get(0.8f).getDemand(orig,dest) + ","
					+ ods.get(Mode.HOV).get(1.7f).getDemand(orig,dest) + ","
					+ ods.get(Mode.SINGLE_OCC).get(0.5f).getDemand(orig,dest) + ","
					+ ods.get(Mode.SINGLE_OCC).get(1.0f).getDemand(orig,dest) + ","
					+ ods.get(Mode.HOV).get(0.5f).getDemand(orig,dest) + ","
					+ ods.get(Mode.HOV).get(1.0f).getDemand(orig,dest) + ","
					+ ods.get(Mode.MED_TRUCK).get(1.5f).getDemand(orig,dest) + ","
					+ ods.get(Mode.HVY_TRUCK).get(1.5f).getDemand(orig,dest) + "\r\n";
			try {
				stream.write(line);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		})
				);
	}

	private Map<Mode,Map<Float,ODMatrix>> aggregateMtxs() {
		// TODO Auto-generated method stub
		return disaggregatedMtxs.entrySet().stream()
				.collect(
						Collectors.groupingBy(
								entry -> getMode(entry.getKey()),
								Collectors.groupingBy(
										entry -> entry.getValue(),
										Collectors.mapping(Map.Entry::getKey, 
												new ODMatrixCollector())
										)
								)
						);
	}


	@Override
	public void process(ODProfile profile) {
		disaggregatedMtxs.put(profile.getMatrix(period),profile.getVOT(period));
	}

	@Override
	public TimePeriod getTimePeriod() {
		return period;
	}

	private Mode getMode(ODMatrix mtx) {
		switch (mtx.getMode()) {
		case HOV_2_PSGR:
		case HOV_3_PSGR:
		case HOV:
			return Mode.HOV;
		default:
			return mtx.getMode();
		}
	}

	public NetworkSkim getSkim(ToDoubleFunction<Link> function) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void outputFlows(Path outputFile) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}
}
