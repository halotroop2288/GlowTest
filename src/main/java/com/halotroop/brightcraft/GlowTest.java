package com.halotroop.brightcraft;

import com.halotroop.brightcraft.config.controls.ControlsHandler;
import com.halotroop.brightcraft.debug.DebugControls;
import com.halotroop.brightcraft.physics.entities.Player;
import com.playsawdust.chipper.glow.Window;
import com.playsawdust.chipper.glow.gl.shader.ShaderError;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class GlowTest {
	public static final ChunkManager chunkManager = new ChunkManager();
	private static final int PATCH_SIZE = 128;
	public static Window window;
	public static Player player = new Player();
	
	public static void main(String... args) {
		ControlsHandler.mapControls();
		MotherRenderer.initWindow();
		ControlsHandler.setCallbacks();
		
		
		try {
			Class.forName("com.halotroop.brightcraft.debug.DebugControls");
		} catch (NoClassDefFoundError | ClassNotFoundException e) {
			e.printStackTrace();
		}
		DebugControls.mapDebugControls();
		
		
		try {
			MotherRenderer.setupStage2();
		} catch (IOException | ShaderError e) {
			e.printStackTrace();
			exit(true);
			return;
		}
		
		Blocks.loadTextures();
		
		/* Setup the Scene */
		chunkManager.scheduleAll();
		
		MotherRenderer.setupScene();
		
		while (!GLFW.glfwWindowShouldClose(window.handle())) {
			ControlsHandler.handleControls();
			
			Timer.tick();
			
			MotherRenderer.render();
			
			if (!ChunkManager.pendingChunkList.isEmpty()) {
				for (int i = 0; i < 2; i++) {
					if (ChunkManager.pendingChunkList.isEmpty()) break;
					ChunkManager.bakeOne();
				}
			}
			
			GLFW.glfwPollEvents();
		}
		
		exit(false);
	}
	
	public static void naturalExit() {
		GLFW.glfwSetWindowShouldClose(GlowTest.window.handle(), true);
	}
	
	public static void changeSubtitle(String newSubtitle) {
		GLFW.glfwSetWindowTitle(GlowTest.window.handle(), "GlowTest  |  " + newSubtitle);
	}
	
	public static void exit(boolean error) {
		MotherRenderer.destroyAll();
		chunkManager.destroy();
		System.exit(error ? 1 : 0);
	}
}
