package com.halotroop.brightcraft;

import com.playsawdust.chipper.glow.gl.shader.Destroyable;
import com.playsawdust.chipper.glow.voxel.VoxelShape;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.lwjgl.stb.STBPerlin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ChunkManager implements Destroyable {
	static final ArrayList<Vector3i> pendingChunkList = new ArrayList<>();
	private static final int maxRenderDist = 32; //Just over 2GiB used for chunk management at 2,197,000 bytes
	private final int renderDist = 12;
	private final int mapSize = renderDist * 2 + 1;
	
	private Chunk[] chunks = new Chunk[mapSize * mapSize * mapSize];
	
	private int xofs = 0;
	private int yofs = 0;
	private int zofs = 0;
	
	static void bakeOne() {
		boolean allEmpty = true;
		
		while (allEmpty && !pendingChunkList.isEmpty()) {
			Vector3i chunkPos = pendingChunkList.remove(0);
			if (chunkPos.x < 0 || chunkPos.y < 0 || chunkPos.z < 0)
				return; //Skip negative positions, this is a quick and dirty game
			Chunk chunk = Chunk.create();
			chunk.setPosition(new Vector3d(chunkPos.x * 32.0, chunkPos.y * 32.0, chunkPos.z * 32.0));
			//System.out.println("Added chunk at "+chunk.getPosition(null));
			generateInto(chunk);
			GlowTest.getChunkManager().set(chunkPos.x, chunkPos.y, chunkPos.z, chunk);
			if (!chunk.isEmpty()) {
				allEmpty = false;
				chunk.bake();
				MasterRenderer.scene.addActor(chunk);
			} // else { /*Do stuff*/}
		}
	}
	
	private static void generateInto(Chunk chunk) {
		//Preload the palette
		chunk.setBlock(0, 0, 0, Blocks.BLOCK_AIR);
		chunk.setBlock(0, 0, 0, Blocks.SLIME_BLOCK);
		chunk.setBlock(0, 0, 0, Blocks.BLOCK_STONE);
		chunk.setBlock(0, 0, 0, Blocks.BLOCK_GRASS);
		chunk.setBlock(0, 0, 0, Blocks.BLOCK_AIR);
		
		if (chunk.getY() > 128) return;
		
		//TODO: We could accelerate this considerably by intentionally setting the patch palette and then filling in integers directly
		for (int z = 0; z < 32; z++) {
			for (int x = 0; x < 32; x++) {
				int wx = x + chunk.getX();
				int wz = z + chunk.getZ();
				int wy = chunk.getY();
				
				int terrainHeight = (int) (STBPerlin.stb_perlin_ridge_noise3(wx * 0.003f, 0, wz * 0.003f, 2.0f, 0.5f, 1.0f, 3) * 128.0 + 8.0);
				
				Block surface = (terrainHeight > 64) ? Blocks.BLOCK_STONE : Blocks.BLOCK_GRASS;
				Block interior = Blocks.BLOCK_STONE;
				
				for (int y = 0; y < 32; y++) {
					if (wy + y > terrainHeight) break;
					
					Block cur = (wy + y < terrainHeight - 32) ? interior : surface;
					
					if (wy + y <= terrainHeight) chunk.setBlock(x, y, z, cur);
				}
			}
		}
	}
	
	public void resize(int renderDist) {
		int newMapSize = renderDist * 2 + 1;
		Chunk[] newChunks = new Chunk[newMapSize * newMapSize * newMapSize];
		
		//TODO: copy chunks over
		
		chunks = newChunks;
	}
	
	public void pan(int dx, int dy, int dz) {
		Chunk[] newChunks = new Chunk[mapSize * mapSize * mapSize];
		for (int y = 0; y < mapSize; y++) {
			for (int z = 0; z < mapSize; z++) {
				for (int x = 0; x < mapSize; x++) {
					int destOfs = chunkCoordOfs(x, y, z);
					int srcOfs = chunkCoordOfs(x + dx, y + dy, z + dz);
					if (srcOfs == -1) {
						newChunks[destOfs] = null;
					} else {
						newChunks[destOfs] = chunks[srcOfs];
					}
				}
			}
		}
		
		//TODO: Destroy old chunks
		Arrays.fill(chunks, null); // May be unnecessary but it helps the GC to untangle fewer refs
		chunks = newChunks;
		xofs += dx;
		yofs += dy;
		zofs += dz;
	}
	
	private int chunkCoordOfs(int x, int y, int z) {
		x -= xofs;
		y -= yofs;
		z -= zofs;
		
		if (x < 0 || x >= mapSize || y < 0 || y >= mapSize || z < 0 || z >= mapSize) return -1;
		return x + (mapSize * z) + (mapSize * mapSize * y); //xzy, so that you can grab x slivers and arraycopy them around to pan in the x direction, or grab whole xz slabs and arraycopy them to pan in the z direction
	}
	
	public void set(int x, int y, int z, Chunk chunk) {
		int ofs = chunkCoordOfs(x, y, z);
		if (ofs != -1) chunks[ofs] = chunk;
	}
	
	
	public void scheduleAll() {
		for (int y = 0; y < mapSize; y++) {
			for (int z = 0; z < mapSize; z++) {
				for (int x = 0; x < mapSize; x++) {
					int ofs = chunkCoordOfs(x + xofs, y + yofs, z + zofs);
					if (ofs != -1) {
						if (chunks[ofs] == null) {
							pendingChunkList.add(new Vector3i(x + xofs, y + yofs, z + zofs));
						}
					}
				}
			}
		}
		pendingChunkList.sort(getCenterComparator());
	}
	
	//public void getSchedulableChunks<List<Chunk> list) {
	
	//}
	
	public VoxelShape getShape(int x, int y, int z) {
		int chunkX = x / 32;
		int chunkY = y / 32;
		int chunkZ = z / 32;
		int ofs = chunkCoordOfs(chunkX, chunkY, chunkZ);
		if (ofs == -1) return VoxelShape.EMPTY;
		Chunk chunk = chunks[ofs];
		if (chunk == null) return VoxelShape.EMPTY;
		Block block = chunk.getBlock(x % 32, y % 32, z % 32);
		if (block == null) return VoxelShape.EMPTY;
		return block.getShape();
	}
	
	public void setBlock(int x, int y, int z, Block block) {
		int chunkX = x / 32;
		int chunkY = y / 32;
		int chunkZ = z / 32;
		int ofs = chunkCoordOfs(chunkX, chunkY, chunkZ);
		if (ofs == -1) return;
		Chunk chunk = chunks[ofs];
		if (chunk == null) {
			chunk = Chunk.create();
		}
		chunk.setBlock(x % 32, y % 32, z % 32, block);
		chunk.mesh();
		chunk.bake();
	}
	
	public Comparator<Vector3i> getCenterComparator() {
		Vector3dc MAP_CENTER = new Vector3d((xofs + renderDist + 0.5) * 32, (yofs + renderDist + 0.5) * 32, (zofs + renderDist + 0.5) * 32);
		return (a, b) -> (int) (MAP_CENTER.distanceSquared(new Vector3d(b)) - MAP_CENTER.distanceSquared(new Vector3d(a)));
	}
	
	@Override
	public void destroy() {
		for (int i = 0; i < chunks.length; i++) {
			if (chunks[i] != null) {
				chunks[i].destroy();
				chunks[i] = null;
			}
		}
	}
}
