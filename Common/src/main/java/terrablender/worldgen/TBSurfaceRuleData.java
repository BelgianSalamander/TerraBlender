/**
 * Copyright (C) Glitchfiend
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package terrablender.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import terrablender.api.GenerationSettings;

import java.util.List;

public class TBSurfaceRuleData
{
    private static final SurfaceRules.RuleSource AIR = makeStateRule(Blocks.AIR);
    private static final SurfaceRules.RuleSource BEDROCK = makeStateRule(Blocks.BEDROCK);
    private static final SurfaceRules.RuleSource WHITE_TERRACOTTA = makeStateRule(Blocks.WHITE_TERRACOTTA);
    private static final SurfaceRules.RuleSource ORANGE_TERRACOTTA = makeStateRule(Blocks.ORANGE_TERRACOTTA);
    private static final SurfaceRules.RuleSource TERRACOTTA = makeStateRule(Blocks.TERRACOTTA);
    private static final SurfaceRules.RuleSource RED_SAND = makeStateRule(Blocks.RED_SAND);
    private static final SurfaceRules.RuleSource RED_SANDSTONE = makeStateRule(Blocks.RED_SANDSTONE);
    private static final SurfaceRules.RuleSource STONE = makeStateRule(Blocks.STONE);
    private static final SurfaceRules.RuleSource DEEPSLATE = makeStateRule(Blocks.DEEPSLATE);
    private static final SurfaceRules.RuleSource DIRT = makeStateRule(Blocks.DIRT);
    private static final SurfaceRules.RuleSource PODZOL = makeStateRule(Blocks.PODZOL);
    private static final SurfaceRules.RuleSource COARSE_DIRT = makeStateRule(Blocks.COARSE_DIRT);
    private static final SurfaceRules.RuleSource MYCELIUM = makeStateRule(Blocks.MYCELIUM);
    private static final SurfaceRules.RuleSource GRASS_BLOCK = makeStateRule(Blocks.GRASS_BLOCK);
    private static final SurfaceRules.RuleSource CALCITE = makeStateRule(Blocks.CALCITE);
    private static final SurfaceRules.RuleSource GRAVEL = makeStateRule(Blocks.GRAVEL);
    private static final SurfaceRules.RuleSource SAND = makeStateRule(Blocks.SAND);
    private static final SurfaceRules.RuleSource SANDSTONE = makeStateRule(Blocks.SANDSTONE);
    private static final SurfaceRules.RuleSource PACKED_ICE = makeStateRule(Blocks.PACKED_ICE);
    private static final SurfaceRules.RuleSource SNOW_BLOCK = makeStateRule(Blocks.SNOW_BLOCK);
    private static final SurfaceRules.RuleSource POWDER_SNOW = makeStateRule(Blocks.POWDER_SNOW);
    private static final SurfaceRules.RuleSource ICE = makeStateRule(Blocks.ICE);
    private static final SurfaceRules.RuleSource WATER = makeStateRule(Blocks.WATER);
    private static final SurfaceRules.RuleSource LAVA = makeStateRule(Blocks.LAVA);
    private static final SurfaceRules.RuleSource MAGMA = makeStateRule(Blocks.MAGMA_BLOCK);
    private static final SurfaceRules.RuleSource OBSIDIAN = makeStateRule(Blocks.OBSIDIAN);
    private static final SurfaceRules.RuleSource TUFF = makeStateRule(Blocks.TUFF);
    private static final SurfaceRules.RuleSource SMOOTH_BASALT = makeStateRule(Blocks.SMOOTH_BASALT);

    // Nether
    private static final SurfaceRules.RuleSource NETHERRACK = makeStateRule(Blocks.NETHERRACK);
    private static final SurfaceRules.RuleSource SOUL_SAND = makeStateRule(Blocks.SOUL_SAND);
    private static final SurfaceRules.RuleSource SOUL_SOIL = makeStateRule(Blocks.SOUL_SOIL);
    private static final SurfaceRules.RuleSource BASALT = makeStateRule(Blocks.BASALT);
    private static final SurfaceRules.RuleSource BLACKSTONE = makeStateRule(Blocks.BLACKSTONE);
    private static final SurfaceRules.RuleSource WARPED_WART_BLOCK = makeStateRule(Blocks.WARPED_WART_BLOCK);
    private static final SurfaceRules.RuleSource WARPED_NYLIUM = makeStateRule(Blocks.WARPED_NYLIUM);
    private static final SurfaceRules.RuleSource NETHER_WART_BLOCK = makeStateRule(Blocks.NETHER_WART_BLOCK);
    private static final SurfaceRules.RuleSource CRIMSON_NYLIUM = makeStateRule(Blocks.CRIMSON_NYLIUM);
    private static final SurfaceRules.RuleSource ENDSTONE = makeStateRule(Blocks.END_STONE);

    private static SurfaceRules.RuleSource makeStateRule(Block block)
    {
        return SurfaceRules.state(block.defaultBlockState());
    }

    public static SurfaceRules.RuleSource overworld()
    {
        return overworldLike(true, false, true);
    }

    public static SurfaceRules.RuleSource overworldLike(boolean checkAbovePreliminarySurface, boolean bedrockRoof, boolean bedrockFloor)
    {
        SurfaceRules.ConditionSource above97 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(97), 2);
        SurfaceRules.ConditionSource above256 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(256), 0);
        SurfaceRules.ConditionSource above63_1 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(63), -1);
        SurfaceRules.ConditionSource above74 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(74), 1);
        SurfaceRules.ConditionSource above62 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0);
        SurfaceRules.ConditionSource above63_0 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0);

        SurfaceRules.ConditionSource isAtOrAboveWaterLevel = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.ConditionSource isAboveWaterLevel = SurfaceRules.waterBlockCheck(0, 0);
        SurfaceRules.ConditionSource surfacerules$conditionsource8 = SurfaceRules.waterStartCheck(-6, -1);
        SurfaceRules.ConditionSource isHole = SurfaceRules.hole();
        SurfaceRules.ConditionSource isFrozenOcean = SurfaceRules.isBiome(Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN);
        SurfaceRules.ConditionSource isSteep = SurfaceRules.steep();

        SurfaceRules.RuleSource grassSurface = SurfaceRules.sequence(SurfaceRules.ifTrue(isAtOrAboveWaterLevel, GRASS_BLOCK), DIRT);
        SurfaceRules.RuleSource sandstoneLinedSand = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, SANDSTONE), SAND);
        SurfaceRules.RuleSource stoneLinedGravel = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, STONE), GRAVEL);

        SurfaceRules.ConditionSource isSandstoneLiningBiome = SurfaceRules.isBiome(Biomes.WARM_OCEAN, Biomes.DESERT, Biomes.BEACH, Biomes.SNOWY_BEACH);

        SurfaceRules.RuleSource surfacerules$rulesource3 = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.STONY_PEAKS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.CALCITE, -0.0125D, 0.0125D), CALCITE),
                    STONE
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.STONY_SHORE),
                SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.GRAVEL, -0.05D, 0.05D), stoneLinedGravel), STONE)
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.WINDSWEPT_HILLS), SurfaceRules.ifTrue(surfaceNoiseAbove(1.0D), STONE)
            ),
            SurfaceRules.ifTrue(isSandstoneLiningBiome, sandstoneLinedSand),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.DRIPSTONE_CAVES), STONE)
        );

        SurfaceRules.RuleSource surfacerules$rulesource4 = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW, 0.45D, 0.58D), POWDER_SNOW);
        SurfaceRules.RuleSource surfacerules$rulesource5 = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW, 0.35D, 0.6D), POWDER_SNOW);
        SurfaceRules.RuleSource surfacerules$rulesource6 = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.FROZEN_PEAKS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(isSteep, PACKED_ICE),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PACKED_ICE, -0.5D, 0.2D), PACKED_ICE),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.ICE, -0.0625D, 0.025D), ICE), SNOW_BLOCK
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.SNOWY_SLOPES),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(isSteep, STONE), surfacerules$rulesource4, SNOW_BLOCK
                )
            ),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.JAGGED_PEAKS), STONE),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.GROVE),
                SurfaceRules.sequence(surfacerules$rulesource4, DIRT)
            ),
            surfacerules$rulesource3,
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.WINDSWEPT_SAVANNA),
                SurfaceRules.ifTrue(surfaceNoiseAbove(1.75D), STONE)
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.WINDSWEPT_GRAVELLY_HILLS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(surfaceNoiseAbove(2.0D), stoneLinedGravel),
                    SurfaceRules.ifTrue(surfaceNoiseAbove(1.0D), STONE),
                    SurfaceRules.ifTrue(surfaceNoiseAbove(-1.0D), DIRT),
                    stoneLinedGravel
                )
            ),
            DIRT
        );

        SurfaceRules.RuleSource atOrAboveWaterLevelRules = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.FROZEN_PEAKS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(isSteep, PACKED_ICE),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PACKED_ICE, 0.0D, 0.2D), PACKED_ICE),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.ICE, 0.0D, 0.025D), ICE), SNOW_BLOCK
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.SNOWY_SLOPES),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(isSteep, STONE),
                    surfacerules$rulesource5, SNOW_BLOCK
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.JAGGED_PEAKS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(isSteep, STONE),
                    SNOW_BLOCK
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.GROVE),
                SurfaceRules.sequence(surfacerules$rulesource5, SNOW_BLOCK)
            ),
            surfacerules$rulesource3,
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.WINDSWEPT_SAVANNA),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(surfaceNoiseAbove(1.75D), STONE),
                    SurfaceRules.ifTrue(surfaceNoiseAbove(-0.5D), COARSE_DIRT)
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.WINDSWEPT_GRAVELLY_HILLS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(surfaceNoiseAbove(2.0D), stoneLinedGravel),
                    SurfaceRules.ifTrue(surfaceNoiseAbove(1.0D), STONE),
                    SurfaceRules.ifTrue(surfaceNoiseAbove(-1.0D), grassSurface),
                    stoneLinedGravel
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(surfaceNoiseAbove(1.75D), COARSE_DIRT),
                    SurfaceRules.ifTrue(surfaceNoiseAbove(-0.95D), PODZOL)
                )
            ),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.ICE_SPIKES), SNOW_BLOCK),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MUSHROOM_FIELDS), MYCELIUM),
            grassSurface
        );

        SurfaceRules.ConditionSource surfacerules$conditionsource13 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.909D, -0.5454D);
        SurfaceRules.ConditionSource surfacerules$conditionsource14 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.1818D, 0.1818D);
        SurfaceRules.ConditionSource surfacerules$conditionsource15 = SurfaceRules.noiseCondition(Noises.SURFACE, 0.5454D, 0.909D);
        SurfaceRules.RuleSource surfaceRules = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.WOODED_BADLANDS),
                        SurfaceRules.ifTrue(
                            above97,
                            SurfaceRules.sequence(
                                SurfaceRules.ifTrue(surfacerules$conditionsource13, COARSE_DIRT),
                                SurfaceRules.ifTrue(surfacerules$conditionsource14, COARSE_DIRT),
                                SurfaceRules.ifTrue(surfacerules$conditionsource15, COARSE_DIRT),
                                grassSurface
                            )
                        )
                    ),
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.SWAMP),
                        SurfaceRules.ifTrue(
                            above62,
                            SurfaceRules.ifTrue(SurfaceRules.not(above63_0),
                                SurfaceRules.ifTrue(
                                    SurfaceRules.noiseCondition(Noises.SWAMP, 0.0D),
                                    WATER
                                )
                            )
                        )
                    )
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.sequence(
                            SurfaceRules.ifTrue(above256, ORANGE_TERRACOTTA),
                            SurfaceRules.ifTrue(
                                above74,
                                SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(surfacerules$conditionsource13, TERRACOTTA),
                                    SurfaceRules.ifTrue(surfacerules$conditionsource14, TERRACOTTA),
                                    SurfaceRules.ifTrue(surfacerules$conditionsource15, TERRACOTTA),
                                    SurfaceRules.bandlands()
                                )
                            ),
                            SurfaceRules.ifTrue(
                                isAtOrAboveWaterLevel,
                                SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, RED_SANDSTONE),
                                    RED_SAND
                                )
                            ),
                            SurfaceRules.ifTrue(SurfaceRules.not(isHole), ORANGE_TERRACOTTA),
                            SurfaceRules.ifTrue(surfacerules$conditionsource8, WHITE_TERRACOTTA),
                            stoneLinedGravel
                        )
                    ),
                    SurfaceRules.ifTrue(
                        above63_1,
                        SurfaceRules.sequence(
                            SurfaceRules.ifTrue(
                                above63_0,
                                SurfaceRules.ifTrue(SurfaceRules.not(above74), ORANGE_TERRACOTTA)
                            ),
                            SurfaceRules.bandlands()
                        )
                    ),
                    SurfaceRules.ifTrue(
                        SurfaceRules.UNDER_FLOOR,
                        SurfaceRules.ifTrue(surfacerules$conditionsource8, WHITE_TERRACOTTA)
                    )
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.ifTrue(
                    isAtOrAboveWaterLevel,
                    SurfaceRules.sequence(
                        SurfaceRules.ifTrue(
                            isFrozenOcean,
                            SurfaceRules.ifTrue(
                                isHole,
                                SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                        isAboveWaterLevel,
                                        AIR),
                                    SurfaceRules.ifTrue(SurfaceRules.temperature(), ICE),
                                    WATER
                                )
                            )
                        ),
                        atOrAboveWaterLevelRules
                    )
                )
            ),
            SurfaceRules.ifTrue(
                surfacerules$conditionsource8,
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(
                            isFrozenOcean,
                            SurfaceRules.ifTrue(
                                isHole,
                                WATER
                            )
                        )
                    ),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, surfacerules$rulesource6),
                    SurfaceRules.ifTrue(
                        isSandstoneLiningBiome,
                        SurfaceRules.ifTrue(
                            SurfaceRules.stoneDepthCheck(0, true, true, CaveSurface.FLOOR),
                            SANDSTONE
                        )
                    )
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS),
                        STONE),
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN),
                        sandstoneLinedSand
                    ),
                    stoneLinedGravel
                )
            )
        );

        List<SurfaceRules.RuleSource> afterBedrockRules = GenerationSettings.getAfterBedrockOverworldSurfaceRules();

        if (!afterBedrockRules.isEmpty())
        {
            ImmutableList.Builder<SurfaceRules.RuleSource> newSurfaceRules = ImmutableList.builder();
            newSurfaceRules.addAll(afterBedrockRules);
            newSurfaceRules.add(surfaceRules);
            surfaceRules = SurfaceRules.sequence(newSurfaceRules.build().toArray(SurfaceRules.RuleSource[]::new));
        }

        ImmutableList.Builder<SurfaceRules.RuleSource> builder = ImmutableList.builder();
        builder.addAll(GenerationSettings.getBeforeBedrockOverworldSurfaceRules());
        if (bedrockRoof)
            builder.add(SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("bedrock_roof", VerticalAnchor.belowTop(5), VerticalAnchor.top())), BEDROCK));

        if (bedrockFloor)
            builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), BEDROCK));

        SurfaceRules.RuleSource surfacerules$rulesource9 = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), surfaceRules);
        builder.add(checkAbovePreliminarySurface ? surfacerules$rulesource9 : surfaceRules);
        builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("deepslate", VerticalAnchor.absolute(0), VerticalAnchor.absolute(8)), DEEPSLATE));
        return SurfaceRules.sequence(builder.build().toArray((count) -> {
            return new SurfaceRules.RuleSource[count];
        }));
    }
    
    public static SurfaceRules.RuleSource nether()
    {
        SurfaceRules.ConditionSource isAbove31 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(31), 0);
        SurfaceRules.ConditionSource surfacerules$conditionsource1 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(32), 0);
        SurfaceRules.ConditionSource yStart30 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(30), 0);
        SurfaceRules.ConditionSource isBelow35 = SurfaceRules.not(SurfaceRules.yStartCheck(VerticalAnchor.absolute(35), 0));
        SurfaceRules.ConditionSource isTop5Blocks = SurfaceRules.yBlockCheck(VerticalAnchor.belowTop(5), 0);
        SurfaceRules.ConditionSource isHole = SurfaceRules.hole();
        SurfaceRules.ConditionSource isSuitableSoulSandNoise = SurfaceRules.noiseCondition(Noises.SOUL_SAND_LAYER, -0.012D);
        SurfaceRules.ConditionSource surfacerules$conditionsource7 = SurfaceRules.noiseCondition(Noises.GRAVEL_LAYER, -0.012D);
        SurfaceRules.ConditionSource isSuitablePatchNoise = SurfaceRules.noiseCondition(Noises.PATCH, -0.012D);
        SurfaceRules.ConditionSource isSuitableNetherrackNoise = SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.54D);
        SurfaceRules.ConditionSource surfacerules$conditionsource10 = SurfaceRules.noiseCondition(Noises.NETHER_WART, 1.17D);
        SurfaceRules.ConditionSource isStateSelectorNoiseSuitable = SurfaceRules.noiseCondition(Noises.NETHER_STATE_SELECTOR, 0.0D);

        SurfaceRules.RuleSource gravelPatchRules = SurfaceRules.ifTrue(isSuitablePatchNoise, SurfaceRules.ifTrue(yStart30, SurfaceRules.ifTrue(isBelow35, GRAVEL)));

        SurfaceRules.RuleSource bedrockRules = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                    SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
                    BEDROCK
            ),
            SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("bedrock_roof", VerticalAnchor.belowTop(5), VerticalAnchor.top())), BEDROCK)
        );

        SurfaceRules.RuleSource surfaceRules = SurfaceRules.sequence(
            SurfaceRules.ifTrue(isTop5Blocks, NETHERRACK),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.BASALT_DELTAS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, BASALT),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR,
                        SurfaceRules.sequence(
                            gravelPatchRules,
                            SurfaceRules.ifTrue(isStateSelectorNoiseSuitable, BASALT),
                            BLACKSTONE
                        )
                    )
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.SOUL_SAND_VALLEY),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.UNDER_CEILING,
                        SurfaceRules.sequence(
                            SurfaceRules.ifTrue(isStateSelectorNoiseSuitable, SOUL_SAND), SOUL_SOIL
                        )
                    ),
                    SurfaceRules.ifTrue(
                        SurfaceRules.UNDER_FLOOR,
                        SurfaceRules.sequence(
                            gravelPatchRules,
                            SurfaceRules.ifTrue(isStateSelectorNoiseSuitable, SOUL_SAND),
                            SOUL_SOIL
                        )
                    )
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.not(surfacerules$conditionsource1),
                        SurfaceRules.ifTrue(isHole, LAVA)
                    ),
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.WARPED_FOREST),
                        SurfaceRules.ifTrue(
                            SurfaceRules.not(isSuitableNetherrackNoise),
                            SurfaceRules.ifTrue(
                                isAbove31,
                                SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(surfacerules$conditionsource10, WARPED_WART_BLOCK),
                                    WARPED_NYLIUM
                                )
                            )
                        )
                    ),
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.CRIMSON_FOREST),
                        SurfaceRules.ifTrue(
                            SurfaceRules.not(isSuitableNetherrackNoise),
                            SurfaceRules.ifTrue(isAbove31,
                                SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(surfacerules$conditionsource10, NETHER_WART_BLOCK),
                                    CRIMSON_NYLIUM
                                )
                            )
                        )
                    )
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.NETHER_WASTES),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.UNDER_FLOOR,
                        SurfaceRules.ifTrue(
                            isSuitableSoulSandNoise,
                            SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                    SurfaceRules.not(isHole),
                                    SurfaceRules.ifTrue(yStart30, SurfaceRules.ifTrue(isBelow35, SOUL_SAND))
                                ),
                                NETHERRACK
                            )
                        )
                    ),
                    SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(
                            isAbove31,
                            SurfaceRules.ifTrue(
                                isBelow35,
                                SurfaceRules.ifTrue(
                                    surfacerules$conditionsource7,
                                    SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(surfacerules$conditionsource1, GRAVEL),
                                        SurfaceRules.ifTrue(SurfaceRules.not(isHole), GRAVEL)
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            NETHERRACK
        );

        ImmutableList.Builder<SurfaceRules.RuleSource> builder = ImmutableList.builder();
        builder.addAll(GenerationSettings.getBeforeBedrockNetherSurfaceRules());
        builder.add(bedrockRules);
        builder.addAll(GenerationSettings.getAfterBedrockNetherSurfaceRules());
        builder.add(surfaceRules);
        return SurfaceRules.sequence(builder.build().toArray(SurfaceRules.RuleSource[]::new));
    }

    private static SurfaceRules.ConditionSource surfaceNoiseAbove(double value) 
    {
        return SurfaceRules.noiseCondition(Noises.SURFACE, value / 8.25D, Double.MAX_VALUE);
    }
}
