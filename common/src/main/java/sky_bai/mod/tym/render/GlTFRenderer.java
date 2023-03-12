package sky_bai.mod.tym.render;

import com.google.common.collect.Lists;
import org.lwjgl.opengl.GL15;
import sky_bai.mod.lib.jgltf.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class GlTFRenderer {

    final GltfModel model;

    final List<IntBuffer> buffers;


    public GlTFRenderer(GltfModel model) {
        this.model = model;
        List<BufferModel> bufferModels = model.getBufferModels();
        buffers = Lists.newArrayList();
        bufferModels.forEach(bufferModel -> buffers.add(bufferModel.getBufferData().asIntBuffer()));

        glEnable(GL_TEXTURE_2D);
    }


    public void load() {
        loadData();
        loadImage();
        loadScene();
    }

    public void loadData() {
        for (IntBuffer buffer : buffers) {
            int vbo_key = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo_key);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
    }

    private byte[] getBytes(ByteBuffer buffer) {
        ByteBuffer b = buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = b.get();
        }
        return bytes;
    }

    public void loadImage() {
        List<ImageModel> imageModels = model.getImageModels();
        for (ImageModel imageModel : imageModels) {
            ByteBuffer buffer = imageModel.getImageData();
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(getBytes(buffer)));

                int width = image.getWidth();
                int height = image.getHeight();

                int vbo_key = glGenTextures();
                glBindTexture(GL_TEXTURE_2D, vbo_key);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer.rewind());

                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

                glBindTexture(GL_TEXTURE_2D, 0);
            } catch (IOException e) {

            }
        }
    }

    void loadScene() {
        List<SceneModel> scenes = model.getSceneModels();
        for (SceneModel scene : scenes) {
            List<NodeModel> nodes = scene.getNodeModels();
            for (NodeModel node : nodes) {
                loadNode(node);
            }
        }
        glBindVertexArray(0);
    }


    public void loadNode(NodeModel node) {
        for (NodeModel child : node.getChildren()) {
            loadNode(child);
        }
        List<MeshModel> meshModels = node.getMeshModels();
        for (MeshModel mesh : meshModels) {
            loadMeshModel(mesh);
        }
    }

    public void loadMeshModel(MeshModel mesh) {
        List<MeshPrimitiveModel> primitives = mesh.getMeshPrimitiveModels();
        int va_key = glGenVertexArrays();
        glBindVertexArray(va_key);
        int i = 0;
        for (MeshPrimitiveModel primitive : primitives) {
            glEnableVertexAttribArray(i);
            for (Map.Entry<String, AccessorModel> entry : primitive.getAttributes().entrySet()) {
                String name = entry.getKey();
                AccessorModel value = entry.getValue();
                int vbo_id = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, vbo_id);
                ByteBuffer buffer = value.getAccessorData().createByteBuffer();
                glBufferData(GL_ARRAY_BUFFER,buffer.rewind(),GL_STATIC_DRAW);
                glVertexAttribPointer(i,value.getElementType().getNumComponents(),
                        value.getComponentType(),false,
                        value.getByteStride(),value.getByteOffset());
                glBindBuffer(GL_ARRAY_BUFFER, 0);
            }
            AccessorModel indices = primitive.getIndices();
            if (indices != null) {
                int vbo_id = glGenBuffers();
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo_id);
                glDrawElements(primitive.getMode(),indices.getCount(),indices.getComponentType(),indices.getByteOffset());
                glBindBuffer(GL_ARRAY_BUFFER, 0);
            }
            i++;
        }
        glBindVertexArray(0);
    }


}
