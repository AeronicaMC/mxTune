package net.aeronica.mods.mxtune.tabula;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.aeronica.mods.mxtune.tabula.baked.VanillaTabulaModel;
import net.aeronica.mods.mxtune.tabula.baked.deserializer.ItemCameraTransformsDeserializer;
import net.aeronica.mods.mxtune.tabula.baked.deserializer.ItemTransformVec3fDeserializer;
import net.aeronica.mods.mxtune.tabula.components.TabulaCubeContainer;
import net.aeronica.mods.mxtune.tabula.components.TabulaCubeGroupContainer;
import net.aeronica.mods.mxtune.tabula.components.TabulaModelContainer;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author pau101
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public enum TabulaModelLoader implements ICustomModelLoader, JsonDeserializationContext {
    INSTANCE;
    
    private final Set<String> enabledDomains = new HashSet<String>();
    private Gson gson = new GsonBuilder().registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3fDeserializer()).registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransformsDeserializer()).create();
    private JsonParser parser = new JsonParser();
    private ModelBlock.Deserializer modelBlockDeserializer = new ModelBlock.Deserializer();
    private IResourceManager manager;

    public void addDomain(String domain)
    {
        enabledDomains.add(domain.toLowerCase());
        ModLogger.info("TabulaModelLoader: Domain %s has been added.", domain.toLowerCase());
    }

    /**
     * Load a {@link TabulaModelContainer} from the path. A slash will be added if it isn't in the path already.
     *
     * @param path the model path
     * @return the new {@link TabulaModelContainer} instance
     * @throws IOException if the file can't be found
     */
    public TabulaModelContainer loadTabulaModel(String path) throws IOException {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith(TabulaUtils.EXT)) {
            path += TabulaUtils.EXT;
        }
        InputStream stream = TabulaModelLoader.class.getResourceAsStream(path);
        return TabulaModelLoader.INSTANCE.loadTabulaModel(this.getModelJsonStream(path, stream));
    }

    /**
     * Load a {@link TabulaModelContainer} from the model.json input stream.
     *
     * @param stream the model.json input stream
     * @return the new {@link TabulaModelContainer} instance
     */
    public TabulaModelContainer loadTabulaModel(InputStream stream) {
        return this.gson.fromJson(new InputStreamReader(stream), TabulaModelContainer.class);
    }

    /**
     * @param name  the cube name
     * @param model the model container
     * @return the cube
     */
    public TabulaCubeContainer getCubeByName(String name, TabulaModelContainer model) {
        List<TabulaCubeContainer> allCubes = this.getAllCubes(model);

        for (TabulaCubeContainer cube : allCubes) {
            if (cube.getName().equals(name)) {
                return cube;
            }
        }

        return null;
    }

    /**
     * @param identifier the cube identifier
     * @param model      the model container
     * @return the cube
     */
    public TabulaCubeContainer getCubeByIdentifier(String identifier, TabulaModelContainer model) {
        List<TabulaCubeContainer> allCubes = this.getAllCubes(model);

        for (TabulaCubeContainer cube : allCubes) {
            if (cube.getIdentifier().equals(identifier)) {
                return cube;
            }
        }

        return null;
    }

    /**
     * @param model the model container
     * @return an array with all cubes of the model
     */
    public List<TabulaCubeContainer> getAllCubes(TabulaModelContainer model) {
        List<TabulaCubeContainer> cubes = new ArrayList<>();

        for (TabulaCubeGroupContainer cubeGroup : model.getCubeGroups()) {
            cubes.addAll(this.traverse(cubeGroup));
        }

        for (TabulaCubeContainer cube : model.getCubes()) {
            cubes.addAll(this.traverse(cube));
        }

        return cubes;
    }

    private List<TabulaCubeContainer> traverse(TabulaCubeGroupContainer group) {
        List<TabulaCubeContainer> retCubes = new ArrayList<>();

        for (TabulaCubeContainer child : group.getCubes()) {
            retCubes.addAll(this.traverse(child));
        }

        for (TabulaCubeGroupContainer child : group.getCubeGroups()) {
            retCubes.addAll(this.traverse(child));
        }

        return retCubes;
    }

    private List<TabulaCubeContainer> traverse(TabulaCubeContainer cube) {
        List<TabulaCubeContainer> retCubes = new ArrayList<>();

        retCubes.add(cube);

        for (TabulaCubeContainer child : cube.getChildren()) {
            retCubes.addAll(this.traverse(child));
        }

        return retCubes;
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
//        return modelLocation.getResourcePath().endsWith(TabulaUtils.TabulaUtils.EXT);
        return enabledDomains.contains(modelLocation.getResourceDomain()) && modelLocation.getResourcePath().endsWith(TabulaUtils.EXT);
    }
    
    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws IOException {
        String modelPath = modelLocation.getResourcePath();
        modelPath = modelPath.substring(0, modelPath.lastIndexOf('.')) + ".json";
        IResource resource = this.manager.getResource(new ResourceLocation(modelLocation.getResourceDomain(), modelPath));
        InputStreamReader jsonStream = new InputStreamReader(resource.getInputStream());
        JsonElement json = this.parser.parse(jsonStream);
        jsonStream.close();
        ModelBlock modelBlock = this.modelBlockDeserializer.deserialize(json, ModelBlock.class, this);
        String tblLocationStr = json.getAsJsonObject().get("tabula").getAsString() + TabulaUtils.EXT;
        ResourceLocation tblLocation = new ResourceLocation(tblLocationStr);
        IResource tblResource = this.manager.getResource(tblLocation);
        InputStream modelStream = this.getModelJsonStream(tblLocation.toString(), tblResource.getInputStream());
        TabulaModelContainer modelJson = TabulaModelLoader.INSTANCE.loadTabulaModel(modelStream);
        modelStream.close();
        ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
        int layer = 0;
        String texture;
        while ((texture = modelBlock.textures.get("layer" + layer++)) != null) {
            builder.add(new ResourceLocation(texture));
        }
        String particle = modelBlock.textures.get("particle");
        return new VanillaTabulaModel(modelJson, particle != null ? new ResourceLocation(particle) : null,builder.build(), IPerspectiveAwareModel.MapWrapper.getTransforms(modelBlock.getAllTransforms()));
    }

    private InputStream getModelJsonStream(String name, InputStream file) throws IOException {
        ZipInputStream zip = new ZipInputStream(file);
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.getName().equals("model.json")) {
                return zip;
            }
        }
        throw new RuntimeException("No model.json present in " + name);
    }

    @Override
    public <T> T deserialize(JsonElement json, Type type) throws JsonParseException {
        return this.gson.fromJson(json, type);
    }
}
