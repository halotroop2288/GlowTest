package com.halotroop.brightcraft.config.controls;

import com.halotroop.brightcraft.Actors;
import com.halotroop.brightcraft.Blocks;
import com.halotroop.brightcraft.GlowTest;
import com.halotroop.brightcraft.MasterRenderer;
import com.playsawdust.chipper.glow.control.ControlSet;
import com.playsawdust.chipper.glow.control.MouseLook;
import com.playsawdust.chipper.glow.scene.Collision;
import com.playsawdust.chipper.glow.scene.CollisionResult;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

public class ControlsHandler {
	private static final double SPEED_FORWARD = 0.2;
	private static final double SPEED_RUN = 0.6;
	private static final double SPEED_LIMIT = SPEED_FORWARD;
	private static final double SPEED_LIMIT_RUN = SPEED_RUN;
	private static final double SPEED_STRAFE = 0.15;
	private static final Vector2i mousePos = new Vector2i();
	public static IterableControlSet inGameControls = new IterableControlSet();
	public static IterableControlSet uiControls = new IterableControlSet();
	public static final IterableControlSet[] controlSets = {inGameControls, uiControls};
	private static boolean inputGrabbed = false;
	public static MouseLook mouseLook = new MouseLook();
	private static Vector3d playerVelocityVectorSum = new Vector3d();
	private static Vector3d lookedAt;
	private static CollisionResult collision;
	private static boolean playerIsRunning;
	
	private static void grab() {
		System.out.println("CONTROLS ARE GRABBED");
		Controls.grabControl.lock();
		inputGrabbed = !inputGrabbed;
		GLFW.glfwSetInputMode(GlowTest.window.handle(), GLFW.GLFW_CURSOR,
				inputGrabbed ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
	}
	
	/**
	 * Should be called at one specific point in the main game loop and nowhere else.
	 */
	public static void handleControls() { // MAIN LOOP
		uiControls.pollAll(); // polled regardless of if the input is grabbed
		
		if (inputGrabbed) {
			inGameControls.pollAll(); // polled only if input is grabbed
			
			playerVelocityVectorSum = new Vector3d();
			
			if (playerVelocityVectorSum.length() > (playerIsRunning ? SPEED_LIMIT_RUN : SPEED_LIMIT)) {
				playerVelocityVectorSum.normalize().mul(playerIsRunning ? SPEED_LIMIT_RUN : SPEED_LIMIT);
			}
			
			MasterRenderer.scene.getCamera().setPosition(playerVelocityVectorSum.add(MasterRenderer.scene.getCamera().getPosition(null)));
			
			mouseLook.step(mousePos.x, mousePos.y, MasterRenderer.windowWidth, MasterRenderer.windowHeight);
			MasterRenderer.scene.getCamera().setOrientation(ControlsHandler.mouseLook.getMatrix());
			
			Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
			collision = new CollisionResult();
			lookedAt = Collision.raycastVoxel(MasterRenderer.scene.getCamera().getPosition(null), lookVec, 100, GlowTest.getChunkManager()::getShape, collision, false);
			if (lookedAt != null) {
				//lookTargetActor.setPosition(collision.getHitLocation());
				Actors.lookTargetActor.setPosition(collision.getVoxelCenter(null));
				
				Vector3d hitNormal = collision.getHitNormal();
				Actors.lookTargetActor.lookAlong(hitNormal.x, hitNormal.y, hitNormal.z);
				Actors.lookTargetActor.setRenderModel(Actors.bakedLookTarget);
			} else {
				Actors.lookTargetActor.setRenderModel(null);
			}
		}
	}
	
	public static void mapControls() { // STAGE 1
		// TODO: Create a player class and move movement shit over there
		Controls.grabControl = uiControls.map("grab", GLFW.GLFW_KEY_TAB);
		Controls.grabControl.onPress().register(ControlsHandler::grab);
		
		uiControls.map("graceful_exit", GLFW.GLFW_KEY_ESCAPE).onPress().register(() ->
				GLFW.glfwSetWindowShouldClose(GlowTest.window.handle(), true));
		
		inGameControls.map("forward", GLFW.GLFW_KEY_W).onPress().register(() -> {
			Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
			if (playerIsRunning) {
				lookVec.mul(SPEED_RUN);
			} else {
				lookVec.mul(SPEED_FORWARD);
			}
			playerVelocityVectorSum.add(lookVec);
		});
		inGameControls.map("backward", GLFW.GLFW_KEY_S).onPress().register(() -> {
			Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
			if (playerIsRunning) {
				lookVec.mul(-SPEED_RUN);
			} else {
				lookVec.mul(-SPEED_FORWARD);
			}
			
			playerVelocityVectorSum.add(lookVec);
		});
		inGameControls.map("left", GLFW.GLFW_KEY_A).onPress().register(() -> {
			Vector3d leftVec = ControlsHandler.mouseLook.getRightVector(null);
			leftVec.mul(-SPEED_STRAFE);
			
			playerVelocityVectorSum.add(leftVec);
		});
		inGameControls.map("right", GLFW.GLFW_KEY_D).onPress().register(() -> {
			Vector3d rightVec = ControlsHandler.mouseLook.getRightVector(null);
			rightVec.mul(SPEED_STRAFE);
			
			playerVelocityVectorSum.add(rightVec);
		});
		
		Controls.runControl = inGameControls.map("run", GLFW.GLFW_KEY_LEFT_SHIFT);
		Controls.runControl.onPress().register(() -> playerIsRunning = true);
		Controls.runControl.onRelease().register(() -> playerIsRunning = false);
		
		Controls.breakControl = inGameControls.mapMouse("break", GLFW.GLFW_MOUSE_BUTTON_LEFT);
		Controls.placeControl = inGameControls.mapMouse("place", GLFW.GLFW_MOUSE_BUTTON_RIGHT);
		Controls.breakControl.onPress().register(() -> {
			Controls.breakControl.lock();
			if (lookedAt != null) {
				Vector3d voxelCenter = new Vector3d();
				collision.getVoxelCenter(voxelCenter);
				GlowTest.getChunkManager().setBlock((int) voxelCenter.x, (int) voxelCenter.y, (int) voxelCenter.z,
						Blocks.BLOCK_AIR);
			}
		});
		Controls.placeControl.onPress().register(() -> {
			if (!Controls.breakControl.isActive()) {
				ControlsHandler.inGameControls.lock("activate");
				if (lookedAt != null) {
					Vector3d voxelCenter = new Vector3d();
					collision.getVoxelCenter(voxelCenter).add(collision.getHitNormal());
					GlowTest.getChunkManager().setBlock((int) voxelCenter.x, (int) voxelCenter.y, (int) voxelCenter.z,
							Blocks.BLOCK_ORANGE);
				}
			}
		});
	}
	
	public static void setCallbacks() { // STAGE 2
		for (ControlSet set : controlSets) {
			GLFW.glfwSetKeyCallback(GlowTest.window.handle(), (w, k, sc, act, mods) -> set.handleKey(k, sc, act, mods));
			GLFW.glfwSetMouseButtonCallback(GlowTest.window.handle(), (w, btn, act, mods) -> set.handleMouse(btn, act, mods));
		}
		
		GLFW.glfwSetCursorPosCallback(GlowTest.window.handle(), (hWin, x, y) -> mousePos.set((int) x, (int) y));
	}
	
	public static class IterableControlSet extends ControlSet {
		protected void pollAll() {
			this.controls.values().iterator().forEachRemaining((control) -> {
				if (control.isActive()) control.onPress().fire();
			});
		}
	}
}
