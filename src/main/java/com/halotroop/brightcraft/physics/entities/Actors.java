package com.halotroop.brightcraft.physics.entities;

import com.halotroop.brightcraft.Models;
import com.halotroop.brightcraft.MotherRenderer;
import com.playsawdust.chipper.glow.gl.BakedModel;
import com.playsawdust.chipper.glow.scene.MeshActor;

import java.util.HashMap;
import java.util.Map;

public class Actors {
	private static final Map<MeshActor, BakedModel> registeredModels = new HashMap<>();
	
	public static final MeshActor lookTargetActor = register(new MeshActor(), MotherRenderer.scheduler.bake(Models.lookTarget));
	
	private static MeshActor register(MeshActor actor, BakedModel model) {
		actor.setRenderModel(model);
		registeredModels.put(actor, model);
		return actor;
	}
	
	public static BakedModel getModel(MeshActor actor) {
		return registeredModels.get(actor);
	}
	
	public static void load() {
		// TOUCH
	}
}
