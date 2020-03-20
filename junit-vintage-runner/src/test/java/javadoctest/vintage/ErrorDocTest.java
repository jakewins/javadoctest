package javadoctest.vintage;

import javadoctest.DocSnippet;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith( DocTestRunner.class )
public class ErrorDocTest
{
    @Rule public ExpectedException exception = ExpectedException.none();

    /**
     * <pre class="doctest:ErrorDocTest#compileError">
     * a compile error
     * </pre>
     */
    public void compileError( DocSnippet snippet )
    {
        // Expect
        exception.expectMessage( "Compilation failed:" );
        exception.expectMessage( "javadoctest/vintage/ErrorDocTest$DocTest.java" );
        exception.expectMessage( "a compile error" );

        // When
        snippet.run();
    }
}
