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
}
