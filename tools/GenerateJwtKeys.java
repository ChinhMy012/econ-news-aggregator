import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class GenerateJwtKeys {

        public static void main(String[] args) throws Exception {
                if (args.length != 1) {
                        throw new IllegalArgumentException(
                                        "Cần truyền thư mục lưu khóa.");
                }

                Path directory = Path.of(args[0]);
                Path privateFile = directory.resolve("private-key.pem");
                Path publicFile = directory.resolve("public-key.pem");

                if (Files.exists(privateFile) || Files.exists(publicFile)) {
                        throw new IllegalStateException(
                                        "Đã có file khóa. Không tự động ghi đè.");
                }

                Files.createDirectories(directory);

                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

                generator.initialize(3072);
                var keyPair = generator.generateKeyPair();

                Files.writeString(
                                privateFile,
                                pem("PRIVATE KEY", keyPair.getPrivate().getEncoded()),
                                StandardCharsets.US_ASCII,
                                StandardOpenOption.CREATE_NEW);

                Files.writeString(
                                publicFile,
                                pem("PUBLIC KEY", keyPair.getPublic().getEncoded()),
                                StandardCharsets.US_ASCII,
                                StandardOpenOption.CREATE_NEW);

                System.out.println("Đã tạo cặp khóa RSA.");
        }

        private static String pem(String label, byte[] bytes) {
                String content = Base64.getMimeEncoder(
                                64,
                                new byte[] { '\n' }).encodeToString(bytes);

                return "-----BEGIN " + label + "-----\n"
                                + content
                                + "\n-----END " + label + "-----\n";
        }
}