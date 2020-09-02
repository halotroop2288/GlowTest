package blue.endless.glow;

import com.playsawdust.chipper.glow.gl.Texture;
import org.lwjgl.glfw.GLFW;

import java.awt.image.BufferedImage;

public class GlowTest {
	public static final double SIXTY_DEGREES = 60.0 * (Math.PI / 180.0);
	private static final int PATCH_SIZE = 128;
	private static final ChunkManager chunkManager = new ChunkManager();
	
	public static void main(String... args) {
		ControlsHandler.setupStage1();
		
		MasterRenderer.setupStage1();
		
		ControlsHandler.setupStage2();
		
		MasterRenderer.shaderProg = MasterRenderer.loadShaderProgram("solid");
		
		if (MasterRenderer.shaderProg == null) {
			exit(true);
			return;
		}
		
		/* Bake Models into BakedModels */
		
		BufferedImage none = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		none.setRGB(0, 0, 0xFF_FFFFFF);
		
		Texture noneTex = MasterRenderer.loadTexture("none", none);
		Texture orangeTex = MasterRenderer.loadTexture("block_face_orange", "orangeDiffuse");
		Texture stoneTex = MasterRenderer.loadTexture("stone", "stoneDiffuse");
		Texture grassTex = MasterRenderer.loadTexture("grass", "grassDiffuse");
		
		/* Setup the Scene */
		GlowTest.getChunkManager().scheduleAll();
		
		MasterRenderer.setupStage2();
		
		while (!GLFW.glfwWindowShouldClose(MasterRenderer.window.handle())) {
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
