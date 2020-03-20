package javadoctest.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileWalker
{
    public static void walk( Path root, Predicate<Path> filter, Consumer<Path> consumer )
    {
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(filter)
                .forEach(consumer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
