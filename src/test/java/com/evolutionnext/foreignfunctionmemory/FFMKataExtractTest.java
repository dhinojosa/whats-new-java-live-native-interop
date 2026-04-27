package com.evolutionnext.foreignfunctionmemory;

import com.evolutionnext.foreignfunctionmemory.generated.Point;
import com.evolutionnext.foreignfunctionmemory.generated.apply_callback$callback;
import com.evolutionnext.foreignfunctionmemory.generated.ffm_kata_h;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FFMKataExtractTest {

    private static Path resolveFfmKataLibPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String libName = switch (os) {
            case String s when s.contains("mac") -> "libffm_kata.dylib";
            case String s when s.contains("win") -> "ffm_kata.dll";
            default -> "libffm_kata.so";
        };

        return Path.of("projects/ffm_kata", libName)
                .toAbsolutePath()
                .normalize();
    }

    static {
        System.out.println("Loading library from: " + resolveFfmKataLibPath());
        System.load(resolveFfmKataLibPath().toString());
    }


    @Test
    void testAddIntsExtracted() throws Throwable {
        int result = ffm_kata_h.add_ints(7, 5);
        assertEquals(12, result);
    }

    @Test
    void testMultiplyDoubleExtracted() throws Throwable {
        double result = ffm_kata_h.multiply_double(2.5, 4.0);
        assertEquals(10.0, result);
    }

    @Test
    void testDistanceSquaredExtracted() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment point = Point.allocate(arena);
            Point.x(point, 3);
            Point.y(point, 4);

            int result = ffm_kata_h.distance_squared(point);
            assertEquals(25, result);
        }
    }

    @Test
    void testGreetExtracted() throws Throwable {
        try (Arena _ = Arena.ofConfined()) {
            MemorySegment ptr = ffm_kata_h.greet.makeInvoker().apply();
            MemorySegment wrapped = ptr.reinterpret(100);
            String result = wrapped.getString(0);
            assertEquals("Hello from C!", result);
        }
    }

    @Test
    void testApplyCallbackExtracted() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            apply_callback$callback.Function doubleFn = (int x) -> x * 2;
            MemorySegment callbackPtr = apply_callback$callback.allocate(doubleFn, arena);
            int result = ffm_kata_h.apply_callback(callbackPtr, 5);
            assertEquals(10, result);
        }
    }

    @Test
    void testCopyStringExtracted() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment src = arena.allocateFrom("hello");
            MemorySegment dest = arena.allocate(10);

            ffm_kata_h.copy_string(src, dest, 6); // include null terminator

            String copied = dest.getString(0);
            assertEquals("hello", copied);
        }
    }
}
