package sky_bai.mod.tym.lib.mcgltf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.*;
import sky_bai.mod.tym.lib.jgltf.model.GltfModel;
import sky_bai.mod.tym.lib.jgltf.model.io.Buffers;
import sky_bai.mod.tym.manager.GlTFModelManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;


public class MCglTF {

    public static final String RESOURCE_LOCATION = "resourceLocation";
    public static final Logger logger = LogManager.getLogger("McGlTF");
    private static final String SKINNING_SHADER = """
            #version 430
                        
            layout(location = 0) in vec4 joint;
            layout(location = 1) in vec4 weight;
            layout(location = 2) in vec3 position;
            layout(location = 3) in vec3 normal;
            layout(location = 4) in vec4 tangent;
                        
            layout(std430, binding = 0) readonly buffer jointMatrixBuffer {mat4 jointMatrices[];};
                        
            out vec3 outPosition;
            out vec3 outNormal;
            out vec4 outTangent;
                        
            void main {
                mat4 skinMatrix = weight.x * jointMatrices[int(joint.x)] + weight.y * jointMatrices[int(joint.y)] + weight.z * jointMatrices[int(joint.z)] + weight.w * jointMatrices[int(joint.w)];
                outPosition = (skinMatrix * vec4(position, 1.0)).xyz;
                mat3 upperLeft = mat3(skinMatrix);
                outNormal = upperLeft * normal;
                outTangent.xyz = upperLeft * tangent.xyz;
                outTangent.w = tangent.w;
            }
                        
            """;
    private static MCglTF INSTANCE;
    private final Map<ResourceLocation, Supplier<ByteBuffer>> loadedBufferResources = new HashMap<ResourceLocation, Supplier<ByteBuffer>>();
    private final Map<ResourceLocation, Supplier<ByteBuffer>> loadedImageResources = new HashMap<ResourceLocation, Supplier<ByteBuffer>>();
    private final List<Runnable> gltfRenderData = new ArrayList<Runnable>();
    private int glProgramSkinning = -1;
    private int defaultColorMap;
    private int defaultNormalMap;
    private AbstractTexture lightTexture;


    public MCglTF() {
    }

    public static MCglTF getInstance() {
        if (INSTANCE == null) INSTANCE = new MCglTF();
        return INSTANCE;
    }

    public static void lookup(Map<String, MutablePair<GltfModel, List<GlTFModelManager.ModelData>>> lookup, GlTFModelManager.ModelData data) {
        String name = data.name;
        GltfModel model = data.model;
        MutablePair<GltfModel, List<GlTFModelManager.ModelData>> pair = lookup.get(name);
        if (pair == null) {
            pair = MutablePair.of(model, new ArrayList<>());
            lookup.put(name, pair);
        }
        pair.getRight().add(data);
    }

    public int getGlProgramSkinning() {
        return glProgramSkinning;
    }

    public int getDefaultColorMap() {
        return defaultColorMap;
    }

    public void setDefaultColorMap(int defaultColorMap) {
        this.defaultColorMap = defaultColorMap;
    }

    public int getDefaultNormalMap() {
        return defaultNormalMap;
    }

    public void setDefaultNormalMap(int defaultNormalMap) {
        this.defaultNormalMap = defaultNormalMap;
    }

    public int getDefaultSpecularMap() {
        return 0;
    }

    public AbstractTexture getLightTexture() {
        return lightTexture;
    }

    public void setLightTexture(AbstractTexture lightTexture) {
        this.lightTexture = lightTexture;
    }

    public ByteBuffer getBufferResource(ResourceLocation location) {
        Supplier<ByteBuffer> supplier;
        synchronized (loadedBufferResources) {
            supplier = loadedBufferResources.get(location);
            if (supplier == null) {
                supplier = new Supplier<ByteBuffer>() {
                    ByteBuffer bufferData;

                    @Override
                    public synchronized ByteBuffer get() {
                        if (bufferData == null) {
                            try (Resource resource = Minecraft.getInstance().getResourceManager().getResource(location)) {
                                bufferData = Buffers.create(IOUtils.toByteArray(new BufferedInputStream(resource.getInputStream())));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return bufferData;
                    }

                };
                loadedBufferResources.put(location, supplier);
            }
        }
        return supplier.get();
    }

    public ByteBuffer getImageResource(ResourceLocation location) {
        Supplier<ByteBuffer> supplier;
        synchronized (loadedImageResources) {
            supplier = loadedImageResources.get(location);
            if (supplier == null) {
                supplier = new Supplier<ByteBuffer>() {
                    ByteBuffer bufferData;

                    @Override
                    public synchronized ByteBuffer get() {
                        if (bufferData == null) {
                            try (Resource resource = Minecraft.getInstance().getResourceManager().getResource(location)) {
                                bufferData = Buffers.create(IOUtils.toByteArray(new BufferedInputStream(resource.getInputStream())));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return bufferData;
                    }

                };
                loadedImageResources.put(location, supplier);
            }
        }
        return supplier.get();
    }

    public Map<ResourceLocation, Supplier<ByteBuffer>> getLoadedImageResources() {
        return loadedImageResources;
    }

    public Map<ResourceLocation, Supplier<ByteBuffer>> getLoadedBufferResources() {
        return loadedBufferResources;
    }

    public List<Runnable> getGltfRenderData() {
        return gltfRenderData;
    }

    public void createSkinningProgram() {
        int glShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(glShader, SKINNING_SHADER);
        GL20.glCompileShader(glShader);

        glProgramSkinning = GL20.glCreateProgram();
        GL20.glAttachShader(glProgramSkinning, glShader);
        GL20.glDeleteShader(glShader);
        GL30.glTransformFeedbackVaryings(glProgramSkinning, new CharSequence[]{"outPosition", "outNormal", "outTangent"}, GL30.GL_SEPARATE_ATTRIBS);
        GL20.glLinkProgram(glProgramSkinning);
    }

    private void processRenderedGltfModels(Map<String, MutablePair<GltfModel, List<GlTFModelManager.ModelData>>> lookup, BiFunction<List<Runnable>, GltfModel, RenderedGltfModel> renderedGltfModelBuilder) {
        lookup.forEach((modelLocation, receivers) -> {
            List<GlTFModelManager.ModelData> list = receivers.getRight();
            for (GlTFModelManager.ModelData data : list) {
                if (data.getData().isReceiveSharedModel(receivers.getLeft(), gltfRenderData)) {
                    RenderedGltfModel renderedModel = renderedGltfModelBuilder.apply(gltfRenderData, receivers.getLeft());
                    data.getData().onReceiveSharedModel(renderedModel);
                }
            }

            /**Iterator<GlTFModelManager.ModelData> iterator = receivers.getRight().iterator();
             do {
             GlTFModelManager.ModelData receiver = iterator.next();
             if (receiver.getData().isReceiveSharedModel(receivers.getLeft(), gltfRenderData)) {
             RenderedGltfModel renderedModel = renderedGltfModelBuilder.apply(gltfRenderData, receivers.getLeft());
             receiver.getData().onReceiveSharedModel(renderedModel);
             while (iterator.hasNext()) {
             receiver = iterator.next();
             if (receiver.getData().isReceiveSharedModel(receivers.getLeft(), gltfRenderData)) {
             receiver.getData().onReceiveSharedModel(renderedModel);
             }
             }
             return;
             }
             } while (iterator.hasNext());*/
        });
    }

    public void processRenderedGltfModels(Map<String, MutablePair<GltfModel, List<GlTFModelManager.ModelData>>> lookup) {
        processRenderedGltfModels(lookup, RenderedGltfModel::new);
        GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0);
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
        GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);
    }

    public void initializeGL() {
        Minecraft.getInstance().execute(() -> {
            setLightTexture(Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation("dynamic/light_map_1")));

            //Since max OpenGL version on Windows from GLCapabilities will always return 3.2 as of Minecraft 1.17, this is a workaround to check if OpenGL 4.3 is available.
            createSkinningProgram();

            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

            int currentTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

            setDefaultColorMap(GL11.glGenTextures());
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, getDefaultColorMap());
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, Buffers.create(new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}));
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);

            setDefaultNormalMap(GL11.glGenTextures());
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, getDefaultNormalMap());
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, Buffers.create(new byte[]{-128, -128, -1, -1, -128, -128, -1, -1, -128, -128, -1, -1, -128, -128, -1, -1}));
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);
        });
    }

    public void reloadManager() {
        List<Runnable> gltfRenderData = getGltfRenderData();
        gltfRenderData.forEach(Runnable::run);
        gltfRenderData.clear();

        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

        int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        Map<String, MutablePair<GltfModel, List<GlTFModelManager.ModelData>>> lookup = new HashMap<>();

        GlTFModelManager.getManager().getGlTF().forEach(data -> MCglTF.lookup(lookup, data));
        MCglTF.lookup(lookup, GlTFModelManager.getManager().getDefaultModelData());

        processRenderedGltfModels(lookup);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);

        getLoadedBufferResources().clear();
        getLoadedImageResources().clear();
    }

}
