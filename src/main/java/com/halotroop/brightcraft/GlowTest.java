package com.halotroop.brightcraft;

import com.halotroop.brightcraft.config.controls.ControlsHandler;
import com.playsawdust.chipper.glow.Window;
import com.playsawdust.chipper.glow.gl.shader.ShaderError;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class GlowTest {
	private static final int PATCH_SIZE = 128;
	private static final ChunkManager chunkManager = new ChunkManager();
	public static Window window;
	
	public static void main(String... args) {
		ControlsHandler.mapControls();
		
		MasterRenderer.initWindow();
		
		ControlsHandler.setCallbacks();
		
		try {
			MasterRenderer.setupStage2();
		} catch (IOException | ShaderError e) {
			e.printStackTrace();
		}
		
		/* Bake Models into BakedModels */
		
		Blocks.loadTextures();
		
		/* Setup the Scene */
		GlowTest.getChunkManager().scheduleAll();
		
		MasterRenderer.setupStage3();
		
		while (!GLFW.glfwWindowShouldClose(window.handle())) {
			ControlsHandler.handleControls();
			
			MasterRenderer.render();
			
			if (!ChunkManager.pendingChunkList.isEmpty()) {
				for (int i = 0; i < 2; i++) {
					if (ChunkManager.pendingChunkList.isEmpty()) break;
					ChunkManager.bakeOne();
				}
			}
		}
		
		exit(false);
	}
	
	
	public static void exit(boolean error) {
		MasterRenderer.destroy();
		chunkManager.destroy();
		System.exit(error ? 1 : 0);
	}
	
	public static ChunkManager getChunkManager() {
		return chunkManager;
	}
}
