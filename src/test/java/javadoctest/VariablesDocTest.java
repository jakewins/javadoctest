package javadoctest;

import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith( DocTestRunner.class )
public class VariablesDocTest
{
    /**
     * <pre class="doctest:javadoctest.VariablesDocTest#testCanReadVar">
     * String aString = "Hello, world!";
     * </pre>
     */
    public void testCanReadVar( DocSnippet snippet )
    {
        // When
        snippet.run();

        // Then
        assertThat( snippet.get( "aString" ), equalTo((Object)"Hello, world!"));
    }

    /**
     * <pre class="doctest:javadoctest.VariablesDocTest#testVariableRoundTrip">
     * String aString = input;
     * </pre>
     */
    public void testVariableRoundTrip( DocSnippet snippet )
    {
        // When
        snippet.set( "input", "This is amazing!" ).run();

        // Then
        assertThat( snippet.get( "aString" ), equalTo((Object)"This is amazing!"));
    }
}
