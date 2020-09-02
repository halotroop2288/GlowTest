package blue.endless.glow;

import com.playsawdust.chipper.glow.RenderScheduler;
import com.playsawdust.chipper.glow.Window;
import com.playsawdust.chipper.glow.gl.Texture;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.scene.Light;
import com.playsawdust.chipper.glow.scene.Scene;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class MasterRenderer {
	public static final BufferedImage MISSINGNO;
	public static boolean windowSizeDirty = false;
	public static int windowWidth, windowHeight;
	public static Scene scene = new Scene();
	public static RenderScheduler scheduler = RenderScheduler.createDefaultScheduler();
	static ShaderProgram shaderProg;
	static Window window;
	
	static {
		MISSINGNO = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 256; x++) {
				int p = (x / 32 + y / 32) % 2;
				
				if (p == 0) {
					MISSINGNO.setRGB(x, y, 0xFF_000000);
				} else {
					MISSINGNO.setRGB(x, y, 0xFF_FF00FF);
				}
			}
		}
	}
	
	public static BufferedImage loadImage(String name) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(Objects.requireNonNull(GlowTest.class.getClassLoader()
					.getResourceAsStream("textures/" + name + ".png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image == null ? MISSINGNO : image;
	}
	
	public static Texture loadTexture(String fileName, String id) {
		Texture texture = Texture.of(loadImage(fileName));
		scheduler.registerTexture(id, texture);
		return texture;
	}
	
	public static Texture loadTexture(String id, BufferedImage image) {
		Texture texture = Texture.of(image);
		scheduler.registerTexture(id, texture);
		return texture;
	}
	
	public static void setupStage1() {
		/* Start GL, spawn up a window, load and compile the ShaderProgram, and attach it to the solid MeshPass. */
		
		Window.initGLFW();
		
		MasterRenderer.window = new Window(1024, 768, "Test");
		
		GLFW.glfwMakeContextCurrent(MasterRenderer.window.handle());
		
		GL.createCapabilities();
	}
	
	public static void setupStage2() {
		/* Set the clear color, set global GL state, and start the render loop */
		GL11.glClearColor(0.39f, 0.74f, 1.0f, 0.0f);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL20.GL_MULTISAMPLE);
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		//Matrix4d projection = new Matrix4d();
		Vector2d windowSize = new Vector2d();
		MasterRenderer.window.getSize(windowSize);
		MasterRenderer.windowWidth = (int) windowSize.x();
		MasterRenderer.windowHeight = (int) windowSize.y();
		MasterRenderer.windowSizeDirty = true;
		MasterRenderer.shaderProg.bind();
		
		Light sun = scene.getSun();
		sun.setRadius(4096);
		sun.setPosition(5 * 32, 5 * 32, 5 * 32);
		
		scene.getCamera().setPosition(32 * 4, 128, 32 * 4);
	}
	
	public static void render() {
		scene.render(scheduler, shaderProg);
		
		GLFW.glfwSwapBuffers(MasterRenderer.window.handle());
		
		GLFW.glfwPollEvents();
	}
	
	public static void destroy() {
		scheduler.destroy();
		shaderProg.destroy();
	}
}
