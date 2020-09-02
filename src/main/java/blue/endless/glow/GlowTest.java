package blue.endless.glow;

import com.playsawdust.chipper.glow.gl.Texture;
import com.playsawdust.chipper.glow.gl.shader.ShaderError;
import com.playsawdust.chipper.glow.gl.shader.ShaderIO;
import com.playsawdust.chipper.glow.pass.MeshPass;
import org.lwjgl.glfw.GLFW;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GlowTest {
	public static final double SIXTY_DEGREES = 60.0 * (Math.PI / 180.0);
	private static final int PATCH_SIZE = 128;
	private static final ChunkManager chunkManager = new ChunkManager();
	
	public static void main(String... args) {
		ControlsHandler.setupStage1();
		
		MasterRenderer.setupStage1();
		
		ControlsHandler.setupStage2();
		
		try {
			InputStream shaderStream = GlowTest.class.getClassLoader().getResourceAsStream("shaders/solid.xml");
			MasterRenderer.shaderProg = ShaderIO.load(shaderStream);
			MeshPass solidPass = (MeshPass) MasterRenderer.scheduler.getPass("solid");
			solidPass.setShader(MasterRenderer.shaderProg);
		} catch (IOException ex) {
			ex.printStackTrace();
			exit(true);
		} catch (ShaderError err) {
			System.err.println(err.getInfoLog());
			exit(true);
		}
		
		if (MasterRenderer.shaderProg == null) return;
		
		/* Bake Models into BakedModels */
		
		BufferedImage none = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		none.setRGB(0, 0, 0xFF_FFFFFF);
		
		Texture noneTex = MasterRenderer.loadTexture("none", none);
		Texture orangeTex = MasterRenderer.loadTexture("block_face_orange", "orangeDiffuse");
		Texture stoneTex = MasterRenderer.loadTexture("stone", "stoneDiffuse");
		Texture grassTex = MasterRenderer.loadTexture("grass", "grassDiffuse");
		
		/* Setup the Scene */
		GlowTest.getChunkManager().scheduleAll(ChunkManager.pendingChunkList);
		
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
