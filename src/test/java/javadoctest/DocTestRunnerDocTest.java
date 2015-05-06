package javadoctest;

import org.junit.runner.RunWith;

import java.util.concurrent.Callable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith( DocTestRunner.class )
public class DocTestRunnerDocTest
{
    public void classDoc( DocSnippet snippet ) throws Exception
    {
        // Given
        Callable<?> myObject = mock(Callable.class);
        snippet.set( "myObject", myObject, Callable.class );

        // When
        snippet.run();

        // Then
        verify( myObject ).call();
    }
}
