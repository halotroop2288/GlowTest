package com.halotroop.brightcraft.debug;

import com.playsawdust.chipper.glow.control.ControlSet;
import com.playsawdust.chipper.glow.control.DigitalButtonControl;
import org.lwjgl.glfw.GLFW;

/**
 * By default, Gradle will exclude this class from production JARs.
 * That means that players cannot use this to cheat, unless they really want to, and build the game themselves.
 * Of course, anyone could make a fork that disables that if they want,
 * but for the sake of integrity, I'd rather it not be the default.
 *
 * @author halotroop2288
 */
public class DebugControls {
	private static final ControlSet debugControls = new ControlSet();
	private static DigitalButtonControl debugF1, debugF2, debugF3, debugF4, debugF5, debugF6, debugF7, debugF8, debugF9,
			debugF10, debugF11, debugF12;
	
	public static void mapDebugControls() throws ClassNotFoundException {
		System.out.println("Hewwo world!");
		debugF1 = debugControls.map("debug_f1", GLFW.GLFW_KEY_F1);
		debugF2 = debugControls.map("debug_f2", GLFW.GLFW_KEY_F2);
		debugF3 = debugControls.map("debug_f3", GLFW.GLFW_KEY_F3);
		debugF4 = debugControls.map("debug_f4", GLFW.GLFW_KEY_F4);
		debugF5 = debugControls.map("debug_f5", GLFW.GLFW_KEY_F5);
		debugF6 = debugControls.map("debug_f6", GLFW.GLFW_KEY_F6);
		debugF7 = debugControls.map("debug_f7", GLFW.GLFW_KEY_F7);
		debugF8 = debugControls.map("debug_f8", GLFW.GLFW_KEY_F8);
		debugF9 = debugControls.map("debug_f9", GLFW.GLFW_KEY_F9);
		debugF10 = debugControls.map("debug_f10", GLFW.GLFW_KEY_F10);
		debugF11 = debugControls.map("debug_f11", GLFW.GLFW_KEY_F11);
		debugF12 = debugControls.map("debug_f12", GLFW.GLFW_KEY_F11);
	}
}
