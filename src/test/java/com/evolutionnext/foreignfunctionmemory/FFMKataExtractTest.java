package com.evolutionnext.foreignfunctionmemory;

import com.evolutionnext.foreignfunctionmemory.generated.Point;
import com.evolutionnext.foreignfunctionmemory.generated.apply_callback$callback;
import com.evolutionnext.foreignfunctionmemory.generated.ffm_kata_h;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FFMKataExtractTest {

    //Or -Djava.library.path=/Users/danno/Development/mine/java_enable_foreign_function_memory_api/projects/ffm_kata
    static {
        String libraryPath = "/Users/danno/Development/mine/java_enable_foreign_function_memory_api/projects/ffm_kata/libffm_kata.dylib"; // Absolute path to the library
        System.load(libraryPath); // Explicitly load the native library
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
        try (Arena arena = Arena.ofConfined()) {
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
