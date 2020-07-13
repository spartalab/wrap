package edu.utexas.wrap.assignment.bush;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.assignment.StaticAssigner;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.util.io.output.ODMatrixStreamWriter;

public class StreamPassthroughAssigner implements StaticAssigner {

	private Collection<ODMatrix> mtxs;
	private final TimePeriod period;
	private final Properties properties;
	private final ProcessBuilder builder;

	public StreamPassthroughAssigner(Path propsFile) throws IOException {
		// TODO Auto-generated constructor stub
		properties = new Properties();
		properties.load(Files.newInputStream(propsFile));
		
		mtxs = new HashSet<ODMatrix>();
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
			System.out.println("Streaming " + period.toString());
			
			Process proc = builder.start();
			OutputStream stdin = proc.getOutputStream();
			streamODs(mtxs, stdin);

			System.out.println(period.toString() + ":TA Process finished with exit code "+ proc.waitFor());
			
		} catch (IOException e) {
			System.out.println(period.toString() + " unable to stream data");
			e.printStackTrace();
			
		} catch (InterruptedException e) {
			System.out.println(period.toString() + " traffic assignment was interrupted");
			e.printStackTrace();
		}
	}

	private void streamODs(Collection<ODMatrix> ods, OutputStream o) {
		ODMatrixStreamWriter.write(period.toString(), ods, o);
	}

	@Override
	public void process(ODProfile profile) {
		mtxs.add(profile.getMatrix(period));
	}

	public TimePeriod getTimePeriod() {
		return period;
	}

}