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
import edu.utexas.wrap.assignment.AssignmentEvaluator;
import edu.utexas.wrap.assignment.AssignmentOptimizer;
import edu.utexas.wrap.assignment.StaticAssigner;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.ODMatrixCollector;

public class FilePassthroughDummyAssigner implements StaticAssigner<Bush> {
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

	public NetworkSkim getSkim(String id, ToDoubleFunction<Link> function) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void outputFlows(Path outputFile) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return false;
	}

	public void iterate() {
		// TODO Auto-generated method stub
		
	}

	public double getProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void initialize(Collection<ODProfile> profiles) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getProgress(double currentValue, int numIterations) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<Mode> assignedModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph getNetwork() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AssignmentEvaluator<Bush> getEvaluator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Bush> getContainers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AssignmentOptimizer<Bush> getOptimizer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends Link> getLinkType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer maxIterations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTollingPolicy(ToDoubleFunction<Link> policy) {
		// TODO Auto-generated method stub
		
	}
}
