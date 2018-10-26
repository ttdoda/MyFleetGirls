import java.io.IOException;
import java.io.BufferedReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.security.MessageDigest;
import java.security.DigestInputStream;
import java.security.NoSuchAlgorithmException;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author ponkotuy, b-wind
 * Date: 15/03/09.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        try {
            List<String> urls = getProperties(Paths.get("update.properties"));
            for(String uStr : urls) {
                URL url = new URL(uStr);
                Path dst = Paths.get(url.getPath()).getFileName();
                if(!Files.exists(dst)) {
                    System.out.println(dst.getFileName() + "は存在しません。ダウンロードします。");
                }
                URLConnection conn = Connection.withRedirect(url, sha1sum(dst, true));
                if(conn == null) {
                    System.out.println(dst.getFileName() + "に変更はありません");
                } else {
                    Connection.download(conn, dst);
                    System.out.println(dst.getFileName() + "のダウンロードが完了しました");
                }
            }
        } catch(MalformedURLException e) {
            System.err.println("おやっ、URLの書式に異常です！ぽんこつさんが悪いです！");
            System.exit(1);
        } catch(IOException e) {
            System.err.println("おやっ、IOExceptionです！");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static List<String> getProperties(Path path) throws IOException {
        Properties p = new Properties();
        try(BufferedReader br = Files.newBufferedReader(path, UTF_8)) {
            p.load(br);
        }
        List<String> result = new ArrayList<>(p.size());
        for(Object prop : p.keySet()) {
            String key = (String) prop;
            result.add(p.getProperty(key));
        }
        return result;
    }

    private static String sha1sum(Path dst, Boolean isEnclosed) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            try(DigestInputStream is = new DigestInputStream(Files.newInputStream(dst), md)) {
                byte[] buf = new byte[1024];
                while (is.read(buf) != -1);
            }

            StringBuilder hash = new StringBuilder();
            for (byte b : md.digest()) {
                hash.append(String.format("%02x", b));
            }
            if(isEnclosed) { hash.insert(0, "\"").append("\""); }

            return hash.toString();
        } catch (NoSuchAlgorithmException | IOException | NullPointerException e) {
            return "";
        }
    }
}
