import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

/** Offline audit of released Mansion tapestry marker direction and neighbors. */
public final class Stage11B2BMarkerSupportAudit {
  record Cell(String name, String facing, String metadata) {}
  record Marker(String file, int x, int y, int z, String metadata, String facing, Map<String, Cell> neighbors) {}

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Mansion resource root required");
    Path root = Path.of(args[0]);
    List<Marker> markers = new ArrayList<>();
    try (var stream = Files.walk(root)) {
      for (Path file : stream.filter(p -> p.toString().endsWith(".nbt")).sorted().toList()) read(file, markers);
    }
    List<Marker> tapestries = markers.stream().filter(m -> "tapestry".equals(m.metadata())).toList();
    int opposite = 0, direct = 0, none = 0, ambiguous = 0;
    for (Marker m : tapestries) {
      String toward = neighbor(m, m.facing());
      String away = neighbor(m, opposite(m.facing()));
      boolean towardSupport = support(toward), awaySupport = support(away);
      if (awaySupport && !towardSupport) opposite++;
      else if (towardSupport && !awaySupport) direct++;
      else if (towardSupport) ambiguous++;
      else none++;
      System.out.printf("MARKER file=%s local=[%d,%d,%d] metadata=%s facing=%s toward=%s away=%s neighbors=%s%n",
          root.relativize(Path.of(m.file())).toString().replace('\\','/'), m.x(),m.y(),m.z(),m.metadata(),m.facing(),toward,away,m.neighbors());
    }
    System.out.printf("STAGE 11B.2B R.4 MARKER SUPPORT AUDIT totalWallMarkers=%d markerFacingPointsTowardBacking=%d markerFacingOppositePointsTowardBacking=%d noAdjacentBacking=%d ambiguous=%d%n",
        tapestries.size(), direct, opposite, none, ambiguous);
    if (tapestries.size() != 56) throw new IllegalStateException("expected 56 tapestry markers, got " + tapestries.size());
  }

  static void read(Path file, List<Marker> out) throws Exception {
    CompoundTag root;
    try (InputStream in = Files.newInputStream(file)) { root = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap()); }
    ListTag palette = root.getListOrEmpty("palette");
    ListTag blocks = root.getListOrEmpty("blocks");
    Map<Integer, Cell> states = new HashMap<>();
    for (int i=0;i<palette.size();i++) {
      CompoundTag p = (CompoundTag) palette.get(i);
      String name = p.getStringOr("Name", "");
      String facing = p.getCompoundOrEmpty("Properties").getStringOr("facing", "");
      states.put(i, new Cell(name, facing, ""));
    }
    Map<String, Cell> cells = new HashMap<>();
    for (int i=0;i<blocks.size();i++) {
      CompoundTag b=(CompoundTag)blocks.get(i); int state=b.getIntOr("state",-1);
      ListTag pos=b.getListOrEmpty("pos"); if (pos.size()!=3) continue;
      int x=pos.getInt(0).orElse(0), y=pos.getInt(1).orElse(0), z=pos.getInt(2).orElse(0);
      Cell c=states.getOrDefault(state,new Cell("<unknown>","",""));
      String meta=b.getCompoundOrEmpty("nbt").getStringOr("metadata", "");
      c=new Cell(c.name(),c.facing(),meta);
      cells.put(key(x,y,z),c);
    }
    for (var e:cells.entrySet()) if ("biomemakeover:directional_data".equals(e.getValue().name()) && "tapestry".equals(e.getValue().metadata())) {
      int[] p=parse(e.getKey()); Map<String,Cell> n=new LinkedHashMap<>();
      n.put("N",cells.getOrDefault(key(p[0],p[1],p[2]-1),new Cell("AIR","","")));
      n.put("S",cells.getOrDefault(key(p[0],p[1],p[2]+1),new Cell("AIR","","")));
      n.put("E",cells.getOrDefault(key(p[0]+1,p[1],p[2]),new Cell("AIR","","")));
      n.put("W",cells.getOrDefault(key(p[0]-1,p[1],p[2]),new Cell("AIR","","")));
      n.put("U",cells.getOrDefault(key(p[0],p[1]+1,p[2]),new Cell("AIR","","")));
      n.put("D",cells.getOrDefault(key(p[0],p[1]-1,p[2]),new Cell("AIR","","")));
      out.add(new Marker(file.toString(),p[0],p[1],p[2],e.getValue().metadata(),e.getValue().facing(),n));
    }
  }
  static String key(int x,int y,int z){return x+","+y+","+z;}
  static int[] parse(String s){String[] a=s.split(",");return new int[]{Integer.parseInt(a[0]),Integer.parseInt(a[1]),Integer.parseInt(a[2])};}
  static String neighbor(Marker m,String d){return m.neighbors().getOrDefault(letter(d),new Cell("AIR",""," ")).name();}
  static String letter(String d){return switch(d.toUpperCase(Locale.ROOT)){case "NORTH"->"N";case "SOUTH"->"S";case "EAST"->"E";case "WEST"->"W";case "UP"->"U";case "DOWN"->"D";default->"?";};}
  static boolean support(String n){return !n.isEmpty()&&!n.equals("AIR")&&!n.equals("minecraft:air")&&!n.equals("minecraft:cave_air")&&!n.equals("minecraft:void_air")&&!n.equals("minecraft:structure_void")&&!n.equals("biomemakeover:directional_data");}
  static String opposite(String d){return switch(d.toUpperCase(Locale.ROOT)){case "NORTH"->"S";case "SOUTH"->"N";case "EAST"->"W";case "WEST"->"E";case "UP"->"D";case "DOWN"->"U";default->"?";};}
}
