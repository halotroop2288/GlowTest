package com.halotroop.brightcraft;

import com.playsawdust.chipper.glow.mesher.PlatonicSolidMesher;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;

public class Models {
	public static final Model lookTarget;
	
	static {
		double lookTargetSize = 1 + 1 / 32.0;
		Mesh lookTargetBase = PlatonicSolidMesher.meshCube(-lookTargetSize / 2, -lookTargetSize / 2, -0.7,
				lookTargetSize, lookTargetSize, 0.2);
		Mesh lookTargetSpike = PlatonicSolidMesher.meshCube(-0.2, -0.2, -1.2, 0.4, 0.4, 0.4);
		lookTargetBase.combineFrom(lookTargetSpike);
		lookTarget = new Model(lookTargetBase);
	}
}
