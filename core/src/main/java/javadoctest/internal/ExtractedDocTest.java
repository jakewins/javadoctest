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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javadoctest.DocSnippet;

public class ExtractedDocTest
{
    private final String code;
    private final Collection<String> sourceClassImports;
    private final String sourcePackage;
    private final String sourceClass;
    /** The source <em>location</em> of this doc test. Not to be confused with source {@link #code}. */
    private final String source;
    private final DocSnippet compiled;

    private final static String template =
            "package %s;\n" +
            "\n" +
            "/* ---- Generated Imports ---- */\n" +
            "%s" +
            "/* ------- End Imports ------- */\n" +
            "\n" +
            "/** This is an auto-generated class for testing code extracted from Javadocs. */\n" +
            "public class %s implements Callable<Void>\n" +
            "{\n" +
            "\n" +
            "    public Void call() throws Exception\n" +
            "    {\n" +
            "        /* ---- Snippet ---- */\n" +
            "        /* Extracted from: %s */\n" +
            "        %s\n" +
            "        /* -- End snippet -- */\n" +
            "        return null;\n" +
            "    }\n" +
            "}\n";

    public ExtractedDocTest(Collection<String> sourceClassImports, String sourcePackage,
        String sourceClass, String source, String code)
    {
        this.sourceClassImports = sourceClassImports;
        this.sourcePackage = sourcePackage;
        this.sourceClass = sourceClass;
        this.source = source;
        this.code = code;
        this.compiled = compile();
    }

    @Override
    public String toString()
    {
        return "DocSnippet{" +
               "code='" + code + '\'' +
               '}';
    }

    public String code()
    {
        return code;
    }

    public String source()
    {
        return source;
    }

    /** @return name of package that the snippet gets stored in */
    public String targetPackage()
    {
        return sourcePackage;
    }

    /** @return name of class that the snippet gets stored in */
    public String targetClass()
    {
        return sourceClass + "$DocTest";
    }

    public DocSnippet compiled()
    {
        return compiled;
    }

    private DocSnippet compile()
    {
        return new ExtractedSnippet();
    }

    private class ExtractedSnippet implements DocSnippet
    {
        private final Set<String> imports = new HashSet<>();

        public ExtractedSnippet()
        {
            // Used by the template code
            addDefaultImports();
        }

        private void addDefaultImports()
        {
            addImport( Callable.class );
            addImport( Void.class );

            // TODO: This should not live here. We should take a list of Class<> and Method<> instances, not strings
            for ( String sourceClassImport : sourceClassImports )
            {
                try
                {
                    addImport( Class.forName( sourceClassImport ) );
                }
                catch(Throwable e)
                {
                    // Not a class we can import. It may be a static import, TODO to handle that
                }
            }

        }

        @Override
        public DocSnippet addImport( Class<?> cls )
        {
            imports.add( cls.getName() );
            return this;
        }

        @Override
        public void run() throws Exception
        {
            String importCode = createImportCode();
            String exampleClassSource = createClassCode(importCode);

            Callable<Map<String, Object>> example = new DynamicCompiler().newInstance(
                targetPackage() + "." + targetClass(),
                exampleClassSource);

            example.call();
        }

        private String createClassCode(String importCode)
        {
            return String.format( template,
                    targetPackage(),
                    importCode,
                    targetClass(),
                    source(),
                    code().replace( "\n", "\n        " ));
        }

        private String createImportCode()
        {
            StringBuilder sb = new StringBuilder();
            for ( String imp : imports )
            {
                sb.append( "import " ).append( imp ).append( ";\n" );
            }
            return sb.toString();
        }
    }
}
