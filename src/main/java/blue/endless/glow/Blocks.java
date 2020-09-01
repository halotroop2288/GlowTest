package blue.endless.glow;

import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.voxel.VoxelShape;
import org.joml.Vector3d;

public class Blocks {
	private static final Material.Generic MATERIAL_STONE = new Material.Generic()
			.with(MaterialAttribute.DIFFUSE_COLOR, new Vector3d(1, 1, 1))
			.with(MaterialAttribute.SPECULARITY, 0.01)
			.with(MaterialAttribute.DIFFUSE_TEXTURE_ID, "stoneDiffuse")
			.with(MaterialAttribute.EMISSIVITY, 0.0);
	private static final Material.Generic MATERIAL_GRASS = new Material.Generic()
			.with(MaterialAttribute.DIFFUSE_COLOR, new Vector3d(1, 1, 1))
			.with(MaterialAttribute.SPECULARITY, 0.01)
			.with(MaterialAttribute.DIFFUSE_TEXTURE_ID, "grassDiffuse")
			.with(MaterialAttribute.EMISSIVITY, 0.0);
	private static final Material.Generic MATERIAL_ORANGE = new Material.Generic()
			.with(MaterialAttribute.DIFFUSE_COLOR, new Vector3d(1, 1, 1))
			.with(MaterialAttribute.SPECULARITY, 0.01)
			.with(MaterialAttribute.DIFFUSE_TEXTURE_ID, "orangeDiffuse")
			.with(MaterialAttribute.EMISSIVITY, 0.0);
	
	public static Block BLOCK_AIR = new Block().setShape(VoxelShape.EMPTY);
	static final Block BLOCK_GRASS = new Block()
			.setShape(VoxelShape.CUBE)
			.setMaterial(MATERIAL_GRASS);
	static final Block BLOCK_STONE = new Block()
			.setShape(VoxelShape.CUBE)
			.setMaterial(MATERIAL_STONE);
	static final Block BLOCK_ORANGE = new Block()
			.setShape(VoxelShape.CUBE)
			.setMaterial(MATERIAL_ORANGE);
}
