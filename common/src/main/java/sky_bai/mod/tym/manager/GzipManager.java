package sky_bai.mod.tym.manager;

import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipManager {

    public static byte[] gzip(byte[] bytes) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream zos = new GZIPOutputStream(bos)) {
            zos.write(bytes);
            zos.finish();
            return bos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static byte[] unGzip(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             GZIPInputStream zis = new GZIPInputStream(bis)) {
            return IOUtils.toByteArray(zis);
        } catch (IOException e) {
            return new byte[0];
        }
    }

}
