package com.halotroop.brightcraft;

import com.playsawdust.chipper.glow.RenderScheduler;
import com.playsawdust.chipper.glow.Window;
import com.playsawdust.chipper.glow.gl.Texture;
import com.playsawdust.chipper.glow.gl.shader.ShaderError;
import com.playsawdust.chipper.glow.gl.shader.ShaderIO;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.pass.MeshPass;
import com.playsawdust.chipper.glow.scene.Scene;
import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MasterRenderer {
	public static final BufferedImage MISSINGNO;
	public static final double SIXTY_DEGREES = 60.0 * (Math.PI / 180.0);
	public static boolean windowSizeDirty = false;
	public static int windowWidth, windowHeight;
	public static Scene scene = new Scene();
	public static RenderScheduler scheduler = RenderScheduler.createDefaultScheduler();
	static ShaderProgram solidShaderProg;
	
	static {
		MISSINGNO = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 256; x++) {
				int p = (x / 32 + y / 32) % 2;
				
				if (p == 0) {
					MISSINGNO.setRGB(x, y, 0xFF_00_00_00);
				} else {
					MISSINGNO.setRGB(x, y, 0xFF_FF_00_FF);
				}
			}
		}
	}
	
	/**
	 * Loads an image from the textures folder.
	 *
	 * @param fileName the fileName, or path relative to the textures folder,
	 *                 of the image file you want to load (Must be a PNG!)
	 * @return A BufferedImage representation of that image file.
	 */
	public static BufferedImage loadImage(String fileName) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(Objects.requireNonNull(GlowTest.class.getClassLoader()
					.getResourceAsStream("textures/" + fileName + ".png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image == null ? MISSINGNO : image;
	}
	
	/**
	 * Registers and returns a Texture created from an image file.
	 *
	 * @param id       the unique string id of the texture for the scheduler registry.
	 * @param fileName the name, or path relative to the textures folder, of the image file you want to load (Must be a PNG!)
	 * @return a Texture object of the requested image file.
	 */
	public static Texture loadTexture(String id, String fileName) {
		return loadTexture(id, loadImage(fileName));
	}
	
	/**
	 * Registers a texture and returns it.
	 *
	 * @param id    the unique string id of the texture for the scheduler registry.
	 * @param image the pre-generated BufferedImage
	 * @return a Texture object of the supplied BufferedImage object.
	 */
	public static Texture loadTexture(String id, BufferedImage image) {
		Texture texture = Texture.of(image);
		scheduler.registerTexture(id, texture);
		return texture;
	}
	
	public static void initWindow() {
		/* Start GL, spawn up a window, load and compile the ShaderProgram, and attach it to the solid MeshPass. */
		
		Window.initGLFW();
		
		GlowTest.window = new Window(1280, 720, "GlowTest");
		
		GLFW.glfwMakeContextCurrent(GlowTest.window.handle());
	}
	
	public static void setupStage2() throws IOException, ShaderError {
		GL.createCapabilities();
		
		GLFW.glfwSetFramebufferSizeCallback(GlowTest.window.handle(), (hWin, width, height) -> {
			windowSizeDirty = true;
			windowWidth = width;
			windowHeight = height;
		});
		
		if (MasterRenderer.windowSizeDirty) {
			GL11.glViewport(0, 0, MasterRenderer.windowWidth, MasterRenderer.windowHeight);
			Matrix4d projection = new Matrix4d();
			projection.setPerspective(SIXTY_DEGREES, MasterRenderer.windowWidth / (double) MasterRenderer.windowHeight, 0.01, 1000);
			MasterRenderer.scene.setProjectionMatrix(projection);
		}
		
		solidShaderProg = loadShaderProgram("solid");
	}
	
	public static void setupStage3() {
		/* Set the clear color, set global GL state, and start the render loop */
		GL11.glClearColor(0.39f, 0.74f, 1.0f, 0.0f);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL20.GL_MULTISAMPLE);
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		//Matrix4d projection = new Matrix4d();
		Vector2d windowSize = new Vector2d();
		GlowTest.window.getSize(windowSize);
		windowWidth = (int) windowSize.x();
		windowHeight = (int) windowSize.y();
		windowSizeDirty = true;
		solidShaderProg.bind();
		
		scene.getSun().setRadius(4096);
		scene.getSun().setPosition(5 * 32, 5 * 32, 5 * 32);
		
		scene.getCamera().setPosition(32 * 4, 128, 32 * 4);
	}
	
	public static void render() {
		scene.render(scheduler, solidShaderProg);
		
		GLFW.glfwSwapBuffers(GlowTest.window.handle());
		
		GLFW.glfwPollEvents();
	}
	
	public static void destroy() {
		if (scheduler != null) scheduler.destroy();
		else System.err.println("Scheduler was null!");
		if (solidShaderProg != null) solidShaderProg.destroy();
		else System.err.println("Solid shader program was null!");
	}
	
	public static ShaderProgram loadShaderProgram(String passName) throws IOException, ShaderError {
		ShaderProgram program;
		
		InputStream shaderStream = GlowTest.class.getClassLoader()
				.getResourceAsStream("shaders/" + passName + ".xml");
		program = ShaderIO.load(shaderStream);
		MeshPass pass = (MeshPass) scheduler.getPass(passName);
		pass.setShader(program);
		
		return program;
	}
}
