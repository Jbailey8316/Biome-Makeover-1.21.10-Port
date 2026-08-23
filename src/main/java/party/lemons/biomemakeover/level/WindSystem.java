package party.lemons.biomemakeover.level;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class WindSystem {
    public static float windX=.05F,windZ=.05F; private static float directionX=1,directionZ=1;
    private WindSystem(){}
    public static void update(RandomSource random){
        if(random.nextInt(100)==0){directionX=-directionX;windX+=directionX*random.nextFloat()/25F;}
        if(random.nextInt(100)==0){directionZ=-directionZ;windZ+=directionZ*random.nextFloat()/25F;}
        if(random.nextInt(20)==0){windX=Mth.clamp(windX+directionX*random.nextFloat()/30F,-.7F,.7F);windZ=Mth.clamp(windZ+directionZ*random.nextFloat()/30F,-.7F,.7F);}
    }
}
