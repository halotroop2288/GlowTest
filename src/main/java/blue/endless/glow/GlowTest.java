package blue.endless.glow;

import com.playsawdust.chipper.glow.RenderScheduler;
import com.playsawdust.chipper.glow.Window;
import com.playsawdust.chipper.glow.control.ControlSet;
import com.playsawdust.chipper.glow.control.MouseLook;
import com.playsawdust.chipper.glow.gl.BakedModel;
import com.playsawdust.chipper.glow.gl.Texture;
import com.playsawdust.chipper.glow.gl.shader.ShaderError;
import com.playsawdust.chipper.glow.gl.shader.ShaderIO;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.mesher.PlatonicSolidMesher;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;
import com.playsawdust.chipper.glow.pass.MeshPass;
import com.playsawdust.chipper.glow.scene.*;
import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.stb.STBPerlin;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GlowTest {
	private static final int PATCH_SIZE = 128;
	
	private static final double SPEED_FORWARD = 0.2;
	private static final double SPEED_RUN = 0.6;
	
	private static final double SPEED_LIMIT = SPEED_FORWARD;
	private static final double SPEED_LIMIT_RUN = SPEED_RUN;
	
	private static final double SPEED_STRAFE = 0.15;
	private static int mouseX = 0;
	private static int mouseY = 0;
	private static boolean grab = false;
	
	static MouseLook mouseLook = ControlsHandler.mouseLook;
	static ControlSet movementControls = ControlsHandler.movementControls;
	
	public static void main(String... args) {
		/* Load up asset(s) */
		BufferedImage stoneImage = MasterRenderer.loadImage("stone");
		BufferedImage orangeImage = MasterRenderer.loadImage("block_face_orange");
		BufferedImage grassImage = MasterRenderer.loadImage("grass");
		
		double lookTargetSize = 1 + 1 / 32.0;
		Mesh lookTargetBase = PlatonicSolidMesher.meshCube(-lookTargetSize / 2, -lookTargetSize / 2, -0.7, lookTargetSize, lookTargetSize, 0.2);
		Mesh lookTargetSpike = PlatonicSolidMesher.meshCube(-0.2, -0.2, -1.2, 0.4, 0.4, 0.4);
		lookTargetBase.combineFrom(lookTargetSpike);
		Model lookTarget = new Model(lookTargetBase);
		ControlsHandler.init();
		
		/* Start GL, spawn up a window, load and compile the ShaderProgram, and attach it to the solid MeshPass. */
		
		Window.initGLFW();
		
		Window window = new Window(1024, 768, "Test");
		
		GLFW.glfwSetKeyCallback(window.handle(), (win, key, scancode, action, mods) -> {
			if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
				GLFW.glfwSetWindowShouldClose(window.handle(), true);
			movementControls.handleKey(key, scancode, action, mods);
		});
		
		GLFW.glfwSetFramebufferSizeCallback(window.handle(), (hWin, width, height) -> {
			MasterRenderer.windowSizeDirty = true;
			MasterRenderer.windowWidth = width;
			MasterRenderer.windowHeight = height;
		});
		
		GLFW.glfwSetCursorPosCallback(window.handle(), (hWin, x, y) -> {
			mouseX = (int) x;
			mouseY = (int) y;
		});
		
		GLFW.glfwSetMouseButtonCallback(window.handle(), (hWin, button, action, mods) ->
				movementControls.handleMouse(button, action, mods));
		
		GLFW.glfwMakeContextCurrent(window.handle());
		
		GL.createCapabilities();
		
		ShaderProgram program;
		RenderScheduler scheduler = RenderScheduler.createDefaultScheduler();
		
		try {
			InputStream shaderStream = GlowTest.class.getClassLoader().getResourceAsStream("shaders/solid.xml");
			program = ShaderIO.load(shaderStream);
			MeshPass solidPass = (MeshPass) scheduler.getPass("solid");
			solidPass.setShader(program);
		} catch (IOException ex) {
			ex.printStackTrace();
			exit(true);
			return;
		} catch (ShaderError err) {
			System.err.println(err.getInfoLog());
			exit(true);
			return;
		}
		
		/* Bake Models into BakedModels */
		
		BufferedImage none = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		none.setRGB(0, 0, 0xFF_FFFFFF);
		Texture noneTex = Texture.of(none);
		scheduler.registerTexture("none", noneTex);
		
		Texture orangeTex = Texture.of(orangeImage);
		scheduler.registerTexture("orangeDiffuse", orangeTex);
		Texture tex = Texture.of(stoneImage);
		scheduler.registerTexture("stoneDiffuse", tex);
		Texture grassTex = Texture.of(grassImage);
		scheduler.registerTexture("grassDiffuse", grassTex);
		
		
		/* Setup the Scene */
		
		Scene scene = new Scene();
		
		ChunkManager chunkManager = new ChunkManager();
		
		ArrayList<Vector3i> pendingChunkList = new ArrayList<>();
		chunkManager.scheduleAll(pendingChunkList);
		
		BakedModel bakedLookTarget = scheduler.bake(lookTarget);
		
		MeshActor lookTargetActor = new MeshActor();
		lookTargetActor.setRenderModel(bakedLookTarget);
		scene.addActor(lookTargetActor);
		
		/* Set the clear color, set global GL state, and start the render loop */
		GL11.glClearColor(0.39f, 0.74f, 1.0f, 0.0f);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL20.GL_MULTISAMPLE);
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		//Matrix4d projection = new Matrix4d();
		Vector2d windowSize = new Vector2d();
		window.getSize(windowSize);
		MasterRenderer.windowWidth = (int) windowSize.x();
		MasterRenderer.windowHeight = (int) windowSize.y();
		MasterRenderer.windowSizeDirty = true;
		double SIXTY_DEGREES = 60.0 * (Math.PI / 180.0);
		program.bind();
		
		Light sun = scene.getSun();
		sun.setRadius(4096);
		sun.setPosition(5 * 32, 5 * 32, 5 * 32);
		
		scene.getCamera().setPosition(32 * 4, 128, 32 * 4);
		
		while (!GLFW.glfwWindowShouldClose(window.handle())) {
			if (MasterRenderer.windowSizeDirty) {
				GL11.glViewport(0, 0, MasterRenderer.windowWidth, MasterRenderer.windowHeight);
				Matrix4d projection = new Matrix4d();
				projection.setPerspective(SIXTY_DEGREES, MasterRenderer.windowWidth / (double) MasterRenderer.windowHeight, 0.01, 1000);
				scene.setProjectionMatrix(projection);
			}
			
			if (movementControls.isActive("grab")) {
				movementControls.lock("grab");
				grab = !grab;
				if (grab) {
					GLFW.glfwSetInputMode(window.handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
				} else {
					GLFW.glfwSetInputMode(window.handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
				}
			}
			if (grab) {
				Vector3d vectorSum = new Vector3d();
				
				if (movementControls.isActive("up")) {
					Vector3d lookVec = mouseLook.getLookVector(null);
					if (movementControls.isActive("run")) {
						lookVec.mul(SPEED_RUN);
					} else {
						lookVec.mul(SPEED_FORWARD);
					}
					vectorSum.add(lookVec);
				}
				
				if (movementControls.isActive("down")) {
					Vector3d lookVec = mouseLook.getLookVector(null);
					if (movementControls.isActive("run")) {
						lookVec.mul(-SPEED_RUN);
					} else {
						lookVec.mul(-SPEED_FORWARD);
					}
					
					vectorSum.add(lookVec);
				}
				
				if (movementControls.isActive("right")) {
					Vector3d rightVec = mouseLook.getRightVector(null);
					rightVec.mul(SPEED_STRAFE);
					
					vectorSum.add(rightVec);
				}
				
				if (movementControls.isActive("left")) {
					Vector3d leftVec = mouseLook.getRightVector(null);
					leftVec.mul(-SPEED_STRAFE);
					
					vectorSum.add(leftVec);
				}
				if (movementControls.isActive("run")) {
					if (vectorSum.length() > SPEED_LIMIT_RUN) vectorSum.normalize().mul(SPEED_LIMIT_RUN);
				} else {
					if (vectorSum.length() > SPEED_LIMIT) vectorSum.normalize().mul(SPEED_LIMIT);
				}
				scene.getCamera().setPosition(vectorSum.add(scene.getCamera().getPosition(null)));
				
				mouseLook.step(mouseX, mouseY, MasterRenderer.windowWidth, MasterRenderer.windowHeight);
				scene.getCamera().setOrientation(mouseLook.getMatrix());
				
				Vector3d lookVec = mouseLook.getLookVector(null);
				CollisionResult collision = new CollisionResult();
				Vector3d lookedAt = Collision.raycastVoxel(scene.getCamera().getPosition(null), lookVec, 100, chunkManager::getShape, collision, false);
				if (lookedAt != null) {
					//lookTargetActor.setPosition(collision.getHitLocation());
					lookTargetActor.setPosition(collision.getVoxelCenter(null));
					
					Vector3d hitNormal = collision.getHitNormal();
					lookTargetActor.lookAlong(hitNormal.x, hitNormal.y, hitNormal.z);
					lookTargetActor.setRenderModel(bakedLookTarget);
				} else {
					lookTargetActor.setRenderModel(null);
				}
				
				if (movementControls.isActive("punch")) {
					movementControls.lock("punch");
					if (lookedAt != null) {
						Vector3d voxelCenter = new Vector3d();
						collision.getVoxelCenter(voxelCenter);
						chunkManager.setBlock((int) voxelCenter.x, (int) voxelCenter.y, (int) voxelCenter.z, Blocks.BLOCK_AIR, scheduler);
					}
				} else if (movementControls.isActive("activate")) {
					movementControls.lock("activate");
					if (lookedAt != null) {
						Vector3d voxelCenter = new Vector3d();
						collision.getVoxelCenter(voxelCenter).add(collision.getHitNormal());
						chunkManager.setBlock((int) voxelCenter.x, (int) voxelCenter.y, (int) voxelCenter.z, Blocks.BLOCK_ORANGE, scheduler);
					}
				}
			}
			
			scene.render(scheduler, program);
			
			GLFW.glfwSwapBuffers(window.handle());
			
			
			GLFW.glfwPollEvents();
			if (!pendingChunkList.isEmpty()) {
				for (int i = 0; i < 2; i++) {
					if (pendingChunkList.isEmpty()) break;
					bakeOne(pendingChunkList, chunkManager, scheduler, scene);
				}
			}
		}
		
		chunkManager.destroy();
		tex.destroy();
		program.destroy();
	}
	
	public static void exit(boolean error) {
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
	
	private static void bakeOne(List<Vector3i> pendingChunkList, ChunkManager chunkManager, RenderScheduler scheduler, Scene scene) {
		boolean allEmpty = true;
		
		while (allEmpty && !pendingChunkList.isEmpty()) {
			Vector3i chunkPos = pendingChunkList.remove(0);
			if (chunkPos.x < 0 || chunkPos.y < 0 || chunkPos.z < 0)
				return; //Skip negative positions, this is a quick and dirty game
			Chunk chunk = Chunk.create();
			chunk.setPosition(new Vector3d(chunkPos.x * 32.0, chunkPos.y * 32.0, chunkPos.z * 32.0));
			//System.out.println("Added chunk at "+chunk.getPosition(null));
			generateInto(chunk);
			chunkManager.set(chunkPos.x, chunkPos.y, chunkPos.z, chunk);
			if (!chunk.isEmpty()) {
				allEmpty = false;
				chunk.bake(scheduler);
				scene.addActor(chunk);
			} // else { /*Do stuff*/}
		}
	}
}
