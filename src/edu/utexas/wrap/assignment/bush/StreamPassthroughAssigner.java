package edu.utexas.wrap.assignment.bush;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.assignment.StaticAssigner;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.util.ODMatrixCollector;
import edu.utexas.wrap.util.io.output.ODMatrixStreamWriter;

public class StreamPassthroughAssigner implements StaticAssigner {

	private Collection<ODMatrix> disaggregatedMtxs;
	private final TimePeriod period;
	private final Properties properties;
	private final ProcessBuilder builder;

	public StreamPassthroughAssigner(Path propsFile) throws IOException {
		// TODO Auto-generated constructor stub
		properties = new Properties();
		properties.load(Files.newInputStream(propsFile));
		
		disaggregatedMtxs = new HashSet<ODMatrix>();
		period = TimePeriod.valueOf(properties.getProperty("timePeriod"));
		
		builder = new ProcessBuilder(
				properties.getProperty("executable"),
				properties.getProperty("netFile"),
				"STREAM",
				properties.getProperty("zoneOrder"));
		

		File out = Paths.get(
				properties.getProperty("outputDir"),
				"/log" + System.currentTimeMillis() + ".txt")
				.toFile();
		out.getParentFile().mkdirs();
		out.createNewFile();
		
		builder.redirectOutput(out);
		builder.redirectError(out);
		
	}

	@Override
	public void run() {
		try {
			System.out.println("Aggregating ODMatrices");
			Map<Mode,Map<Float,ODMatrix>> aggregatedMtxs = aggregateMtxs();
			
			
			System.out.println("Streaming " + period.toString());
			
			Process proc = builder.start();
			OutputStream stdin = proc.getOutputStream();
			ODMatrixStreamWriter.write(period.toString(), aggregatedMtxs, stdin);

			System.out.println(period.toString() + ":TA Process finished with exit code "+ proc.waitFor());
			
		} catch (IOException e) {
			System.out.println(period.toString() + " unable to stream data");
			e.printStackTrace();
			
		} catch (InterruptedException e) {
			System.out.println(period.toString() + " traffic assignment was interrupted");
			e.printStackTrace();
		}
	}

	private Map<Mode,Map<Float,ODMatrix>> aggregateMtxs() {
		// TODO Auto-generated method stub
		return disaggregatedMtxs.stream()
		.collect(
				Collectors.groupingBy(
						this::getMode,
						Collectors.groupingBy(
								ODMatrix::getVOT,
								new ODMatrixCollector()
								)
						)
				);
	}


	@Override
	public void process(ODProfile profile) {
		disaggregatedMtxs.add(profile.getMatrix(period));
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
}