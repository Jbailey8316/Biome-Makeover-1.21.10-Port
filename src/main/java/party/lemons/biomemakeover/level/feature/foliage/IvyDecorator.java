package party.lemons.biomemakeover.level.feature.foliage;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMFeatures;

/** Released 1-in-8-per-side log ivy decorator used only by Small Ancient Oak. */
public final class IvyDecorator extends TreeDecorator {
    public static final IvyDecorator INSTANCE=new IvyDecorator();
    public static final MapCodec<IvyDecorator> CODEC=MapCodec.unit(INSTANCE);
    private IvyDecorator() {}
    @Override protected TreeDecoratorType<?> type(){return BMFeatures.IVY_DECORATOR;}
    @Override public void place(Context context){
        context.logs().forEach(log->{
            for(Direction outward:Direction.Plane.HORIZONTAL){
                if(context.random().nextInt(8)!=0)continue;
                BlockPos target=log.relative(outward);
                if(context.isAir(target))context.setBlock(target,BMBlocks.IVY.defaultBlockState()
                    .setValue(MultifaceBlock.getFaceProperty(outward.getOpposite()),true));
            }
        });
    }
}
