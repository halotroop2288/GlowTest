package com.halotroop.brightcraft.config.controls;

import com.halotroop.brightcraft.Actors;
import com.halotroop.brightcraft.Blocks;
import com.halotroop.brightcraft.GlowTest;
import com.halotroop.brightcraft.MasterRenderer;
import com.playsawdust.chipper.glow.control.ControlSet;
import com.playsawdust.chipper.glow.control.DigitalButtonControl;
import com.playsawdust.chipper.glow.control.MouseLook;
import com.playsawdust.chipper.glow.scene.Collision;
import com.playsawdust.chipper.glow.scene.CollisionResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;

public class ControlsHandler {
	private static final double SPEED_FORWARD = 0.2;
	private static final double SPEED_RUN = 0.6;
	private static final double SPEED_LIMIT = SPEED_FORWARD;
	private static final double SPEED_LIMIT_RUN = SPEED_RUN;
	private static final double SPEED_STRAFE = 0.15;
	public static MouseLook mouseLook = new MouseLook();
	public static IterableControlSet movementControls = new IterableControlSet();
	static int mouseX, mouseY;
	static boolean inputGrabbed = false;
	static Vector3d playerVelocityVectorSum = new Vector3d();
	private static Vector3d lookedAt;
	private static CollisionResult collision;
	
	static void grab() {
		System.out.println("CONTROLS ARE GRABBED");
		Controls.grabControl.lock();
		inputGrabbed = !inputGrabbed;
		GLFW.glfwSetInputMode(GlowTest.window.handle(), GLFW.GLFW_CURSOR,
				inputGrabbed ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
	}
	
	public static void handleControls() {
		if (inputGrabbed) {
			for (DigitalButtonControl control : movementControls) {
				if (control.isActive()) {
					control.onPress().fire();
				}
			}
			
			playerVelocityVectorSum = new Vector3d();
			
			if (!movementControls.isActive("run") && playerVelocityVectorSum.length() > SPEED_LIMIT) {
				playerVelocityVectorSum.normalize().mul(SPEED_LIMIT);
			}
			
			MasterRenderer.scene.getCamera().setPosition(playerVelocityVectorSum.add(MasterRenderer.scene.getCamera().getPosition(null)));
			
			mouseLook.step(ControlsHandler.mouseX, ControlsHandler.mouseY, MasterRenderer.windowWidth, MasterRenderer.windowHeight);
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
		Controls.grabControl = movementControls.map("grab", GLFW.GLFW_KEY_TAB);
		Controls.grabControl.onPress().register(ControlsHandler::grab);
		
		movementControls.map("forward", GLFW.GLFW_KEY_W).onPress().register(() -> {
			Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
			if (movementControls.isActive("run")) {
				lookVec.mul(SPEED_RUN);
			} else {
				lookVec.mul(SPEED_FORWARD);
			}
			playerVelocityVectorSum.add(lookVec);
		});
		movementControls.map("backward", GLFW.GLFW_KEY_S).onPress().register(() -> {
			Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
			if (movementControls.isActive("run")) {
				lookVec.mul(-SPEED_RUN);
			} else {
				lookVec.mul(-SPEED_FORWARD);
			}
			
			playerVelocityVectorSum.add(lookVec);
		});
		movementControls.map("left", GLFW.GLFW_KEY_A).onPress().register(() -> {
			Vector3d leftVec = ControlsHandler.mouseLook.getRightVector(null);
			leftVec.mul(-SPEED_STRAFE);
			
			playerVelocityVectorSum.add(leftVec);
		});
		movementControls.map("right", GLFW.GLFW_KEY_D).onPress().register(() -> {
			Vector3d rightVec = ControlsHandler.mouseLook.getRightVector(null);
			rightVec.mul(SPEED_STRAFE);
			
			playerVelocityVectorSum.add(rightVec);
		});
		
		movementControls.map("run", GLFW.GLFW_KEY_LEFT_SHIFT).onPress().register(() -> {
			if (playerVelocityVectorSum.length() > SPEED_LIMIT_RUN) {
				playerVelocityVectorSum.normalize().mul(SPEED_LIMIT_RUN);
			}
		});
		Controls.breakControl = movementControls.mapMouse("break", GLFW.GLFW_MOUSE_BUTTON_LEFT);
		Controls.placeControl = movementControls.mapMouse("place", GLFW.GLFW_MOUSE_BUTTON_RIGHT);
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
				ControlsHandler.movementControls.lock("activate");
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
		GLFW.glfwSetKeyCallback(GlowTest.window.handle(), (win, key, scancode, action, mods) -> {
			if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
				GLFW.glfwSetWindowShouldClose(GlowTest.window.handle(), true);
			movementControls.handleKey(key, scancode, action, mods);
		});
		
		GLFW.glfwSetCursorPosCallback(GlowTest.window.handle(), (hWin, x, y) -> {
			mouseX = (int) x;
			mouseY = (int) y;
		});
		
		GLFW.glfwSetMouseButtonCallback(GlowTest.window.handle(), (hWin, button, action, mods) ->
				movementControls.handleMouse(button, action, mods));
	}
	
	public static class IterableControlSet extends ControlSet implements Iterable<DigitalButtonControl> {
		@Override
		@NonNull
		public Iterator<DigitalButtonControl> iterator() {
			return this.controls.values().iterator();
		}
	}
}
