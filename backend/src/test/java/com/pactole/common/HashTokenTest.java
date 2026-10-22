package com.pactole.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class HashTokenTest {

    @Test
    void hash_producesLowerHexStringOf64Chars() {
        String result = HashToken.hash("test-token");
        assertThat(result).matches("[0-9a-f]{64}");
    }

    @Test
    void hash_isDeterministic() {
        assertThat(HashToken.hash("abc")).isEqualTo(HashToken.hash("abc"));
    }

    @Test
    void hash_differentInputs_produceDifferentHashes() {
        assertThat(HashToken.hash("token-1")).isNotEqualTo(HashToken.hash("token-2"));
    }

    @Test
    void hash_emptyString_knownSha256Value() {
        assertThat(HashToken.hash(""))
            .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }
}
