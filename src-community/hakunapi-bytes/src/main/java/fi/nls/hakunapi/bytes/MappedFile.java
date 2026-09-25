package fi.nls.hakunapi.bytes;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A local file mapped whole.
 */
public class MappedFile extends SegmentSourceBase {

    public MappedFile(Path path) throws IOException {
        this(path, Arena.ofShared());
    }

    private MappedFile(Path path, Arena arena) throws IOException {
        super(arena, map(path, arena));
    }

    private static MemorySegment map(Path path, Arena arena) throws IOException {
        // The mapping does not depend on the channel staying open
        try (FileChannel ch = FileChannel.open(path, StandardOpenOption.READ)) {
            return ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size(), arena);
        } catch (IOException | RuntimeException e) {
            arena.close();
            throw e;
        }
    }

}
