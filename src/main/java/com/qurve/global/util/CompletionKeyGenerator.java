package com.qurve.global.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

/**
 * 동일한 문제 묶음의 완료 처리를 한 번만 기록하기 위한 키를 생성합니다.
 */
public final class CompletionKeyGenerator {

    private CompletionKeyGenerator() {
    }

    public static String generate(Collection<Long> problemIds) {
        String source = problemIds.stream()
                .distinct()
                .sorted()
                .map(String::valueOf)
                .reduce((left, right) -> left + "," + right)
                .orElseThrow(() -> new IllegalArgumentException("problemIds must not be empty"));

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : hash) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}
