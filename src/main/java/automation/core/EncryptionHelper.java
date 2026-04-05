package automation.core;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.Base64;

/**
 * Encryption helper using BouncyCastle.
 */
public class EncryptionHelper
{

    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static String encodeBase64(String input)
    {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public static String decodeBase64(String input)
    {
        return new String(Base64.getDecoder().decode(input));
    }

    /**
     * Decrypt a value using the provided token as key (XOR-based)
     */
    public static String decrypt(String encryptedValue, String thanosToken)
    {
        try
        {
            byte[] decoded = Base64.getDecoder().decode(encryptedValue);
            byte[] key = thanosToken.getBytes();
            byte[] result = new byte[decoded.length];
            for (int i = 0; i < decoded.length; i++)
            {
                result[i] = (byte) (decoded[i] ^ key[i % key.length]);
            }
            return new String(result);
        }
        catch (Exception e)
        {
            Log.error("Decryption failed: " + e.getMessage());
            return encryptedValue;
        }
    }

    public static String encrypt(String value, String key)
    {
        try
        {
            byte[] valueBytes = value.getBytes();
            byte[] keyBytes = key.getBytes();
            byte[] result = new byte[valueBytes.length];
            for (int i = 0; i < valueBytes.length; i++)
            {
                result[i] = (byte) (valueBytes[i] ^ keyBytes[i % keyBytes.length]);
            }
            return Base64.getEncoder().encodeToString(result);
        }
        catch (Exception e)
        {
            Log.error("Encryption failed: " + e.getMessage());
            return value;
        }
    }
}
