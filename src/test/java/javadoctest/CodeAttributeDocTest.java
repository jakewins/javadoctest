package javadoctest;


import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith( DocTestRunner.class )
public class CodeAttributeDocTest
{
    /**
     * <pre class="doctest:javadoctest.CodeAttributeDocTest#testCodeInsideCodeAttributeWorks">
     * {@code
     * String aString = "Hello, world!";
     * }
     * </pre>
     */
    public void testCodeInsideCodeAttributeWorks( DocSnippet snippet )
    {
        // When
        snippet.run();

        // Then
        assertThat( snippet.get( "aString" ), equalTo((Object)"Hello, world!"));
    }
}
