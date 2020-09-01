package blue.endless.glow;

import com.playsawdust.chipper.glow.control.ControlSet;
import com.playsawdust.chipper.glow.control.MouseLook;
import org.lwjgl.glfw.GLFW;

public class ControlsHandler {
	public static MouseLook mouseLook = new MouseLook();
	public static ControlSet movementControls = new ControlSet();
	
	public static void init() {
		movementControls.mapWASD();
		movementControls.map("grab", GLFW.GLFW_KEY_TAB);
		movementControls.map("run", GLFW.GLFW_KEY_LEFT_SHIFT);
		movementControls.mapMouse("punch", GLFW.GLFW_MOUSE_BUTTON_LEFT);
		movementControls.mapMouse("activate", GLFW.GLFW_MOUSE_BUTTON_RIGHT);
	}
}
