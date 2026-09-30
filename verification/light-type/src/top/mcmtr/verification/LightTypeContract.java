package top.mcmtr.verification;

import org.mtr.mapping.holder.LightType;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

/** Runs against the selected loader's real resolved runtime classpath, without a game. */
public final class LightTypeContract {
    public static void main(String[] args) throws Exception {
        check(LightType.getBlockMapped() == LightType.BLOCK, "BLOCK helper identity");
        check(LightType.getSkyMapped() == LightType.SKY, "SKY helper identity");
        for (int i = 0; i < 1000; i++) {
            check(LightType.getBlockMapped() == LightType.BLOCK, "Repeated BLOCK identity");
            check(LightType.getSkyMapped() == LightType.SKY, "Repeated SKY identity");
        }
        // Verify the real native enum order behind convert(values()[ordinal]), too.
        Enum<?> sky = (Enum<?>) LightType.class.getField("data").get(LightType.SKY);
        Enum<?> block = (Enum<?>) LightType.class.getField("data").get(LightType.BLOCK);
        check(sky.ordinal() == LightType.SKY.ordinal(), "Native SKY ordinal");
        check(block.ordinal() == LightType.BLOCK.ordinal(), "Native BLOCK ordinal");
        describe(LightType.class);
        describe(sky.getClass());
        System.out.println("PASS: resolved LightType BLOCK/SKY helpers return the exact singleton identities");
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void describe(Class<?> type) throws Exception {
        URL location = type.getProtectionDomain().getCodeSource().getLocation();
        Path path = Path.of(location.toURI());
        System.out.println(type.getName() + " from " + path);
        if (Files.isRegularFile(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (java.io.InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[65536];
                for (int read; (read = input.read(buffer)) >= 0;) digest.update(buffer, 0, read);
            }
            StringBuilder hash = new StringBuilder();
            for (byte value : digest.digest()) hash.append(String.format("%02x", value & 255));
            System.out.println("SHA-256 " + hash);
        }
    }
}
