/*******************************************************************************
 * Copyright 2022, the Glitchfiend Team.
 * Creative Commons Attribution-NonCommercial-NoDerivatives 4.0.
 ******************************************************************************/
package terrablender.worldgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;
import net.minecraft.world.level.levelgen.material.WorldGenMaterialRule;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TBNoiseBasedChunkGenerator extends NoiseBasedChunkGenerator
{
    public static final Codec<TBNoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(RegistryLookupCodec.create(Registry.NOISE_REGISTRY).forGetter((instance) -> {
            return instance.noises;
        }), BiomeSource.CODEC.fieldOf("biome_source").forGetter((instance) -> {
            return instance.biomeSource;
        }), Codec.LONG.fieldOf("seed").stable().forGetter((instance) -> {
            return instance.seed;
        }), TBNoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((instance) -> {
            return instance.settings;
        })).apply(builder, builder.stable(TBNoiseBasedChunkGenerator::new));
    });

    protected TBNoiseSampler tbSampler;
    protected SurfaceSystem tbSurfaceSystem;

    public TBNoiseBasedChunkGenerator(Registry<NormalNoise.NoiseParameters> p_188609_, BiomeSource p_188610_, long p_188611_, Supplier<NoiseGeneratorSettings> p_188612_) {
        this(p_188609_, p_188610_, p_188610_, p_188611_, p_188612_);
    }

    private TBNoiseBasedChunkGenerator(Registry<NormalNoise.NoiseParameters> p_188614_, BiomeSource p_188615_, BiomeSource p_188616_, long p_188617_, Supplier<NoiseGeneratorSettings> p_188618_) {
        super(p_188614_, p_188616_, p_188617_, p_188618_);
        this.noises = p_188614_;
        this.seed = p_188617_;
        this.settings = p_188618_;
        NoiseGeneratorSettings noisegeneratorsettings = this.settings.get();
        this.defaultBlock = noisegeneratorsettings.getDefaultBlock();
        NoiseSettings noisesettings = noisegeneratorsettings.noiseSettings();
        this.tbSampler = new TBNoiseSampler(noisesettings, noisegeneratorsettings.isNoiseCavesEnabled(), p_188617_, p_188614_, noisegeneratorsettings.getRandomSource());
        ImmutableList.Builder<WorldGenMaterialRule> builder = ImmutableList.builder();
        builder.add(NoiseChunk::updateNoiseAndGenerateBaseState);
        builder.add(NoiseChunk::oreVeinify);
        this.materialRule = new MaterialRuleList(builder.build());
        Aquifer.FluidStatus aquifer$fluidstatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int i = noisegeneratorsettings.seaLevel();
        Aquifer.FluidStatus aquifer$fluidstatus1 = new Aquifer.FluidStatus(i, noisegeneratorsettings.getDefaultFluid());
        Aquifer.FluidStatus aquifer$fluidstatus2 = new Aquifer.FluidStatus(noisesettings.minY() - 1, Blocks.AIR.defaultBlockState());
        this.globalFluidPicker = (p_198228_, p_198229_, p_198230_) -> {
            return p_198229_ < Math.min(-54, i) ? aquifer$fluidstatus : aquifer$fluidstatus1;
        };
        this.tbSurfaceSystem = new SurfaceSystem(p_188614_, this.defaultBlock, i, p_188617_, noisegeneratorsettings.getRandomSource());

        // Nuke Vanilla's sampler and surface system to reduce memory usage.
        this.sampler = null;
        this.surfaceSystem = null;
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> p_197005_, Executor p_197006_, Blender p_197007_, StructureFeatureManager p_197008_, ChunkAccess p_197009_)
    {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            this.doCreateBiomes(p_197005_, p_197007_, p_197008_, p_197009_);
            return p_197009_;
        }), Util.backgroundExecutor());
    }

    private void doCreateBiomes(Registry<Biome> biomeRegistry, Blender blender, StructureFeatureManager structureManager, ChunkAccess chunkAccess)
    {
        TBNoiseChunk noiseChunk = this.getOrCreateNoiseChunk(chunkAccess, this.tbSampler, () -> {
            return new Beardifier(structureManager, chunkAccess);
        }, this.settings.get(), this.globalFluidPicker, blender);
        BiomeResolver biomeresolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.runtimeBiomeSource), biomeRegistry, chunkAccess);

        fillBiomesFromNoise(chunkAccess, biomeresolver, (p_188655_, p_188656_, p_188657_) -> {
            return this.tbSampler.targetTB(p_188655_, p_188656_, p_188657_, noiseChunk.noiseDataTB(p_188655_, p_188657_));
        });
    }

    public TBNoiseChunk getOrCreateNoiseChunk(ChunkAccess chunkAccess, TBNoiseSampler sampler, Supplier<NoiseChunk.NoiseFiller> noiseFiller, NoiseGeneratorSettings noiseGenSettings, Aquifer.FluidPicker fluidPicker, Blender blender)
    {
        if (chunkAccess.noiseChunk == null)
            chunkAccess.noiseChunk = TBNoiseChunk.forChunk(chunkAccess, sampler, noiseFiller, noiseGenSettings, fluidPicker, blender);

        return (TBNoiseChunk)chunkAccess.noiseChunk;
    }

    private void fillBiomesFromNoise(ChunkAccess chunkAccess, BiomeResolver biomeResolver, TBClimate.Sampler sampler)
    {
        ChunkPos chunkpos = chunkAccess.getPos();
        int i = QuartPos.fromBlock(chunkpos.getMinBlockX());
        int j = QuartPos.fromBlock(chunkpos.getMinBlockZ());
        LevelHeightAccessor levelheightaccessor = chunkAccess.getHeightAccessorForGeneration();

        for (int k = levelheightaccessor.getMinSection(); k < levelheightaccessor.getMaxSection(); ++k) {
            LevelChunkSection levelchunksection = chunkAccess.getSection(chunkAccess.getSectionIndexFromSectionY(k));
            levelchunksection.fillBiomesFromNoise(biomeResolver, sampler, i, j);
        }
    }

    @Override
    public Climate.Sampler climateSampler() {
        return this.tbSampler;
    }

    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public ChunkGenerator withSeed(long p_64374_) {
        return new TBNoiseBasedChunkGenerator(this.noises, this.biomeSource.withSeed(p_64374_), p_64374_, this.settings);
    }

    public boolean stable(long p_64376_, ResourceKey<NoiseGeneratorSettings> p_64377_) {
        return this.seed == p_64376_ && this.settings.get().stable(p_64377_);
    }

    public int getBaseHeight(int p_158405_, int p_158406_, Heightmap.Types p_158407_, LevelHeightAccessor p_158408_) {
        NoiseSettings noisesettings = this.settings.get().noiseSettings();
        int i = Math.max(noisesettings.minY(), p_158408_.getMinBuildHeight());
        int j = Math.min(noisesettings.minY() + noisesettings.height(), p_158408_.getMaxBuildHeight());
        int k = Mth.intFloorDiv(i, noisesettings.getCellHeight());
        int l = Mth.intFloorDiv(j - i, noisesettings.getCellHeight());
        return l <= 0 ? p_158408_.getMinBuildHeight() : this.iterateNoiseColumn(p_158405_, p_158406_, (BlockState[])null, p_158407_.isOpaque(), k, l).orElse(p_158408_.getMinBuildHeight());
    }

    @Override
    public NoiseColumn getBaseColumn(int p_158401_, int p_158402_, LevelHeightAccessor p_158403_) {
        NoiseSettings noisesettings = this.settings.get().noiseSettings();
        int i = Math.max(noisesettings.minY(), p_158403_.getMinBuildHeight());
        int j = Math.min(noisesettings.minY() + noisesettings.height(), p_158403_.getMaxBuildHeight());
        int k = Mth.intFloorDiv(i, noisesettings.getCellHeight());
        int l = Mth.intFloorDiv(j - i, noisesettings.getCellHeight());
        if (l <= 0) {
            return new NoiseColumn(i, EMPTY_COLUMN);
        } else {
            BlockState[] ablockstate = new BlockState[l * noisesettings.getCellHeight()];
            this.iterateNoiseColumn(p_158401_, p_158402_, ablockstate, (Predicate<BlockState>)null, k, l);
            return new NoiseColumn(i, ablockstate);
        }
    }

    @Override
    protected OptionalInt iterateNoiseColumn(int p_158414_, int p_158415_, @Nullable BlockState[] p_158416_, @Nullable Predicate<BlockState> p_158417_, int p_158418_, int p_158419_) {
        NoiseSettings noisesettings = this.settings.get().noiseSettings();
        int i = noisesettings.getCellWidth();
        int j = noisesettings.getCellHeight();
        int k = Math.floorDiv(p_158414_, i);
        int l = Math.floorDiv(p_158415_, i);
        int i1 = Math.floorMod(p_158414_, i);
        int j1 = Math.floorMod(p_158415_, i);
        int k1 = k * i;
        int l1 = l * i;
        double d0 = (double)i1 / (double)i;
        double d1 = (double)j1 / (double)i;
        TBNoiseChunk noisechunk = TBNoiseChunk.forColumn(k1, l1, p_158418_, p_158419_, this.tbSampler, this.settings.get(), this.globalFluidPicker);
        noisechunk.initializeForFirstCellX();
        noisechunk.advanceCellX(0);

        for(int i2 = p_158419_ - 1; i2 >= 0; --i2) {
            noisechunk.selectCellYZ(i2, 0);

            for(int j2 = j - 1; j2 >= 0; --j2) {
                int k2 = (p_158418_ + i2) * j + j2;
                double d2 = (double)j2 / (double)j;
                noisechunk.updateForY(d2);
                noisechunk.updateForX(d0);
                noisechunk.updateForZ(d1);
                BlockState blockstate = this.materialRule.apply(noisechunk, p_158414_, k2, p_158415_);
                BlockState blockstate1 = blockstate == null ? this.defaultBlock : blockstate;
                if (p_158416_ != null) {
                    int l2 = i2 * j + j2;
                    p_158416_[l2] = blockstate1;
                }

                if (p_158417_ != null && p_158417_.test(blockstate1)) {
                    return OptionalInt.of(k2 + 1);
                }
            }
        }

        return OptionalInt.empty();
    }

    @Override
    public void buildSurface(WorldGenRegion p_188636_, StructureFeatureManager p_188637_, ChunkAccess chunkAccess) {
        if (!SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
            WorldGenerationContext worldgenerationcontext = new WorldGenerationContext(this, p_188636_);
            NoiseGeneratorSettings noisegeneratorsettings = this.settings.get();
            TBNoiseChunk noisechunk = this.getOrCreateNoiseChunk(chunkAccess, this.tbSampler, () -> {
                return new Beardifier(p_188637_, chunkAccess);
            }, noisegeneratorsettings, this.globalFluidPicker, Blender.of(p_188636_));
            this.tbSurfaceSystem.buildSurface(p_188636_.getBiomeManager(), p_188636_.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), noisegeneratorsettings.useLegacyRandomSource(), worldgenerationcontext, chunkAccess, noisechunk, noisegeneratorsettings.surfaceRule());
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion p_188629_, long p_188630_, BiomeManager biomeManager, StructureFeatureManager p_188632_, ChunkAccess chunkAccess, GenerationStep.Carving p_188634_) {
        BiomeManager biomemanager = biomeManager.withDifferentSource((p_188620_, p_188621_, p_188622_) -> {
            return this.biomeSource.getNoiseBiome(p_188620_, p_188621_, p_188622_, this.climateSampler());
        });
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
        int i = 8;
        ChunkPos chunkpos = chunkAccess.getPos();
        TBNoiseChunk noisechunk = this.getOrCreateNoiseChunk(chunkAccess, this.tbSampler, () -> {
            return new Beardifier(p_188632_, chunkAccess);
        }, this.settings.get(), this.globalFluidPicker, Blender.of(p_188629_));
        Aquifer aquifer = noisechunk.aquifer();
        CarvingContext carvingcontext = new CarvingContext(this, p_188629_.registryAccess(), chunkAccess.getHeightAccessorForGeneration(), noisechunk);
        CarvingMask carvingmask = ((ProtoChunk)chunkAccess).getOrCreateCarvingMask(p_188634_);

        for(int j = -8; j <= 8; ++j) {
            for(int k = -8; k <= 8; ++k) {
                ChunkPos chunkpos1 = new ChunkPos(chunkpos.x + j, chunkpos.z + k);
                ChunkAccess chunkaccess = p_188629_.getChunk(chunkpos1.x, chunkpos1.z);
                BiomeGenerationSettings biomegenerationsettings = chunkaccess.carverBiome(() -> {
                    return this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkpos1.getMinBlockX()), 0, QuartPos.fromBlock(chunkpos1.getMinBlockZ()), this.climateSampler());
                }).getGenerationSettings();
                List<Supplier<ConfiguredWorldCarver<?>>> list = biomegenerationsettings.getCarvers(p_188634_);
                ListIterator<Supplier<ConfiguredWorldCarver<?>>> listiterator = list.listIterator();

                while(listiterator.hasNext()) {
                    int l = listiterator.nextIndex();
                    ConfiguredWorldCarver<?> carver = listiterator.next().get();
                    worldgenrandom.setLargeFeatureSeed(p_188630_ + (long)l, chunkpos1.x, chunkpos1.z);
                    if (carver.isStartChunk(worldgenrandom)) {
                        carver.carve(carvingcontext, chunkAccess, biomemanager::getBiome, worldgenrandom, aquifer, chunkpos1, carvingmask);
                    }
                }
            }
        }

    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor p_188702_, Blender p_188703_, StructureFeatureManager p_188704_, ChunkAccess p_188705_)
    {
        NoiseSettings noisesettings = this.settings.get().noiseSettings();
        LevelHeightAccessor levelheightaccessor = p_188705_.getHeightAccessorForGeneration();
        int i = Math.max(noisesettings.minY(), levelheightaccessor.getMinBuildHeight());
        int j = Math.min(noisesettings.minY() + noisesettings.height(), levelheightaccessor.getMaxBuildHeight());
        int k = Mth.intFloorDiv(i, noisesettings.getCellHeight());
        int l = Mth.intFloorDiv(j - i, noisesettings.getCellHeight());
        if (l <= 0) {
            return CompletableFuture.completedFuture(p_188705_);
        } else {
            int i1 = p_188705_.getSectionIndex(l * noisesettings.getCellHeight() - 1 + i);
            int j1 = p_188705_.getSectionIndex(i);
            Set<LevelChunkSection> set = Sets.newHashSet();

            for(int k1 = i1; k1 >= j1; --k1) {
                LevelChunkSection levelchunksection = p_188705_.getSection(k1);
                levelchunksection.acquire();
                set.add(levelchunksection);
            }

            return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise", () -> {
                return this.doFill(p_188703_, p_188704_, p_188705_, k, l);
            }), Util.backgroundExecutor()).whenCompleteAsync((p_188677_, p_188678_) -> {
                for(LevelChunkSection levelchunksection1 : set) {
                    levelchunksection1.release();
                }

            }, p_188702_);
        }
    }

    private ChunkAccess doFill(Blender p_188663_, StructureFeatureManager p_188664_, ChunkAccess chunkAccess, int p_188666_, int p_188667_) {
        NoiseGeneratorSettings noisegeneratorsettings = this.settings.get();
        TBNoiseChunk noisechunk = this.getOrCreateNoiseChunk(chunkAccess, this.tbSampler, () -> {
            return new Beardifier(p_188664_, chunkAccess);
        }, noisegeneratorsettings, this.globalFluidPicker, p_188663_);
        Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap1 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkpos = chunkAccess.getPos();
        int i = chunkpos.getMinBlockX();
        int j = chunkpos.getMinBlockZ();
        Aquifer aquifer = noisechunk.aquifer();
        noisechunk.initializeForFirstCellX();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        NoiseSettings noisesettings = noisegeneratorsettings.noiseSettings();
        int k = noisesettings.getCellWidth();
        int l = noisesettings.getCellHeight();
        int i1 = 16 / k;
        int j1 = 16 / k;

        for(int k1 = 0; k1 < i1; ++k1) {
            noisechunk.advanceCellX(k1);

            for(int l1 = 0; l1 < j1; ++l1) {
                LevelChunkSection levelchunksection = chunkAccess.getSection(chunkAccess.getSectionsCount() - 1);

                for(int i2 = p_188667_ - 1; i2 >= 0; --i2) {
                    noisechunk.selectCellYZ(i2, l1);

                    for(int j2 = l - 1; j2 >= 0; --j2) {
                        int k2 = (p_188666_ + i2) * l + j2;
                        int l2 = k2 & 15;
                        int i3 = chunkAccess.getSectionIndex(k2);
                        if (chunkAccess.getSectionIndex(levelchunksection.bottomBlockY()) != i3) {
                            levelchunksection = chunkAccess.getSection(i3);
                        }

                        double d0 = (double)j2 / (double)l;
                        noisechunk.updateForY(d0);

                        for(int j3 = 0; j3 < k; ++j3) {
                            int k3 = i + k1 * k + j3;
                            int l3 = k3 & 15;
                            double d1 = (double)j3 / (double)k;
                            noisechunk.updateForX(d1);

                            for(int i4 = 0; i4 < k; ++i4) {
                                int j4 = j + l1 * k + i4;
                                int k4 = j4 & 15;
                                double d2 = (double)i4 / (double)k;
                                noisechunk.updateForZ(d2);
                                BlockState blockstate = this.materialRule.apply(noisechunk, k3, k2, j4);
                                if (blockstate == null) {
                                    blockstate = this.defaultBlock;
                                }

                                blockstate = this.debugPreliminarySurfaceLevel(noisechunk, k3, k2, j4, blockstate);
                                if (blockstate != AIR && !SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
                                    if (blockstate.getLightEmission() != 0 && chunkAccess instanceof ProtoChunk) {
                                        blockpos$mutableblockpos.set(k3, k2, j4);
                                        ((ProtoChunk)chunkAccess).addLight(blockpos$mutableblockpos);
                                    }

                                    levelchunksection.setBlockState(l3, l2, k4, blockstate, false);
                                    heightmap.update(l3, k2, k4, blockstate);
                                    heightmap1.update(l3, k2, k4, blockstate);
                                    if (aquifer.shouldScheduleFluidUpdate() && !blockstate.getFluidState().isEmpty()) {
                                        blockpos$mutableblockpos.set(k3, k2, j4);
                                        chunkAccess.markPosForPostprocessing(blockpos$mutableblockpos);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            noisechunk.swapSlices();
        }

        return chunkAccess;
    }

    private BlockState debugPreliminarySurfaceLevel(TBNoiseChunk p_198232_, int p_198233_, int p_198234_, int p_198235_, BlockState p_198236_) {
        return p_198236_;
    }

    public int getGenDepth() {
        return this.settings.get().noiseSettings().height();
    }

    public int getSeaLevel() {
        return this.settings.get().seaLevel();
    }

    public int getMinY() {
        return this.settings.get().noiseSettings().minY();
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome p_158433_, StructureFeatureManager p_158434_, MobCategory p_158435_, BlockPos p_158436_) {
        if (!p_158434_.hasAnyStructureAt(p_158436_)) {
            return super.getMobsAt(p_158433_, p_158434_, p_158435_, p_158436_);
        } else {
            WeightedRandomList<MobSpawnSettings.SpawnerData> spawns = net.minecraftforge.common.world.StructureSpawnManager.getStructureSpawns(p_158434_, p_158435_, p_158436_);
            if (spawns != null) return spawns;
            if (false) {//Forge: We handle these hardcoded cases above in StructureSpawnManager#getStructureSpawns, but allow for insideOnly to be changed and allow for creatures to be spawned in ones other than just the witch hut
                if (p_158434_.getStructureWithPieceAt(p_158436_, StructureFeature.SWAMP_HUT).isValid()) {
                    if (p_158435_ == MobCategory.MONSTER) {
                        return SwamplandHutFeature.SWAMPHUT_ENEMIES;
                    }

                    if (p_158435_ == MobCategory.CREATURE) {
                        return SwamplandHutFeature.SWAMPHUT_ANIMALS;
                    }
                }

                if (p_158435_ == MobCategory.MONSTER) {
                    if (p_158434_.getStructureAt(p_158436_, StructureFeature.PILLAGER_OUTPOST).isValid()) {
                        return PillagerOutpostFeature.OUTPOST_ENEMIES;
                    }

                    if (p_158434_.getStructureAt(p_158436_, StructureFeature.OCEAN_MONUMENT).isValid()) {
                        return OceanMonumentFeature.MONUMENT_ENEMIES;
                    }

                    if (p_158434_.getStructureWithPieceAt(p_158436_, StructureFeature.NETHER_BRIDGE).isValid()) {
                        return NetherFortressFeature.FORTRESS_ENEMIES;
                    }
                }
            }

            return (p_158435_ == MobCategory.UNDERGROUND_WATER_CREATURE || p_158435_ == MobCategory.AXOLOTLS) && p_158434_.getStructureAt(p_158436_, StructureFeature.OCEAN_MONUMENT).isValid() ? MobSpawnSettings.EMPTY_MOB_LIST : super.getMobsAt(p_158433_, p_158434_, p_158435_, p_158436_);
        }
    }

    public void spawnOriginalMobs(WorldGenRegion p_64379_) {
        if (!this.settings.get().disableMobGeneration()) {
            ChunkPos chunkpos = p_64379_.getCenter();
            Biome biome = p_64379_.getBiome(chunkpos.getWorldPosition().atY(p_64379_.getMaxBuildHeight() - 1));
            WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
            worldgenrandom.setDecorationSeed(p_64379_.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
            NaturalSpawner.spawnMobsForChunkGeneration(p_64379_, biome, chunkpos, worldgenrandom);
        }
    }

    @Override
    public Optional<BlockState> topMaterial(CarvingContext context, Function<BlockPos, Biome> p_188670_, ChunkAccess chunkAccess, NoiseChunk chunk, BlockPos pos, boolean p_188674_)
    {
        return this.tbSurfaceSystem.topMaterial(this.settings.get().surfaceRule(), context, p_188670_, chunkAccess, chunk, pos, p_188674_);
    }
}
