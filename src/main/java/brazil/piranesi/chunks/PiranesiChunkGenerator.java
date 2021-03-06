package brazil.piranesi.chunks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import brazil.piranesi.Piranesi;
import brazil.piranesi.biome.PiranesiBiomeProvider;
import brazil.piranesi.blockpopulation.PiranesiTreePopulator;
import brazil.piranesi.configurator.WorldGenConfiguration;
import brazil.piranesi.noise.FastNoiseLite;
import brazil.piranesi.noise.NoiseConstraint;


public class PiranesiChunkGenerator extends ChunkGenerator {

	/*@Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
		// TODO invalid response
        return Arrays.asList((BlockPopulator) new PiranesiTreePopulator());
    }*/
	
	@Override
	public BiomeProvider getDefaultBiomeProvider(WorldInfo info)
	{
		return new PiranesiBiomeProvider();
	}
    
	@Override
	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome)
	{
		
		Random r = new Random();

		/**
		 * # BIOMEDATA
		 * BiomeData is a noise profile that has information about
		 * WHERE biomes are. It is a large map of Biomes.
		 * It is always the 0th index of the NoiseCache.
		 * 
		 * The BIOMEDATA is consistently within range -1 to 1,
		 * unless there is a frequency greater than 1.
		 * 
		 * The default frequency is 2/1000 (0.002).
		 */
		
		FastNoiseLite biomeData = WorldGenConfiguration.layerData.get(0).getNoiseProfile().getNoise();
		
		/**
		 * # WORLD SHAPE
		 * The following will collect all other instances
		 * of NoiseCache with a total of NoiseCache.size();
		 */
		FastNoiseLite noise = WorldGenConfiguration.layerData.get(1).getNoiseProfile().getNoise();
		
		/**
		 * # FIND NOISE CONSTRAINTS
		 * The following will calculate the noise limits (constraints of height)
		 * of the base terrain layer's noise. It will be based off of the first 10
		 * chunks that are generated from a left-to-right perspective.
		 * 
		 * This new constraint method IS 100% because it is on a per-chunk basis.
		 */
		
		NoiseConstraint constraint = new NoiseConstraint(noise, chunkX, chunkZ);
		float min = constraint.getMinimum();
		float max = constraint.getMaximum();
		
		/* Init Defaults */
		
		int currentHeight = 70;
		
	    final int SEA_LEVEL = 20;
		
		//Chunk Generator
		ChunkData chunk = createChunkData(world);
		
		Bisected topGrassData = (Bisected) Material.TALL_GRASS.createBlockData();
		topGrassData.setHalf(Half.TOP);
		
		for (int X = 0; X < 16; X++)
		{
			for (int Z = 0; Z < 16; Z++)
			{
				
				float biomeValue = biomeData.GetNoise(chunkX*16+X, chunkZ*16+Z);
				
				float pointValue = noise.GetNoise(chunkX*16+X, chunkZ*16+Z);

				currentHeight = (int) (Math.abs(pointValue) * 100);
				
				Material currentMaterial = Material.BLUE_ICE;
				
				
				// -1.6 to 0
				
				// so the percent is the second numbe
				
				/*
				 * # Example:
				 * 
				 * `pointvalie > max * 0.0625`
				 * 0.0625 = percent, 6.25% bottom is going to be the sand
				 * 
				 */
				//  -1.4102119125 : -1.2984704
				// -1.3953264 * 1.125 = -1.5697422
				if (currentHeight < 0)
				{
					currentMaterial = Material.GRAVEL;
				}
				else if (currentHeight < 20)
				{
					currentMaterial = Material.SAND;
				}
				else
				{
					currentMaterial = Material.GRASS_BLOCK;
					Double d = r.nextDouble();
					if (d < 0.15)
					{
						chunk.setBlock(X, currentHeight, Z, Material.PINK_TULIP);
					}
					else if (d < 0.25)
					{
						chunk.setBlock(X, currentHeight, Z, Material.FERN);
					}
					else if (d < 0.35)
					{
						chunk.setBlock(X, currentHeight, Z, Material.TALL_GRASS);
						chunk.setBlock(X, currentHeight+1, Z, topGrassData);
					}
					else if (d < 0.6)
					{
						chunk.setBlock(X, currentHeight, Z, Material.GRASS);
					}
				}
				
				
				for (int i = 1; i <= currentHeight; i++)
				{
					chunk.setBlock(X, i, Z, currentMaterial);
					if (currentHeight < SEA_LEVEL)
					{
						for (int s = currentHeight; s < SEA_LEVEL; s++)
						{
							chunk.setBlock(X, s, Z, Material.WATER);
						}
					}
				}
				
				chunk.setBlock(X, 0, Z, Material.BEDROCK);
			}
		}
		
		return chunk;
	}

	public static int getNoiseTopPosition(int posX, int posZ, World world)
	{
		FastNoiseLite noise = WorldGenConfiguration.layerData.get(1).getNoiseProfile().getNoise();
		int height = (int) (Math.abs(noise.GetNoise(posX, posZ) * 100));
		/*if (height <= 86)
		{
			return 300;
		}*/
		return height;
	}
	
	@Override
	public List<BlockPopulator> getDefaultPopulators(World world)
	{
		List<BlockPopulator> populators = new ArrayList<BlockPopulator>();
		populators.add(new PiranesiTreePopulator()); // trees
		return populators;
	}
	
	/* For certain server softwares, prevent vanilla generation entirely */
	
	@Override
	public boolean shouldGenerateNoise() { return false; }
	
	@Override
	public boolean shouldGenerateSurface() { return false; }
	
	@Override
	public boolean shouldGenerateBedrock() { return false; }
	
	@Override
	public boolean shouldGenerateCaves() { return false; }
	
	@Override
	public boolean shouldGenerateDecorations() { return false; }
	
	@Override
	public boolean shouldGenerateMobs() { return false; }
	
	@Override
	public boolean shouldGenerateStructures() { return false; }
}