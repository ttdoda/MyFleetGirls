import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;

/**
 * @author ponkotuy
 * @since 15/03/11.
 */
public class Connection {
    private static String USER_AGENT = null;

    static {
        USER_AGENT = String.format("MyFleetGirls Updater w/%s (%s)", System.getProperty("java.vm.version"), System.getProperty("os.name"));
    }

    static URLConnection withRedirect(URL url, String etag) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("Accept-Encoding", "pack200-gzip");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setUseCaches(false);
        conn.setRequestProperty("If-None-Match", etag);
        HttpURLConnection http = (HttpURLConnection) conn;

        http.connect();
        int code = http.getResponseCode();
        if(code == HTTP_NOT_MODIFIED) {
            return null;
        } else if(300 <= code && code < 400) { // Redirect
            URL newUrl = new URL(http.getHeaderField("Location"));
            return withRedirect(newUrl, etag);
        } else if(200 <= code && code < 300) {
            return http;
        } else {
            throw new IOException("Error status code: " + code);
        }
    }

    static void download(URLConnection conn, Path dst) throws IOException {
        String content = conn.getHeaderField("Content-Encoding");
        Path tempFile = Files.createTempFile("myfleetgirls",null);
        tempFile.toFile().deleteOnExit();
        try(InputStream is = conn.getInputStream()) {
            if(content == null) {
                Files.copy(is, tempFile, REPLACE_EXISTING);
                Files.copy(tempFile, dst, REPLACE_EXISTING);
            } else switch (content) {
                case "pack200-gzip":
                    pack20ODownload(conn, tempFile);
                    Files.copy(tempFile, dst, REPLACE_EXISTING);
                    break;
                case "gzip":
                    try(GZIPInputStream gzipIs = new GZIPInputStream(is)){
                        Files.copy(gzipIs, tempFile, REPLACE_EXISTING);
                    }
                    Files.copy(tempFile, dst, REPLACE_EXISTING);
                    break;
                default:
                    Files.copy(is, tempFile, REPLACE_EXISTING);
                    Files.copy(tempFile, dst, REPLACE_EXISTING);
            }
        }finally{
            Files.deleteIfExists(tempFile);
        }
    }

    private static void pack20ODownload(URLConnection conn, Path dst) throws IOException {
        try(InputStream is = conn.getInputStream();
            GZIPInputStream gzipIs = new GZIPInputStream(is);
            OutputStream os = Files.newOutputStream(dst, WRITE, CREATE, TRUNCATE_EXISTING);
            JarOutputStream jar = new JarOutputStream(os)) {
            Pack200.Unpacker unpacker = Pack200.newUnpacker();
            unpacker.unpack(gzipIs, jar);
        }
    }
}
