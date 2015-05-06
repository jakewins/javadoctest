/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javadoctest.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import static java.util.Arrays.asList;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

/**
 * Wrapper around javac that provides an easy means of getting a live instance from a string of java code.
 */
public class DynamicCompiler
{
    /**
     * @param className has to match the package and class name, for instance 'mypackage.MyClass'
     * @param source the full class source code
     * @param <T> an interface the class implements
     * @throws Exception if the compilation fails
     * @return an instance of the compiled class
     */
    public <T> T newInstance( String className, String source )
            throws Exception
    {
        // Save source in .java file.
        File workDir = newWorkDir();

        compile( className, source, workDir );

        // Load and instantiate compiled class.
        Class<?> cls = Class.forName(className, true, URLClassLoader.newInstance( new URL[]{workDir.toURI().toURL()} ) );
        Object instance = cls.newInstance();

        deleteRecursively( workDir );

        return (T)instance;
    }

    private void compile( String className, String source, File workDir ) throws IOException
    {
        File sourceFile = new File(workDir, className.replace( ".", File.separator ) + ".java" );

        sourceFile.getParentFile().mkdirs();
        new FileWriter(sourceFile).append(source).close();

        // Compile source file.
        try
        {
            JavaCompiler compiler = getSystemJavaCompiler();

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fm = compiler.getStandardFileManager( diagnostics, null, null );

            compiler.getTask( null, fm, diagnostics, null, null, fm.getJavaFileObjects( sourceFile ) ).call();

            ensureNoErrors( diagnostics, source  );
        }
        finally
        {
            sourceFile.delete();
        }
    }

    private void ensureNoErrors( DiagnosticCollector<JavaFileObject> diagnostics, String source )
    {
        StringBuilder compileErrors = new StringBuilder();
        for ( Diagnostic<?> diagnostic : diagnostics.getDiagnostics() )
        {
            switch( diagnostic.getKind() )
            {
            case ERROR:
                compileErrors.append( diagnostic ).append( "\n" );
                break;
            default:
                System.err.println( "Warn: " + diagnostic );
            }
        }

        if( compileErrors.length() > 0 )
        {
            throw new RuntimeException( "Compilation failed: " + compileErrors.toString() + "\n" +
                                        "\n" +
                                        "Full source code:\n" +
                                        source );
        }
    }

    private File newWorkDir() throws IOException
    {
        File workDir = File.createTempFile( "neo4j", "compiler" );
        workDir.delete();
        return workDir;
    }

    private static void deleteRecursively( File file )
    {
        if( file.isDirectory() )
        {
            File[] files = file.listFiles();
            if(files != null)
            {
                for ( File sub : files )
                {
                    deleteRecursively( sub );
                }
            }
        }

        if(!file.delete())
        {
            System.err.println( "WARN: Failed to delete '" + file.getAbsolutePath() + "'." );
        }
    }
}
