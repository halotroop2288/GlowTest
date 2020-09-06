package com.halotroop.brightcraft.config.controls;

import com.halotroop.brightcraft.Blocks;
import com.halotroop.brightcraft.GlowTest;
import com.halotroop.brightcraft.MotherRenderer;
import com.halotroop.brightcraft.physics.entities.Actors;
import com.halotroop.brightcraft.physics.entities.Player;
import com.playsawdust.chipper.glow.control.ControlSet;
import com.playsawdust.chipper.glow.control.MouseLook;
import com.playsawdust.chipper.glow.scene.Collision;
import com.playsawdust.chipper.glow.scene.CollisionResult;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class ControlsHandler {
	private static final Vector2i mousePos = new Vector2i();
	public static ControlSet inGameControls = new ControlSet();
	public static ControlSet uiControls = new ControlSet();
	public static MouseLook mouseLook = new MouseLook();
	private static boolean inputGrabbed = false;
	private static Vector3d lookedAt;
	private static CollisionResult collision;
	
	private static void toggleGrab() {
		System.out.println("GRABBING CONTROLS");
		Controls.grabControl.lock();
		inputGrabbed = !inputGrabbed;
		GLFW.glfwSetInputMode(GlowTest.window.handle(), GLFW.GLFW_CURSOR,
				inputGrabbed ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
	}
	
	/**
	 * Should be called at one specific point in the main game loop and nowhere else.
	 */
	public static void handleControls() { // MAIN LOOP
		if (inputGrabbed) {
			GlowTest.player.velocityVectorSum = new Vector3d();
			
			final double CUR_SPEED_LIMIT = (GlowTest.player.isRunning ? Player.SPEED_LIMIT_RUN : Player.SPEED_LIMIT);
			
			// TODO: Move this to Player#tick!
			if (GlowTest.player.velocityVectorSum.length() > CUR_SPEED_LIMIT) {
				GlowTest.player.velocityVectorSum.normalize().mul(CUR_SPEED_LIMIT);
			}
			
			MotherRenderer.scene.getCamera().setPosition(GlowTest.player.velocityVectorSum.add(MotherRenderer.scene.getCamera().getPosition(null)));
			
			mouseLook.step(mousePos.x, mousePos.y, MotherRenderer.windowSize.x, MotherRenderer.windowSize.y);
			MotherRenderer.scene.getCamera().setOrientation(ControlsHandler.mouseLook.getMatrix());
			
			Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
			collision = new CollisionResult();
			lookedAt = Collision.raycastVoxel(MotherRenderer.scene.getCamera().getPosition(null), lookVec, 100, GlowTest.chunkManager::getShape, collision, false);
			if (lookedAt != null) {
				//lookTargetActor.setPosition(collision.getHitLocation());
				Actors.lookTargetActor.setPosition(collision.getVoxelCenter(null));
				
				Vector3d hitNormal = collision.getHitNormal();
				Actors.lookTargetActor.lookAlong(hitNormal.x, hitNormal.y, hitNormal.z);
				MotherRenderer.scene.forEach(actor -> {
					if (actor.equals(Actors.lookTargetActor)) MotherRenderer.scene.addActor(Actors.lookTargetActor);
				});
			} else {
				MotherRenderer.scene.removeActor(Actors.lookTargetActor);
			}
		}
	}
	
	public static void mapControls() { // STAGE 1
		System.out.println("Mapping controls");
		
		// UI Controls
		Controls.grabControl = uiControls.map("grab", GLFW.GLFW_KEY_TAB);
		Controls.grabControl.onPress().register(ControlsHandler::toggleGrab);
		
		// In-Game
		uiControls.map("graceful_exit", GLFW.GLFW_KEY_ESCAPE).onRelease().register(GlowTest::naturalExit);
		
		inGameControls.map("forward", GLFW.GLFW_KEY_W).onPress().register(
				() -> GlowTest.player.handleMovement(Player.MovementDirection.FORWARD));
		
		inGameControls.map("backward", GLFW.GLFW_KEY_S).onPress().register(
				() -> GlowTest.player.handleMovement(Player.MovementDirection.BACKWARD));
		
		inGameControls.map("left", GLFW.GLFW_KEY_A).onPress().register(
				() -> GlowTest.player.handleMovement(Player.MovementDirection.LEFT));
		
		inGameControls.map("right", GLFW.GLFW_KEY_D).onPress().register(
				() -> GlowTest.player.handleMovement(Player.MovementDirection.RIGHT));
		
		Controls.runControl = inGameControls.map("run", GLFW.GLFW_KEY_LEFT_SHIFT);
		Controls.runControl.onPress().register(() -> GlowTest.player.isRunning = true);
		Controls.runControl.onRelease().register(() -> GlowTest.player.isRunning = false);
		
		Controls.breakControl = inGameControls.mapMouse("break", GLFW.GLFW_MOUSE_BUTTON_LEFT);
		Controls.placeControl = inGameControls.mapMouse("place", GLFW.GLFW_MOUSE_BUTTON_RIGHT);
		
		Controls.breakControl.onPress().register(() -> {
			Controls.breakControl.lock();
			if (lookedAt != null) {
				Vector3d blockPos = new Vector3d();
				collision.getVoxelCenter(blockPos);
				GlowTest.chunkManager.setBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z,
						Blocks.BLOCK_AIR, true);
			}
		});
		
		Controls.placeControl.onPress().register(() -> {
			if (!Controls.breakControl.isActive()) {
				ControlsHandler.inGameControls.lock("activate");
				if (lookedAt != null) {
					Vector3d voxelCenter = new Vector3d();
					collision.getVoxelCenter(voxelCenter).add(collision.getHitNormal());
					GlowTest.chunkManager.setBlock((int) voxelCenter.x, (int) voxelCenter.y, (int) voxelCenter.z,
							Blocks.BLOCK_ORANGE, true);
				}
			}
		});
		
		setCallbacksFor(uiControls);
		GLFW.glfwSetCursorPosCallback(GlowTest.window.handle(), (hWin, x, y) -> mousePos.set((int) x, (int) y));
	}
	
	private static void setCallbacksFor(ControlSet... sets) {
		System.out.println("Setting callbacks for " + Arrays.toString(sets));
		GLFW.glfwSetKeyCallback(GlowTest.window.handle(), (window, key, scancode, action, mods) -> {
			for (ControlSet set : sets) set.handleKey(key, scancode, action, mods);
			System.out.println("Key " + GLFW.glfwGetKeyName(key, scancode) + "(" + key + ", " + scancode + ")"
					+ (action == GLFW.GLFW_PRESS ? " pressed" : " released"));
		});
		GLFW.glfwSetMouseButtonCallback(GlowTest.window.handle(), (w, btn, act, mods) -> {
			for (ControlSet set : sets) set.handleMouse(btn, act, mods);
		});
	}
}
