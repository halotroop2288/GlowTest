package com.halotroop.brightcraft.physics.entities;

import com.halotroop.brightcraft.Timer;
import com.halotroop.brightcraft.config.controls.ControlsHandler;
import org.joml.Vector3d;

public class Player extends VisibleEntity implements Timer.Tickable {
	public static final double SPEED_FORWARD = 0.2;
	public static final double SPEED_LIMIT = SPEED_FORWARD;
	public static final double SPEED_STRAFE = SPEED_FORWARD * 0.75;
	public static final double SPEED_RUN = SPEED_FORWARD * 3;
	public static final double SPEED_LIMIT_RUN = SPEED_RUN;
	public boolean isRunning;
	public Vector3d velocityVectorSum = new Vector3d();
	
	public Player() {
	
	}
	
	@Override
	public void tick() {
	
	}
	
	public void handleMovement(MovementDirection direction) {
		switch (direction) {
			default: {
				break;
			}
			case LEFT: {
				Vector3d leftVec = ControlsHandler.mouseLook.getRightVector(null);
				leftVec.mul(-Player.SPEED_STRAFE);
				
				velocityVectorSum.add(leftVec);
			}
			case RIGHT: {
				Vector3d rightVec = ControlsHandler.mouseLook.getRightVector(null);
				rightVec.mul(Player.SPEED_STRAFE);
				
				velocityVectorSum.add(rightVec);
			}
			case FORWARD: {
				Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
				if (isRunning) {
					lookVec.mul(Player.SPEED_RUN);
				} else {
					lookVec.mul(Player.SPEED_FORWARD);
				}
				velocityVectorSum.add(lookVec);
			}
			case BACKWARD: {
				Vector3d lookVec = ControlsHandler.mouseLook.getLookVector(null);
				if (isRunning) {
					lookVec.mul(-Player.SPEED_RUN);
				} else {
					lookVec.mul(-Player.SPEED_FORWARD);
				}
				
				velocityVectorSum.add(lookVec);
			}
		}
	}
	
	public enum MovementDirection {
		LEFT, RIGHT, FORWARD, BACKWARD
	}
}
