package com.evolutionnext.foreignfunctionmemory;

import org.junit.jupiter.api.Test;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Consumer;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.*;

public class FFMKataTest {

    private static final Linker linker = Linker.nativeLinker();
    private static final Path libPath = resolveLibraryPath();

    private static SymbolLookup lookup(Arena arena) {
        return SymbolLookup.libraryLookup(libPath, arena);
    }

    private static Path resolveLibraryPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String libName = switch (os) {
            case String s when s.contains("mac") -> "libffm_kata.dylib";
            case String s when s.contains("win") -> "ffm_kata.dll";
            default -> "libffm_kata.so";
        };
        return Path.of("projects/ffm_kata", libName).toAbsolutePath().normalize();
    }

    @Test
    void testAddInts() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MethodHandle addInts = linker.downcallHandle(
                lookup(arena).find("add_ints").orElseThrow(),
                FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT)
            );
            int result = (int) addInts.invoke(3, 4);
            assertEquals(7, result);
        }
    }

    @Test
    void testMultiplyDouble() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MethodHandle mult = linker.downcallHandle(
                lookup(arena).find("multiply_double").orElseThrow(),
                FunctionDescriptor.of(JAVA_DOUBLE, JAVA_DOUBLE, JAVA_DOUBLE)
            );
            double result = (double) mult.invoke(2.5, 4.0);
            assertEquals(10.0, result, 0.0001);
        }
    }

    @Test
    void testStrlenOnLibC() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nativeStringSegment = arena.allocateFrom("Hello Native World!");

            SymbolLookup stdLib = linker.defaultLookup();
            MemorySegment strlenAddr = stdLib.find("strlen").orElseThrow();

            // Most modern systems define size_t as 64-bit (long) on 64-bit platforms
            FunctionDescriptor strlenSig = FunctionDescriptor.of(JAVA_LONG, ADDRESS);
            MethodHandle strlen = linker.downcallHandle(strlenAddr, strlenSig);

            long result = (long) strlen.invoke(nativeStringSegment);
            assertEquals(19L, result);
        }
    }

    @Test
    void testDistanceSquared() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemoryLayout pointLayout =
                MemoryLayout.structLayout(JAVA_INT.withName("x"), JAVA_INT.withName("y"));
            MemorySegment point = arena.allocate(pointLayout);
            point.setAtIndex(JAVA_INT, 0, 3);
            point.setAtIndex(JAVA_INT, 1, 4);

            MethodHandle distance = linker.downcallHandle(
                lookup(arena).find("distance_squared").orElseThrow(),
                FunctionDescriptor.of(JAVA_INT, ADDRESS)
            );
            int result = (int) distance.invoke(point);
            assertEquals(25, result);
        }
    }

    @Test
    void testCopyString() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment src = arena.allocateFrom("Hello from Java!");
            MemorySegment dest = arena.allocate(100);

            MethodHandle copier = linker.downcallHandle(
                lookup(arena).find("copy_string").orElseThrow(),
                FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, JAVA_INT)
            );

            copier.invoke(src, dest, 100);
            assertEquals("Hello from Java!", dest.getString(0));
        }
    }

    @Test
    void testReturnPointerFromC() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MethodHandle greet = linker.downcallHandle(
                lookup(arena).find("greet").orElseThrow(),
                FunctionDescriptor.of(ADDRESS)
            );

            MemorySegment ptr = (MemorySegment) greet.invoke();

            System.out.println(
                "Size, in bytes, of memory segment created by calling malloc.invoke(): "
                + ptr.byteSize());

            MemorySegment readable = ptr.reinterpret(100);
            System.out.println(
                "Size, in bytes, of memory segment created by calling ptr.reinterpret: " +
                readable.byteSize());

            String result = readable.getString(0, StandardCharsets.UTF_8);
            assertEquals("Hello from C!", result);
        }
    }

    @Test
    void testApplyCallback() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            FunctionDescriptor cbDesc = FunctionDescriptor.of(JAVA_INT, JAVA_INT);
            MethodHandle callback = MethodHandles.lookup().findStatic(
                FFMKataTest.class,
                "myCallback",
                java.lang.invoke.MethodType.methodType(int.class, int.class)
            );

            MemorySegment cbStub = linker.upcallStub(callback, cbDesc, arena);
            MethodHandle applyCallback = linker.downcallHandle(
                lookup(arena).find("apply_callback").orElseThrow(),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT)
            );

            int result = (int) applyCallback.invoke(cbStub, 7);
            assertEquals(70, result);
        }
    }

    public static int myCallback(int x) {
        return x * 10;
    }
}
