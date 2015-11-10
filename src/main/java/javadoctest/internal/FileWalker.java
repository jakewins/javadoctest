package javadoctest.internal;

import javadoctest.internal.func.Consumer;
import javadoctest.internal.func.Predicate;

import java.io.File;
import java.nio.file.Path;

public class FileWalker
{
    public static void walk( Path root, Predicate<Path> filter, Consumer<Path> consumer )
    {
        File[] list = root.toFile().listFiles();

        if (list == null) return;

        for ( File f : list )
        {
            Path current = f.toPath();
            if ( f.isDirectory() )
            {
                walk( current, filter, consumer );
            }
            else
            {
                if( filter.test( current ))
                {
                    consumer.accept( current );
                }
            }
        }
    }
}
