package br.com.fiap.api.config;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.Base64;

public class Senha {
    public static String gerar(String senha) {
        byte[] salt=new byte[16]; new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(derivar(senha,salt));
    }
    public static boolean conferir(String senha, String hash) {
        String[] partes=hash.split(":");
        return MessageDigest.isEqual(derivar(senha,Base64.getDecoder().decode(partes[0])),Base64.getDecoder().decode(partes[1]));
    }
    private static byte[] derivar(String senha, byte[] salt) {
        PBEKeySpec spec=new PBEKeySpec(senha.toCharArray(),salt,600000,256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (GeneralSecurityException e) { throw new IllegalStateException("Falha ao proteger senha",e); }
        finally { spec.clearPassword(); }
    }
}
