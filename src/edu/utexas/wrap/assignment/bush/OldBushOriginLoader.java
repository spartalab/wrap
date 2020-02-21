package edu.utexas.wrap.assignment.bush;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class OldBushOriginLoader extends OldBushOriginBuilder {
	Boolean print = true;
	
	public OldBushOriginLoader(Graph g, TravelSurveyZone zone, Set<OldBushOrigin> origins) {
		super(g, zone, origins);
	}

	@Override
	public void run() {
		orig = new OldBushOrigin(zone);
		for (Mode c : map.keySet()) {
			for (Float vot : map.get(c).keySet()) {
				AutoDemandMap odm = map.get(c).get(vot);
				if (!odm.isEmpty()) try {
					orig.loadBush(g, vot, odm, c);
				} catch (IOException ex) {
//					if (print) System.out.println("Couldn't load origin "+o.getID()+" "+c+" VOT="+vot+". Building...");
					Bush bush = orig.buildBush(g, vot, odm, c);
					
					if (print) {
						StringBuilder sb = new StringBuilder();
						for (byte b : g.getMD5()) {
							sb.append(String.format("%02X", b));
						}

						File file = new File(sb+"/"+zone.node().getID()+"/"+c+"-"+vot+".bush");
						file.getParentFile().mkdirs();
						FileOutputStream out = null;
						try {
							out = new FileOutputStream(file);
							bush.toByteStream(out);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							try {
								if (out != null) out.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		orig.deleteInitMap();
		origins.add(orig);
	}
}
