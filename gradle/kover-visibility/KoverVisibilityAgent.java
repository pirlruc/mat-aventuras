package pt.mataventuras.gradle;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * Gradle 9.8 deprecates {@code Configuration.setVisible}. Kover 0.9.11, AGP 9.5,
 * KSP, and Detekt still call it, and {@code org.gradle.warning.mode=fail} stops
 * the build. No release of those plugins has dropped the call. This agent removes
 * the invokes in those packages only. A call from this repo's own scripts still
 * fails the build. On Gradle 9 the visibility flag no longer changes resolution.
 */
public final class KoverVisibilityAgent {
    private static final String[] PREFIXES = {
        "kotlinx/kover/",
        "com/android/build/",
        "com/google/devtools/ksp/",
        "dev/detekt/",
        "io/gitlab/arturbosch/detekt/",
    };

    private KoverVisibilityAgent() {}

    public static void premain(String args, Instrumentation value) {
        value.addTransformer(new Patcher(), false);
    }

    static final class Patcher implements ClassFileTransformer {
        @Override
        public byte[] transform(
                Module module,
                ClassLoader loader,
                String className,
                Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain,
                byte[] classfileBuffer) {
            if (!targeted(className)) {
                return null;
            }
            byte[] patched = dropSetVisible(classfileBuffer);
            return patched == classfileBuffer ? null : patched;
        }
    }

    private static boolean targeted(String className) {
        if (className == null) {
            return false;
        }
        String name = className.replace('.', '/');
        for (String prefix : PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    static byte[] dropSetVisible(byte[] input) {
        if (input.length < 10 || readU4(input, 0) != 0xCAFEBABE) {
            return input;
        }
        int count = readU2(input, 8);
        int[] offsets = new int[count];
        int cursor = 10;
        for (int index = 1; index < count; index++) {
            offsets[index] = cursor;
            int tag = input[cursor] & 0xFF;
            cursor += 1;
            switch (tag) {
                case 1 -> cursor += 2 + readU2(input, cursor);
                case 7, 8, 16, 19, 20 -> cursor += 2;
                case 15 -> cursor += 3;
                case 3, 4, 9, 10, 11, 12, 17, 18 -> cursor += 4;
                case 5, 6 -> {
                    cursor += 8;
                    index++;
                }
                default -> {
                    return input;
                }
            }
        }
        Set<Integer> targets = new HashSet<>();
        for (int index = 1; index < count; index++) {
            if (offsets[index] == 0) {
                continue;
            }
            int tag = input[offsets[index]] & 0xFF;
            if (tag != 10 && tag != 11) {
                continue;
            }
            int nameAndType = readU2(input, offsets[index] + 3);
            int name = readU2(input, offsets[nameAndType] + 1);
            int descriptor = readU2(input, offsets[nameAndType] + 3);
            if ("setVisible".equals(utf8(input, offsets[name]))
                    && "(Z)Lorg/gradle/api/artifacts/Configuration;".equals(utf8(input, offsets[descriptor]))) {
                targets.add(index);
            }
        }
        if (targets.isEmpty()) {
            return input;
        }
        byte[] output = input.clone();
        boolean changed = false;
        for (int at = cursor; at + 5 < output.length; at++) {
            if ((output[at] & 0xFF) != 0xB9 || (output[at + 5] & 0xFF) != 0x57) {
                continue;
            }
            int method = readU2(output, at + 1);
            if (!targets.contains(method) || (output[at + 3] & 0xFF) != 2) {
                continue;
            }
            output[at] = 0x58;
            output[at + 1] = 0x00;
            output[at + 2] = 0x00;
            output[at + 3] = 0x00;
            output[at + 4] = 0x00;
            output[at + 5] = 0x00;
            changed = true;
        }
        return changed ? output : input;
    }

    private static String utf8(byte[] bytes, int offset) {
        int length = readU2(bytes, offset + 1);
        return new String(bytes, offset + 3, length, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static int readU2(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF);
    }

    private static int readU4(byte[] bytes, int offset) {
        return (readU2(bytes, offset) << 16) | readU2(bytes, offset + 2);
    }
}
