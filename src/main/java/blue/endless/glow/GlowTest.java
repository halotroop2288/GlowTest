package blue.endless.glow;

import com.playsawdust.chipper.glow.RenderScheduler;
import com.playsawdust.chipper.glow.gl.Texture;
import com.playsawdust.chipper.glow.gl.shader.ShaderError;
import com.playsawdust.chipper.glow.gl.shader.ShaderIO;
import com.playsawdust.chipper.glow.pass.MeshPass;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.stb.STBPerlin;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GlowTest {
	private static final int PATCH_SIZE = 128;
	
	public static final double SIXTY_DEGREES = 60.0 * (Math.PI / 180.0);
	private static final ChunkManager chunkManager = new ChunkManager();
	public static RenderScheduler scheduler= RenderScheduler.createDefaultScheduler();
	private static final ArrayList<Vector3i> pendingChunkList = new ArrayList<>();;
	
	public static void main(String... args) {
		ControlsHandler.setupStage1();
		
		MasterRenderer.setupStage1();
		
		ControlsHandler.setupStage2();
		
		try {
			InputStream shaderStream = GlowTest.class.getClassLoader().getResourceAsStream("shaders/solid.xml");
			MasterRenderer.shaderProg = ShaderIO.load(shaderStream);
			MeshPass solidPass = (MeshPass) scheduler.getPass("solid");
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
		
		Texture noneTex   = MasterRenderer.loadTexture("none", none);
		Texture orangeTex = MasterRenderer.loadTexture("block_face_orange", "orangeDiffuse");
		Texture stoneTex  = MasterRenderer.loadTexture("stone", "stoneDiffuse");
		Texture grassTex  = MasterRenderer.loadTexture("grass", "grassDiffuse");
		
		/* Setup the Scene */
		GlowTest.getChunkManager().scheduleAll(pendingChunkList);
		
		MasterRenderer.setupStage2();
		
		while (!GLFW.glfwWindowShouldClose(MasterRenderer.window.handle())) {
			ControlsHandler.handleControls();
			
			MasterRenderer.render();
			
			if (!pendingChunkList.isEmpty()) {
				for (int i = 0; i < 2; i++) {
					if (pendingChunkList.isEmpty()) break;
					bakeOne();
				}
			}
		}
		
		exit(false);
	}
	
	public static void exit(boolean error) {
		scheduler.destroy();
		chunkManager.destroy();
		MasterRenderer.shaderProg.destroy();
		System.exit(error ? 1 : 0);
	}
	
	private static void generateInto(Chunk chunk) {
		//Preload the palette
		chunk.setBlock(0, 0, 0, Blocks.BLOCK_AIR);
		chunk.setBlock(0, 0, 0, Blocks.BLOCK_STONE);
		chunk.setBlock(0, 0, 0, Blocks.BLOCK_GRASS);
		chunk.setBlock(0, 0, 0, Blocks.BLOCK_AIR);
		
		if (chunk.getY() > 128) return;
		
		//TODO: We could accelerate this considerably by intentionally setting the patch palette and then filling in integers directly
		for (int z = 0; z < 32; z++) {
			for (int x = 0; x < 32; x++) {
				int wx = x + chunk.getX();
				int wz = z + chunk.getZ();
				int wy = chunk.getY();
				
				int terrainHeight = (int) (STBPerlin.stb_perlin_ridge_noise3(wx * 0.003f, 0, wz * 0.003f, 2.0f, 0.5f, 1.0f, 3) * 128.0 + 8.0);
				
				Block surface = (terrainHeight > 64) ? Blocks.BLOCK_STONE : Blocks.BLOCK_GRASS;
				Block interior = Blocks.BLOCK_STONE;
				
				for (int y = 0; y < 32; y++) {
					if (wy + y > terrainHeight) break;
					
					Block cur = (wy + y < terrainHeight - 32) ? interior : surface;
					
					if (wy + y <= terrainHeight) chunk.setBlock(x, y, z, cur);
				}
			}
		}
	}
	
	private static void bakeOne() {
		boolean allEmpty = true;
		
		while (allEmpty && !GlowTest.pendingChunkList.isEmpty()) {
			Vector3i chunkPos = GlowTest.pendingChunkList.remove(0);
			if (chunkPos.x < 0 || chunkPos.y < 0 || chunkPos.z < 0)
				return; //Skip negative positions, this is a quick and dirty game
			Chunk chunk = Chunk.create();
			chunk.setPosition(new Vector3d(chunkPos.x * 32.0, chunkPos.y * 32.0, chunkPos.z * 32.0));
			//System.out.println("Added chunk at "+chunk.getPosition(null));
			generateInto(chunk);
			GlowTest.chunkManager.set(chunkPos.x, chunkPos.y, chunkPos.z, chunk);
			if (!chunk.isEmpty()) {
				allEmpty = false;
				chunk.bake(scheduler);
				MasterRenderer.scene.addActor(chunk);
			} // else { /*Do stuff*/}
		}
	}
	
	public static ChunkManager getChunkManager() {
		return chunkManager;
	}
}
