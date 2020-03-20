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
package javadoctest.vintage;

import javadoctest.internal.DocTests;
import javadoctest.internal.ExtractedDocTest;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a junit test runner that tests examples in Javadoc. It works by reading all javadoc in source files
 * it finds below the current working directory, and correlating examples in those javadocs to test classes
 * that contain setup and assertion code.
 *
 * Javadoc examples are wrapped in 'pre' html tags and '@code' javadoc tags. The 'pre' tag gives monospace
 * formatting and the '@code' tag avoids having to escape special characters. The class attribute of the pre
 * tag should point to the class and method that contains setup and assertion code.
 *
 * <h2>For example:</h2>
 *
 * {@code
 * <pre class="__doctest">
 * myObject.call();
 * </pre>
 * }
 *
 * The test method does not need annotations, but since this runner is a regular JUnit4 runner, JUnit rules
 * and before/after methods will run as normal. The test method will get passed a {@link DocSnippet docs snippet},
 * which represents a runnable version of the example code. Before running the
 * snippet, you will want to assign any variables that the example assumes are available.
 */
public class DocTestRunner extends BlockJUnit4ClassRunner
{
    public DocTestRunner( Class<?> testClass ) throws InitializationError
    {
        super(testClass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods()
    {
        List<ExtractedDocTest> snippets = DocTests.testsFor( getTestClass().getJavaClass() );
        ArrayList<FrameworkMethod> methods = new ArrayList<>();
        for ( ExtractedDocTest docSnippet : snippets )
        {
            methods.add( new SnippetFrameworkMethod( docSnippet ) );
        }
        return methods;
    }

    @Override
    protected Statement methodInvoker( final FrameworkMethod method, final Object test )
    {
        final SnippetFrameworkMethod snippetMethod = (SnippetFrameworkMethod) method;
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                snippetMethod.invoke( test );
            }
        };
    }

    @Override
    protected void validateInstanceMethods(List<Throwable> errors) {
        // Overridden to remove a check ensuring there are @Test annotated methods in the class.
        validatePublicVoidNoArgMethods(After.class, false, errors);
        validatePublicVoidNoArgMethods(Before.class, false, errors);
        validateTestMethods(errors);
    }

    private class SnippetFrameworkMethod extends FrameworkMethod
    {
        private final ExtractedDocTest docSnippet;

        public SnippetFrameworkMethod( ExtractedDocTest docSnippet )
        {
            super(docSnippet.testMethod());
            this.docSnippet = docSnippet;
        }

        public void invoke( Object test ) throws Throwable
        {
            try
            {
                getMethod().invoke( test, docSnippet.compiled() );
            }
            catch ( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }

        public Description describe()
        {
            return Description.createTestDescription( "DocTest", docSnippet.source() );
        }
    }
}
