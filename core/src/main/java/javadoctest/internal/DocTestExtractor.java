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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Extracts doc tests from java source files.
 */
public class DocTestExtractor
{
    /** This is the class name in the "pre" tags that we look for to tell if something should be tested. */
    private final String docTestClass = System.getProperty( "doctest.className", "doctest" );

    public List<ExtractedDocTest> extractFrom( Path p ) throws IOException
    {
        return extractExamples( readFileToString( p, StandardCharsets.UTF_8 ) );
    }

    public static String readFileToString( Path path, Charset encoding ) throws IOException
    {
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, encoding);
    }

    public List<ExtractedDocTest> extractExamples( String source )
    {
        // Fast exit for classes without <pre in their docs. A small contribution to cutting down read time
        if(!source.contains( "<pre" ))
        {
            return Collections.emptyList();
        }

        ASTParser parser = ASTParser.newParser( AST.JLS2 );

        parser.setResolveBindings( false );
        parser.setStatementsRecovery( false );
        parser.setBindingsRecovery( false );
        parser.setSource( source.toCharArray() );
        parser.setIgnoreMethodBodies(false);

        ASTNode ast = parser.createAST( null );

        final AtomicReference<String> className = new AtomicReference<>();
        final AtomicReference<String> classPkg = new AtomicReference<>();

        final List<String> imports = new LinkedList<>();
        final List<ExtractedDocTest> examples = new ArrayList<>();
        ASTVisitor visitor = new ASTVisitor()
        {
            @Override
            public boolean visit( ImportDeclaration node )
            {
                // We collect imports as well,
                imports.add( node.getName().toString() );
                return true;
            }

            @Override
            public boolean visit( PackageDeclaration node )
            {
                classPkg.set( node.getName().toString() );
                return true;
            }

            @Override
            public boolean visit( FieldDeclaration node )
            {
                examples.addAll( extractExamples( imports, classPkg.get(), className.get(),
                        node.fragments().get( 0 ).toString(), node ) );
                return true;
            }

            @Override
            public boolean visit( MethodDeclaration node )
            {
                examples.addAll( extractExamples( imports, classPkg.get(), className.get(),
                        node.getName().toString(), node ) );
                return true;
            }

            @Override
            public boolean visit( TypeDeclaration node )
            {
                if( className.get() == null )
                {
                    className.set( node.getName().getIdentifier().toString() );
                }
                examples.addAll( extractExamples( imports, classPkg.get(), className.get(),
                        node.getName().toString() + " classdoc", node ) );
                return true;
            }
        };
        ast.accept( visitor );
        return examples;
    }

    private List<ExtractedDocTest> extractExamples(
            Collection<String> imports,
            String pkg,
            String rootClass,
            String source,
            BodyDeclaration node )
    {
        Javadoc javadoc = node.getJavadoc();
        if(javadoc == null) { return Collections.emptyList(); }

        return extractCodeBlocks( source, imports, pkg, rootClass, extractJavadoc( javadoc.getComment() ) );
    }

    private List<ExtractedDocTest> extractCodeBlocks(
            String source,
            Collection<String> imports,
            String pkg,
            String rootClass,
            String javadoc )
    {
        Document doc = Jsoup.parse( javadoc );
        List<ExtractedDocTest> blocks = new ArrayList<>();
        for ( Element element : doc.select( "pre[class]" ) )
        {
            String classAttribute = element.attr( "class" );
            if(!classAttribute.toLowerCase().startsWith( docTestClass ))
            {
                continue;
            }

            String text = element.text();
            if(text.trim().startsWith( "{@code" ))
            {
                text = text.trim();
                text = text.substring( "{@code".length(), text.length() - 1 );
            }

            blocks.add( new ExtractedDocTest( imports, pkg, rootClass, source, text ) );
        }
        return blocks;
    }

    private String extractJavadoc( String comment )
    {
        StringBuilder sb = new StringBuilder();
        for ( String s : comment.split( "\n" ) )
        {
            s = s.trim();

            if(s.equals( "/**" ) || s.equals( "*/" )) { continue; }

            s = s.startsWith( "/**" ) ? s.substring( 3 ) : s;
            s = s.endsWith( "*/" ) ? s.substring( 0, s.length() - 2 ) : s;
            s = s.startsWith( "* " ) ? s.substring( 2 ) : s;
            s = s.startsWith( "*" ) ? s.substring( 1 ) : s;
            sb.append( s ).append( "\n" );
        }

        return sb.toString();
    }
}
