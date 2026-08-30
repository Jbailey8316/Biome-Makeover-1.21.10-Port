package party.lemons.biomemakeover.worldgen.mansion;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface TemplateGetter
{
	List<ResourceLocation> getTemplate(MansionTemplates feature);
}