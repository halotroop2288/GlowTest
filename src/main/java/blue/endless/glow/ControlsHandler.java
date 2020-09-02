package blue.endless.glow;

import com.playsawdust.chipper.glow.control.ControlSet;
import com.playsawdust.chipper.glow.control.MouseLook;
import com.playsawdust.chipper.glow.scene.Collision;
import com.playsawdust.chipper.glow.scene.CollisionResult;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ControlsHandler {
	private static final double SPEED_FORWARD = 0.2;
	private static final double SPEED_RUN = 0.6;
	private static final double SPEED_LIMIT = SPEED_FORWARD;
	private static final double SPEED_LIMIT_RUN = SPEED_RUN;
	private static final double SPEED_STRAFE = 0.15;
	public static MouseLook mouseLook = new MouseLook();
	public static ControlSet movementControls = new ControlSet();
	static int mouseX = 0;
	static int mouseY = 0;
	static boolean grab = false;
	
	public static void handleControls() {
		if (MasterRenderer.windowSizeDirty) {
			GL11.glViewport(0, 0, MasterRenderer.windowWidth, MasterRenderer.windowHeight);
			Matrix4d projection = new Matrix4d();
			projection.setPerspective(GlowTest.SIXTY_DEGREES, MasterRenderer.windowWidth / (double) MasterRenderer.windowHeight, 0.01, 1000);
			MasterRenderer.scene.setProjectionMatrix(projection);
		}
		
		if (ControlsHandler.movementControls.isActive("grab")) {
			ControlsHandler.movementControls.lock("grab");
			ControlsHandler.grab = !ControlsHandler.grab;
			if (ControlsHandler.grab) {
				GLFW.glfwSetInputMode(MasterRenderer.window.handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
			} else {
				GLFW.glfwSetInputMode(MasterRenderer.window.handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
			}
		}
		if (ControlsHandler.grab) {
			Vector3d playerVelocity = new Vector3d();
			
			if (ControlsHandler.movementControls.isActive("up")) {
				Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
				if (ControlsHandler.movementControls.isActive("run")) {
					lookVec.mul(SPEED_RUN);
				} else {
					lookVec.mul(SPEED_FORWARD);
				}
				playerVelocity.add(lookVec);
			}
			
			if (ControlsHandler.movementControls.isActive("down")) {
				Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
				if (ControlsHandler.movementControls.isActive("run")) {
					lookVec.mul(-SPEED_RUN);
				} else {
					lookVec.mul(-SPEED_FORWARD);
				}
				
				playerVelocity.add(lookVec);
			}
			
			if (ControlsHandler.movementControls.isActive("right")) {
				Vector3d rightVec = ControlsHandler.mouseLook.getRightVector(null);
				rightVec.mul(SPEED_STRAFE);
				
				playerVelocity.add(rightVec);
			}
			
			if (ControlsHandler.movementControls.isActive("left")) {
				Vector3d leftVec = ControlsHandler.mouseLook.getRightVector(null);
				leftVec.mul(-SPEED_STRAFE);
				
				playerVelocity.add(leftVec);
			}
			if (ControlsHandler.movementControls.isActive("run")) {
				if (playerVelocity.length() > SPEED_LIMIT_RUN) playerVelocity.normalize().mul(SPEED_LIMIT_RUN);
			} else {
				if (playerVelocity.length() > SPEED_LIMIT) playerVelocity.normalize().mul(SPEED_LIMIT);
			}
			MasterRenderer.scene.getCamera().setPosition(playerVelocity.add(MasterRenderer.scene.getCamera().getPosition(null)));
			
			ControlsHandler.mouseLook.step(ControlsHandler.mouseX, ControlsHandler.mouseY, MasterRenderer.windowWidth, MasterRenderer.windowHeight);
			MasterRenderer.scene.getCamera().setOrientation(ControlsHandler.mouseLook.getMatrix());
			
			Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
			CollisionResult collision = new CollisionResult();
			Vector3d lookedAt = Collision.raycastVoxel(MasterRenderer.scene.getCamera().getPosition(null), lookVec, 100, GlowTest.getChunkManager()::getShape, collision, false);
			if (lookedAt != null) {
				//lookTargetActor.setPosition(collision.getHitLocation());
				Actors.lookTargetActor.setPosition(collision.getVoxelCenter(null));
				
				Vector3d hitNormal = collision.getHitNormal();
				Actors.lookTargetActor.lookAlong(hitNormal.x, hitNormal.y, hitNormal.z);
				Actors.lookTargetActor.setRenderModel(Actors.bakedLookTarget);
			} else {
				Actors.lookTargetActor.setRenderModel(null);
			}
			
			if (ControlsHandler.movementControls.isActive("punch")) {
				ControlsHandler.movementControls.lock("punch");
				if (lookedAt != null) {
					Vector3d voxelCenter = new Vector3d();
					collision.getVoxelCenter(voxelCenter);
					GlowTest.getChunkManager().setBlock((int) voxelCenter.x, (int) voxelCenter.y, (int) voxelCenter.z, Blocks.BLOCK_AIR, MasterRenderer.scheduler);
				}
			} else if (ControlsHandler.movementControls.isActive("activate")) {
				ControlsHandler.movementControls.lock("activate");
				if (lookedAt != null) {
					Vector3d voxelCenter = new Vector3d();
					collision.getVoxelCenter(voxelCenter).add(collision.getHitNormal());
					GlowTest.getChunkManager().setBlock((int) voxelCenter.x, (int) voxelCenter.y, (int) voxelCenter.z, Blocks.BLOCK_ORANGE, MasterRenderer.scheduler);
				}
			}
		}
	}
	
	public static void setupStage1() {
		movementControls.mapWASD();
		movementControls.map("grab", GLFW.GLFW_KEY_TAB);
		movementControls.map("run", GLFW.GLFW_KEY_LEFT_SHIFT);
		movementControls.mapMouse("punch", GLFW.GLFW_MOUSE_BUTTON_LEFT);
		movementControls.mapMouse("activate", GLFW.GLFW_MOUSE_BUTTON_RIGHT);
	}
	
	public static void setupStage2() {
		GLFW.glfwSetKeyCallback(MasterRenderer.window.handle(), (win, key, scancode, action, mods) -> {
			if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
				GLFW.glfwSetWindowShouldClose(MasterRenderer.window.handle(), true);
			ControlsHandler.movementControls.handleKey(key, scancode, action, mods);
		});
		
		GLFW.glfwSetFramebufferSizeCallback(MasterRenderer.window.handle(), (hWin, width, height) -> {
			MasterRenderer.windowSizeDirty = true;
			MasterRenderer.windowWidth = width;
			MasterRenderer.windowHeight = height;
		});
		
		GLFW.glfwSetCursorPosCallback(MasterRenderer.window.handle(), (hWin, x, y) -> {
			ControlsHandler.mouseX = (int) x;
			ControlsHandler.mouseY = (int) y;
		});
		
		GLFW.glfwSetMouseButtonCallback(MasterRenderer.window.handle(), (hWin, button, action, mods) ->
				ControlsHandler.movementControls.handleMouse(button, action, mods));
	}
}
