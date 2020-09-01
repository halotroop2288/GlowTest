package blue.endless.glow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class MasterRenderer {
	public static final BufferedImage MISSINGNO;
	public static boolean windowSizeDirty = false;
	public static int windowWidth = 0;
	public static int windowHeight = 0;
	
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
}
