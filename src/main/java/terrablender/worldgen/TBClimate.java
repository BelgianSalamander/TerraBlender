/*******************************************************************************
 * Copyright 2022, the Glitchfiend Team.
 * Creative Commons Attribution-NonCommercial-NoDerivatives 4.0.
 ******************************************************************************/
package terrablender.worldgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.Parameter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.world.level.biome.Climate.quantizeCoord;

public class TBClimate
{
    private static final boolean DEBUG_SLOW_BIOME_SEARCH = false;
    private static final float QUANTIZATION_FACTOR = 10000.0F;
    @VisibleForTesting
    protected static final int PARAMETER_COUNT = 8;

    public static TargetPoint target(float temperature, float humidity, float continentalness, float erosion, float depth, float weirdness, float uniqueness)
    {
        return new TargetPoint(quantizeCoord(temperature), quantizeCoord(humidity), quantizeCoord(continentalness), quantizeCoord(erosion), quantizeCoord(depth), quantizeCoord(weirdness), quantizeCoord(uniqueness));
    }

    public static ParameterPoint parameters(float temperature, float humidity, float continentalness, float erosion, float depth, float weirdness, float uniqueness, float offset)
    {
        return new ParameterPoint(Parameter.point(temperature), Parameter.point(humidity), Parameter.point(continentalness), Parameter.point(erosion), Parameter.point(depth), Parameter.point(weirdness), Parameter.point(uniqueness), quantizeCoord(offset));
    }

    public static ParameterPoint parameters(Parameter temperature, Parameter humidity, Parameter continentalness, Parameter erosion, Parameter depth, Parameter weirdness, Parameter uniqueness, float offset)
    {
        return new ParameterPoint(temperature, humidity, continentalness, erosion, depth, weirdness, uniqueness, quantizeCoord(offset));
    }

    public static BlockPos findSpawnPosition(List<ParameterPoint> p_186807_, TBNoiseSampler p_186808_) {
        return (new SpawnFinder(p_186807_, p_186808_)).result.location();
    }

    interface DistanceMetric<T> {
        long distance(RTree.Node<T> p_186810_, long[] p_186811_);
    }

    public static class ParameterList<T>
    {
        private final List<Pair<ParameterPoint, T>> values;
        private final RTree<T> index;

        public ParameterList(List<Pair<ParameterPoint, T>> p_186849_) {
            this.values = p_186849_;
            this.index = RTree.create(p_186849_);
        }

        public List<Pair<ParameterPoint, T>> values() {
            return this.values;
        }

        public T findValue(TargetPoint target, T fallback) {
            return this.findValueIndex(target);
        }

        public T findValueIndex(TargetPoint p_186852_) {
            return this.findValueIndex(p_186852_, RTree.Node::distance);
        }

        protected T findValueIndex(TargetPoint p_186854_, DistanceMetric<T> p_186855_) {
            return this.index.search(p_186854_, p_186855_);
        }
    }

    public record ParameterPoint(Parameter temperature, Parameter humidity, Parameter continentalness, Parameter erosion, Parameter depth, Parameter weirdness, Parameter uniqueness, long offset)
    {
        public static final Codec<ParameterPoint> CODEC = RecordCodecBuilder.create((p_186885_) -> {
            return p_186885_.group(Parameter.CODEC.fieldOf("temperature").forGetter((p_186905_) -> {
                return p_186905_.temperature;
            }), Parameter.CODEC.fieldOf("humidity").forGetter((p_186902_) -> {
                return p_186902_.humidity;
            }), Parameter.CODEC.fieldOf("continentalness").forGetter((p_186897_) -> {
                return p_186897_.continentalness;
            }), Parameter.CODEC.fieldOf("erosion").forGetter((p_186894_) -> {
                return p_186894_.erosion;
            }), Parameter.CODEC.fieldOf("depth").forGetter((p_186891_) -> {
                return p_186891_.depth;
            }), Parameter.CODEC.fieldOf("weirdness").forGetter((p_186888_) -> {
                return p_186888_.weirdness;
            }), Parameter.CODEC.fieldOf("uniqueness").forGetter((p_186888_) -> {
                return p_186888_.uniqueness;
            }), Codec.floatRange(0.0F, 1.0F).fieldOf("offset").xmap(Climate::quantizeCoord, Climate::unquantizeCoord).forGetter((p_186881_) -> {
                return p_186881_.offset;
            })).apply(p_186885_, ParameterPoint::new);
        });

        long fitness(TargetPoint target) {
            return Mth.square(this.temperature.distance(target.temperature)) + Mth.square(this.humidity.distance(target.humidity)) + Mth.square(this.continentalness.distance(target.continentalness)) + Mth.square(this.erosion.distance(target.erosion)) + Mth.square(this.depth.distance(target.depth)) + Mth.square(this.weirdness.distance(target.weirdness)) + Mth.square(this.uniqueness.distance(target.uniqueness)) + Mth.square(this.offset);
        }

        protected List<Parameter> parameterSpace() {
            return ImmutableList.of(this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, this.uniqueness, new Parameter(this.offset, this.offset));
        }
    }

    protected static final class RTree<T>
    {
        private static final int CHILDREN_PER_NODE = 10;
        private final RTree.Node<T> root;
        private final ThreadLocal<RTree.Leaf<T>> lastResult = new ThreadLocal<>();

        private RTree(RTree.Node<T> p_186913_) {
            this.root = p_186913_;
        }

        public static <T> RTree<T> create(List<Pair<ParameterPoint, T>> p_186936_)
        {
            if (p_186936_.isEmpty()) {
                throw new IllegalArgumentException("Need at least one value to build the search tree.");
            } else {
                int i = p_186936_.get(0).getFirst().parameterSpace().size();
                if (i != PARAMETER_COUNT) {
                    throw new IllegalStateException("Expecting parameter space to be 8, got " + i);
                } else {
                    List<RTree.Leaf<T>> list = p_186936_.stream().map((p_186934_) -> {
                        return new RTree.Leaf<T>(p_186934_.getFirst(), p_186934_.getSecond());
                    }).collect(Collectors.toCollection(ArrayList::new));
                    return new RTree<>(build(i, list));
                }
            }
        }

        private static <T> RTree.Node<T> build(int p_186921_, List<? extends RTree.Node<T>> p_186922_)
        {
            if (p_186922_.isEmpty()) {
                throw new IllegalStateException("Need at least one child to build a node");
            } else if (p_186922_.size() == 1) {
                return p_186922_.get(0);
            } else if (p_186922_.size() <= 10) {
                p_186922_.sort(Comparator.comparingLong((p_186916_) -> {
                    long i1 = 0L;

                    for(int j1 = 0; j1 < p_186921_; ++j1) {
                        Parameter climate$parameter = p_186916_.parameterSpace[j1];
                        i1 += Math.abs((climate$parameter.min() + climate$parameter.max()) / 2L);
                    }

                    return i1;
                }));
                return new RTree.SubTree<>(p_186922_);
            } else {
                long i = Long.MAX_VALUE;
                int j = -1;
                List<RTree.SubTree<T>> list = null;

                for(int k = 0; k < p_186921_; ++k) {
                    sort(p_186922_, p_186921_, k, false);
                    List<RTree.SubTree<T>> list1 = bucketize(p_186922_);
                    long l = 0L;

                    for(RTree.SubTree<T> subtree : list1) {
                        l += cost(subtree.parameterSpace);
                    }

                    if (i > l) {
                        i = l;
                        j = k;
                        list = list1;
                    }
                }

                sort(list, p_186921_, j, true);
                return new RTree.SubTree<>(list.stream().map((p_186919_) -> {
                    return build(p_186921_, Arrays.asList(p_186919_.children));
                }).collect(Collectors.toList()));
            }
        }

        private static <T> void sort(List<? extends RTree.Node<T>> p_186938_, int p_186939_, int p_186940_, boolean p_186941_) {
            Comparator<RTree.Node<T>> comparator = comparator(p_186940_, p_186941_);

            for(int i = 1; i < p_186939_; ++i) {
                comparator = comparator.thenComparing(comparator((p_186940_ + i) % p_186939_, p_186941_));
            }

            p_186938_.sort(comparator);
        }

        private static <T> Comparator<RTree.Node<T>> comparator(int p_186924_, boolean p_186925_) {
            return Comparator.comparingLong((p_186929_) -> {
                Parameter climate$parameter = p_186929_.parameterSpace[p_186924_];
                long i = (climate$parameter.min() + climate$parameter.max()) / 2L;
                return p_186925_ ? Math.abs(i) : i;
            });
        }

        private static <T> List<RTree.SubTree<T>> bucketize(List<? extends RTree.Node<T>> p_186945_) {
            List<RTree.SubTree<T>> list = Lists.newArrayList();
            List<RTree.Node<T>> list1 = Lists.newArrayList();
            int i = (int)Math.pow(10.0D, Math.floor(Math.log((double)p_186945_.size() - 0.01D) / Math.log(10.0D)));

            for(RTree.Node<T> node : p_186945_) {
                list1.add(node);
                if (list1.size() >= i) {
                    list.add(new RTree.SubTree<>(list1));
                    list1 = Lists.newArrayList();
                }
            }

            if (!list1.isEmpty()) {
                list.add(new RTree.SubTree<>(list1));
            }

            return list;
        }

        private static long cost(Parameter[] p_186943_) {
            long i = 0L;

            for(Parameter climate$parameter : p_186943_) {
                i += Math.abs(climate$parameter.max() - climate$parameter.min());
            }

            return i;
        }

        static <T> List<Parameter> buildParameterSpace(List<? extends RTree.Node<T>> p_186947_) {
            if (p_186947_.isEmpty()) {
                throw new IllegalArgumentException("SubTree needs at least one child");
            } else {
                int i = PARAMETER_COUNT;
                List<Parameter> list = Lists.newArrayList();

                for(int j = 0; j < PARAMETER_COUNT; ++j) {
                    list.add((Parameter)null);
                }

                for(RTree.Node<T> node : p_186947_) {
                    for(int k = 0; k < PARAMETER_COUNT; ++k) {
                        list.set(k, node.parameterSpace[k].span(list.get(k)));
                    }
                }

                return list;
            }
        }

        public T search(TargetPoint p_186931_, DistanceMetric<T> p_186932_) {
            long[] along = p_186931_.toParameterArray();
            RTree.Leaf<T> leaf = this.root.search(along, this.lastResult.get(), p_186932_);
            this.lastResult.set(leaf);
            return leaf.value;
        }

        static final class Leaf<T> extends RTree.Node<T> {
            final T value;

            Leaf(ParameterPoint p_186950_, T p_186951_) {
                super(p_186950_.parameterSpace());
                this.value = p_186951_;
            }

            protected RTree.Leaf<T> search(long[] p_186953_, @Nullable RTree.Leaf<T> p_186954_, DistanceMetric<T> p_186955_) {
                return this;
            }
        }

        abstract static class Node<T> {
            protected final Parameter[] parameterSpace;

            protected Node(List<Parameter> p_186958_) {
                this.parameterSpace = p_186958_.toArray(new Parameter[0]);
            }

            protected abstract RTree.Leaf<T> search(long[] p_186961_, @Nullable RTree.Leaf<T> p_186962_, DistanceMetric<T> p_186963_);

            protected long distance(long[] p_186960_) {
                long i = 0L;

                for(int j = 0; j < PARAMETER_COUNT; ++j) {
                    i += Mth.square(this.parameterSpace[j].distance(p_186960_[j]));
                }

                return i;
            }

            public String toString() {
                return Arrays.toString((Object[])this.parameterSpace);
            }
        }

        static final class SubTree<T> extends RTree.Node<T> {
            final RTree.Node<T>[] children;

            protected SubTree(List<? extends RTree.Node<T>> p_186967_) {
                this(RTree.buildParameterSpace(p_186967_), p_186967_);
            }

            protected SubTree(List<Parameter> p_186969_, List<? extends RTree.Node<T>> p_186970_) {
                super(p_186969_);
                this.children = p_186970_.toArray(new RTree.Node[0]);
            }

            protected RTree.Leaf<T> search(long[] p_186972_, @Nullable RTree.Leaf<T> p_186973_, DistanceMetric<T> p_186974_) {
                long i = p_186973_ == null ? Long.MAX_VALUE : p_186974_.distance(p_186973_, p_186972_);
                RTree.Leaf<T> leaf = p_186973_;

                for(RTree.Node<T> node : this.children) {
                    long j = p_186974_.distance(node, p_186972_);
                    if (i > j) {
                        RTree.Leaf<T> leaf1 = node.search(p_186972_, leaf, p_186974_);
                        long k = node == leaf1 ? j : p_186974_.distance(leaf1, p_186972_);
                        if (i > k) {
                            i = k;
                            leaf = leaf1;
                        }
                    }
                }

                return leaf;
            }
        }
    }

    // We must extend Climate.Sampler to account for the use by BiomeResolver
    public interface Sampler extends Climate.Sampler
    {
        TargetPoint sampleTB(int x, int y, int z);

        default Climate.TargetPoint sample(int x, int y, int z)
        {
            throw new RuntimeException("Vanilla sample called on TB sampler!");
        }

        default BlockPos findSpawnPosition() {
            return BlockPos.ZERO;
        }
    }

    static class SpawnFinder
    {
        SpawnFinder.Result result;

        SpawnFinder(List<ParameterPoint> p_186980_, TBNoiseSampler p_186981_) {
            this.result = getSpawnPositionAndFitness(p_186980_, p_186981_, 0, 0);
            this.radialSearch(p_186980_, p_186981_, 2048.0F, 512.0F);
            this.radialSearch(p_186980_, p_186981_, 512.0F, 32.0F);
        }

        private void radialSearch(List<ParameterPoint> p_186983_, TBNoiseSampler p_186984_, float p_186985_, float p_186986_) {
            float f = 0.0F;
            float f1 = p_186986_;
            BlockPos blockpos = this.result.location();

            while(f1 <= p_186985_) {
                int i = blockpos.getX() + (int)(Math.sin((double)f) * (double)f1);
                int j = blockpos.getZ() + (int)(Math.cos((double)f) * (double)f1);
                SpawnFinder.Result climate$spawnfinder$result = getSpawnPositionAndFitness(p_186983_, p_186984_, i, j);
                if (climate$spawnfinder$result.fitness() < this.result.fitness()) {
                    this.result = climate$spawnfinder$result;
                }

                f += p_186986_ / f1;
                if ((double)f > (Math.PI * 2D)) {
                    f = 0.0F;
                    f1 += p_186986_;
                }
            }

        }

        private static SpawnFinder.Result getSpawnPositionAndFitness(List<ParameterPoint> p_186988_, TBNoiseSampler p_186989_, int p_186990_, int p_186991_) {
            double d0 = Mth.square(2500.0D);
            int i = 2;
            long j = (long)((double)Mth.square(10000.0F) * Math.pow((double)(Mth.square((long)p_186990_) + Mth.square((long)p_186991_)) / d0, 2.0D));
            TargetPoint climate$targetpoint = p_186989_.sampleTB(QuartPos.fromBlock(p_186990_), 0, QuartPos.fromBlock(p_186991_));
            TargetPoint climate$targetpoint1 = new TargetPoint(climate$targetpoint.temperature(), climate$targetpoint.humidity(), climate$targetpoint.continentalness(), climate$targetpoint.erosion(), 0L, climate$targetpoint.weirdness(), climate$targetpoint.uniqueness());
            long k = Long.MAX_VALUE;

            for(ParameterPoint climate$parameterpoint : p_186988_) {
                k = Math.min(k, climate$parameterpoint.fitness(climate$targetpoint1));
            }

            return new SpawnFinder.Result(new BlockPos(p_186990_, 0, p_186991_), j + k);
        }

        record Result(BlockPos location, long fitness) {
        }
    }

    public record TargetPoint(long temperature, long humidity, long continentalness, long erosion, long depth, long weirdness, long uniqueness)
    {
        @VisibleForTesting
        protected long[] toParameterArray() {
            return new long[]{this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, this.uniqueness, 0L};
        }
    }
}
