package com.evolutionnext.foreignfunctionmemory;

import org.junit.jupiter.api.Test;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FFMRustTest {

    private static final Linker linker = Linker.nativeLinker();

    private static Path resolveRustLibPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String libName = switch (os) {
            case String s when s.contains("mac") -> "librust_math.dylib";
            case String s when s.contains("win") -> "rust_math.dll";
            default -> "librust_math.so";
        };
        return Path.of("projects/rust_math/target/release", libName)
            .toAbsolutePath()
            .normalize();
    }

    private static SymbolLookup rustLookup(Arena arena) {
        return SymbolLookup.libraryLookup(resolveRustLibPath(), arena);
    }

    @Test
    void testSquareFunctionInRust() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MethodHandle squareFn = linker.downcallHandle(
                rustLookup(arena).find("square").orElseThrow(),
                FunctionDescriptor.of(JAVA_INT, JAVA_INT)
            );

            int result = (int) squareFn.invoke(9);
            assertEquals(81, result);
        }
    }


}
