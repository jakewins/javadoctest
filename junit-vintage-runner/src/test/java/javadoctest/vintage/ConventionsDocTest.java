package javadoctest.vintage;

import javadoctest.DocSnippet;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;


@RunWith( DocTestRunner.class )
public class ConventionsDocTest
{
    /**
     * <pre class="doctest:ConventionsDocTest#testPackageDefaultsToSameAsSourcePackage">
     * String aString = "Hello, world!";
     * </pre>
     */
    public void testPackageDefaultsToSameAsSourcePackage( DocSnippet snippet )
    {
        // When
        snippet.run();

        // Then
        assertThat( snippet.get( "aString" ), equalTo((Object)"Hello, world!"));
    }

    /**
     * <pre class="doctest:ConventionsDocTest#sourceImportsAutomaticallyImported">
     * Collection list = new LinkedList();
     * </pre>
     */
    public void sourceImportsAutomaticallyImported( DocSnippet snippet )
    {
        // Given 'Collection' and 'LinkedList' are used in the source class
        Collection coll;
        LinkedList list;

        // When I run without explicitly specifying these imports
        snippet.run();

        // Then the imports should get picked up automatically
        assertThat( snippet.get( "list" ), instanceOf( LinkedList.class ));
    }
}
