package blue.endless.glow;

import com.playsawdust.chipper.glow.gl.BakedModel;
import com.playsawdust.chipper.glow.scene.MeshActor;

public class Actors {
	public static BakedModel bakedLookTarget = GlowTest.scheduler.bake(Models.lookTarget);
	public static MeshActor lookTargetActor = new MeshActor();
	
	static {
		lookTargetActor.setRenderModel(bakedLookTarget);
		MasterRenderer.scene.addActor(lookTargetActor);
	}
}
