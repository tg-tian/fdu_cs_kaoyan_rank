package fdu.kaoyanrank.utils;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmacUtil {
    private static final String HMAC_SHA_256 = "HmacSHA256";

    @Value("${app.hmac.secret}")
    private String secret;

    public String hmacSha256Hex(String data) {
        byte[] hash = hmacSha256(data);
        return HexFormat.of().formatHex(hash);
    }

    public byte[] hmacSha256(String data) {
        return hmacSha256(
            data.getBytes(StandardCharsets.UTF_8),
            secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    private byte[] hmacSha256(byte[] data, byte[] secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            SecretKeySpec keySpec = new SecretKeySpec(secret, HMAC_SHA_256);
            mac.init(keySpec);
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 计算失败", e);
        }
    }
}
