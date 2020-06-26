package net.aeronica.mods.mxtune.model;

import com.google.gson.*;
import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

@SuppressWarnings("deprecation")
public class ModelLoader implements ICustomModelLoader, JsonDeserializationContext
{
    public static final ModelLoader INSTANCE = new ModelLoader();
    public static String EXTENSION = ".mtex";

    private final Gson gson = new GsonBuilder().registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3fDeserializer()).registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransformsDeserializer()).create();
    private final JsonParser parser = new JsonParser();
    private final ModelBlock.Deserializer modelBlockDeserializer = new ModelBlock.Deserializer();
    private IResourceManager manager;

    private ModelLoader() { /* NOP */ }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws IOException
    {
        String modelPath = modelLocation.getPath();
        modelPath = modelPath.substring(0, modelPath.lastIndexOf('.')) + ".mtex.json";
        IResource resource = this.manager.getResource(new ResourceLocation(modelLocation.getNamespace(), modelPath));
        InputStreamReader jsonStream = new InputStreamReader(resource.getInputStream());
        JsonElement json = this.parser.parse(jsonStream);
        jsonStream.close();
        ModelBlock modelBlock = this.modelBlockDeserializer.deserialize(json, ModelBlock.class, this);
        return new ModelMultiTex(modelBlock);
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        if (modelLocation.getNamespace().contains(Reference.MOD_ID))
            ModLogger.debug("ModelLoader#accepts %s. accept=%s", modelLocation.toString(), modelLocation.getPath().endsWith(EXTENSION));
        return modelLocation.getNamespace().contains(Reference.MOD_ID) && modelLocation.getPath().endsWith(EXTENSION);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        manager = resourceManager;
    }

    @Override
    public <T> T deserialize(JsonElement json, Type type) throws JsonParseException
    {
        return this.gson.fromJson(json, type);
    }
}
