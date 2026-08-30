import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.Field;

/** Real 1.21.10 DataFix/StructureTemplate loader for the Mansion corpus. */
public final class Stage11A2MansionTemplateRuntimeValidator {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("resource root required");
    SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); loadBlocks();
    Path root=Path.of(args[0]); List<Path> files;
    try (var stream=Files.walk(root.resolve("data/biomemakeover/structure/mansion"))) { files=stream.filter(p->p.toString().endsWith(".nbt")).sorted().toList(); }
    require(files.size()==168,"expected 168 templates, got "+files.size()); int blocks=0;
    Set<String> missing=new TreeSet<>();
    for(Path path:files){ CompoundTag raw; try(InputStream in=Files.newInputStream(path)){raw=NbtIo.readCompressed(in,NbtAccounter.unlimitedHeap());}
      int dv=NbtUtils.getDataVersion(raw,500); CompoundTag up=DataFixTypes.STRUCTURE.updateToCurrentVersion(DataFixers.getDataFixer(),raw,dv);
      ListTag palette=up.getListOrEmpty("palette"), list=up.getListOrEmpty("blocks"); require(!palette.isEmpty()&&!list.isEmpty(),path+" empty palette/blocks");
      for(var e:palette){CompoundTag paletteEntry=(CompoundTag)e; String n=paletteEntry.getStringOr("Name",""); require(!n.isEmpty(),path+" empty block name"); ResourceLocation id=ResourceLocation.parse(n); if(!BuiltInRegistries.BLOCK.containsKey(id)){missing.add(n); continue;} validateProperties(path, BuiltInRegistries.BLOCK.get(id).orElseThrow().value(), paletteEntry.getCompoundOrEmpty("Properties"));}
      for(var e:list){int i=((CompoundTag)e).getIntOr("state",-1); require(i>=0&&i<palette.size(),path+" invalid palette index");}
      StructureTemplate t=new StructureTemplate(); t.load(BuiltInRegistries.BLOCK,up); require(t.getSize().getX()>0&&t.getSize().getY()>0&&t.getSize().getZ()>0,path+" empty template"); blocks+=list.size(); }
    require(missing.isEmpty(),"unresolved palette blocks: "+String.join(", ",missing));
    System.out.printf("STAGE 11A.2 MANSION DATAFIX/LOAD PASSED (templates=%d blocks=%d)%n",files.size(),blocks);
  }
  @SuppressWarnings({"rawtypes","unchecked"})
  static void validateProperties(Path path, Block block, CompoundTag properties) {
    BlockState state=block.defaultBlockState();
    for(String key:properties.keySet()) {
      Property property=block.getStateDefinition().getProperty(key);
      require(property!=null, path+" block "+BuiltInRegistries.BLOCK.getKey(block)+" does not expose property "+key);
      String value=properties.getStringOr(key, "");
      require(property.getValue(value).isPresent(), path+" block "+BuiltInRegistries.BLOCK.getKey(block)+" rejects "+key+"="+value);
      state=state.setValue(property, (Comparable)property.getValue(value).get());
    }
  }
  static void loadBlocks() throws Exception {
    Field frozen=MappedRegistry.class.getDeclaredField("frozen"), intrusive=MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
    frozen.setAccessible(true); intrusive.setAccessible(true);
    for(Registry<?> r:BuiltInRegistries.REGISTRY) if(r instanceof MappedRegistry<?> m){frozen.setBoolean(m,false); if((r==BuiltInRegistries.BLOCK||r==BuiltInRegistries.ITEM)&&intrusive.get(m)==null) intrusive.set(m,new IdentityHashMap<>());}
    Class.forName("party.lemons.biomemakeover.init.BMBlocks");
  }
  static void require(boolean ok,String msg){if(!ok)throw new IllegalStateException(msg);}
}
