package com.pocketmoney.service;

import com.pocketmoney.entity.AppConfig;
import com.pocketmoney.repository.AppConfigRepository;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

@Service
public class VapidService {

    private static final String KEY_PUBLIC = "vapid.public";
    private static final String KEY_PRIVATE = "vapid.private";

    private final AppConfigRepository configRepo;
    private String publicKey;
    private String privateKey;

    public VapidService(AppConfigRepository configRepo) {
        this.configRepo = configRepo;
    }

    @PostConstruct
    @Transactional
    public void init() throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        var pub = configRepo.findById(KEY_PUBLIC);
        var priv = configRepo.findById(KEY_PRIVATE);

        if (pub.isPresent() && priv.isPresent()) {
            this.publicKey = pub.get().getConfigValue();
            this.privateKey = priv.get().getConfigValue();
            return;
        }

        // 최초 1회 키 생성 후 DB 저장 (이후 재배포에도 유지)
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("prime256v1");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", "BC");
        kpg.initialize(spec);
        KeyPair keyPair = kpg.generateKeyPair();

        BCECPublicKey pk = (BCECPublicKey) keyPair.getPublic();
        byte[] pubBytes = pk.getQ().getEncoded(false); // 65 bytes (0x04 | X | Y)
        this.publicKey = Base64.getUrlEncoder().withoutPadding().encodeToString(pubBytes);

        BCECPrivateKey sk = (BCECPrivateKey) keyPair.getPrivate();
        byte[] dBytes = sk.getD().toByteArray();
        // BigInteger는 가끔 leading sign byte 0x00을 붙이거나 32바이트 미만일 수 있음 → 정확히 32바이트로
        byte[] priv32 = new byte[32];
        if (dBytes.length == 33 && dBytes[0] == 0) {
            System.arraycopy(dBytes, 1, priv32, 0, 32);
        } else if (dBytes.length <= 32) {
            System.arraycopy(dBytes, 0, priv32, 32 - dBytes.length, dBytes.length);
        } else {
            throw new IllegalStateException("Unexpected private key length: " + dBytes.length);
        }
        this.privateKey = Base64.getUrlEncoder().withoutPadding().encodeToString(priv32);

        configRepo.save(new AppConfig(KEY_PUBLIC, this.publicKey));
        configRepo.save(new AppConfig(KEY_PRIVATE, this.privateKey));
    }

    public String getPublicKey() { return publicKey; }
    public String getPrivateKey() { return privateKey; }
}
