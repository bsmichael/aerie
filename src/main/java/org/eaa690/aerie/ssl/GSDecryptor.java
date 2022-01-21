/*
 *  Copyright (C) 2021 Gwinnett County Experimental Aircraft Association
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.eaa690.aerie.ssl;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;
import org.eaa690.aerie.config.CommonConstants;

import java.nio.charset.StandardCharsets;

/**
 * Decrypts encrypted values.
 */
@Slf4j
public class GSDecryptor {

    /**
     * Secret Key.
     */
    private final PaddedBufferedBlockCipher cipher;

    /**
     * Contructor.
     *
     * @param secretKey secret key
     * @param initVector init vector
     */
    public GSDecryptor(final String secretKey, final String initVector) {
        AESEngine engine = new AESEngine();
        CBCBlockCipher blockCipher = new CBCBlockCipher(engine);
        cipher = new PaddedBufferedBlockCipher(blockCipher);
        KeyParameter keyParam = new KeyParameter(Base64.decode(secretKey));
        cipher.init(false, new ParametersWithIV(keyParam, initVector.getBytes(StandardCharsets.UTF_8), 0,
                CommonConstants.SIXTEEN));
    }

    /**
     * Decrypts provided encrypted value.
     *
     * @param encryptedDataStr encrypted data
     * @return decrypted value
     */
    public String decrypt(final String encryptedDataStr) throws InvalidCipherTextException {
        byte[] out2 = Base64.decode(encryptedDataStr);
        byte[] comparisonBytes = new byte[cipher.getOutputSize(out2.length)];
        int length = cipher.processBytes(out2, 0, out2.length, comparisonBytes, 0);
        cipher.doFinal(comparisonBytes, length);
        return new String(comparisonBytes).trim();
    }
}
