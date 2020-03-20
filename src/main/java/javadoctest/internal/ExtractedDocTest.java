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

import javadoctest.DocSnippet;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class ExtractedDocTest
{
    private final String code;
    private final Collection<String> sourceClassImports;
    private final String sourcePackage;
    private final String sourceClass;
    private final String source;
    private final Class<?> testClass;
    private final Method testMethod;
    private final DocSnippet compiled;

    private final static String template =
            "package %s;\n" +
            "\n" +
            "/* ---- Generated Imports ---- */\n" +
            "%s" +
            "/* ------- End Imports ------- */\n" +
            "\n" +
            "/** This is an auto-generated class for testing code extracted from javadocs. */\n" +
            "public class %s implements Callable<Map<String, Object>>\n" +
            "{\n" +
            "    /* ---- Context variables ---- */\n" +
            "%s" +
            "    /* -- End context variables -- */\n" +
            "\n" +
            "    public Map<String, Object> call() throws Exception\n" +
            "    {\n" +
            "        /* ---- Snippet ---- */\n" +
            "        /* Extracted from: %s */\n" +
            "        %s\n" +
            "        /* -- End snippet -- */\n" +
            "\n" +
            "        Map<String, Object> $out = new HashMap<>();\n" +
            "%s" +
            "        return $out;\n" +
            "    }\n" +
            "}\n";

    public ExtractedDocTest( Collection<String> sourceClassImports, String sourcePackage, String sourceClass, String source,
            Class<?> testClass,
            Method testMethod,
            String code )
    {
        this.sourceClassImports = sourceClassImports;
        this.sourcePackage = sourcePackage;
        this.sourceClass = sourceClass;
        this.source = source;
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.code = code;
        this.compiled = compile();
    }

    public Class<?> testClass()
    {
        return testClass;
    }

    public Method testMethod()
    {
        return testMethod;
    }

    @Override
    public String toString()
    {
        return "DocSnippet{" +
               "code='" + code + '\'' +
               ", testClass=" + testClass +
               ", testMethod=" + testMethod +
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

    private static class Variable
    {
        private final Class<?> cls;
        private final Object value;

        private Variable( Class<?> cls, Object value )
        {
            this.cls = cls;
            this.value = value;
        }

        public String shortClassName()
        {
            return cls.getSimpleName();
        }
    }

    private DocSnippet compile()
    {
        return new ExtractedSnippet();
    }

    private class ExtractedSnippet implements DocSnippet
    {
        private final Map<String, Variable> variables = new HashMap<>();
        private final Set<String> imports = new HashSet<>();
        private Map<String,Object> result;

        public ExtractedSnippet()
        {
            // Used by the template code
            addDefaultImports();
        }

        private void addDefaultImports()
        {
            addImport( Callable.class );
            addImport( Map.class );
            addImport( HashMap.class );

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
        public void run()
        {
            try
            {
                String variablesCode = createContextFieldDefinitions();
                String importCode = createImportCode();
                String exportCode = createExportCode(importCode, variablesCode);

                String exampleClassSource = createClassCode( variablesCode, importCode, exportCode );

                Callable<Map<String, Object>> example = new DynamicCompiler().newInstance(
                        targetPackage() + "." + targetClass(),
                        exampleClassSource );

                assignVariables( example );

                result = example.call();
            }
            catch ( Exception e )
            {
                if(e instanceof RuntimeException)
                {
                    throw (RuntimeException)e;
                }
                throw new RuntimeException( e );
            }
        }

        @Override
        public <T> T get( String variableName )
        {
            return (T) result.get( variableName );
        }

        private String createClassCode( String variablesCode, String importCode, String exportCode)
        {
            return String.format( template,
                    targetPackage(),
                    importCode,
                    targetClass(),
                    variablesCode,
                    source(),
                    code().replace( "\n", "\n        " ),
                    exportCode );
        }

        private String createExportCode( String importCode, String variablesCode )
        {
            ASTParser parser = ASTParser.newParser( AST.JLS2 );

            parser.setResolveBindings(false);
            parser.setStatementsRecovery(false);
            parser.setBindingsRecovery(false);
            parser.setSource(createClassCode( variablesCode, importCode, "" ).toCharArray());
            parser.setIgnoreMethodBodies(false);

            ASTNode ast = parser.createAST( null );

            final StringBuilder sb = new StringBuilder();
            ASTVisitor visitor = new ASTVisitor()
            {
                @Override
                public boolean visit( VariableDeclarationFragment node )
                {
                    String varName = node.getName().getIdentifier();
                    if(!varName.startsWith( "$" ))
                    {
                        sb.append( "        $out.put(\"" )
                          .append( varName ).append( "\", " )
                          .append( varName ).append( ");\n" );
                    }
                    return true;
                }
            };
            ast.accept( visitor );
            return sb.toString();
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

        private void assignVariables( Object instance ) throws NoSuchFieldException, IllegalAccessException
        {
            for ( Map.Entry<String,Variable> ctxVar : variables.entrySet() )
            {
                String name = ctxVar.getKey();
                Variable var = ctxVar.getValue();
                instance.getClass().getField( name ).set( instance, var.value );
            }
        }

        /**
         * Each example may depend on a set of context variables, like 'session' and so on. This builds those variables
         * up as a set of public fields in the example class that we construct.
         */
        private String createContextFieldDefinitions()
        {
            StringBuilder sb = new StringBuilder();
            for ( Map.Entry<String,Variable> ctxVar : variables.entrySet() )
            {
                String name = ctxVar.getKey();
                Variable var = ctxVar.getValue();
                sb.append( String.format( "    public %s %s;\n", var.shortClassName(), name ) );
            }
            return sb.toString();
        }
    }
}
