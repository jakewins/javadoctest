# JavaDoc-Test

[![Build Status](https://travis-ci.org/jakewins/javadoctest.svg?branch=master)](https://travis-ci.org/jakewins/javadoctest)

Test code examples in your java docs with JUnit!

This allows your examples to contain exactly the code you want to show - setup, assertions and tear-down is done in
a JUnit test. Run your tests from inside your IDE, use JUnit rules, parameters and setup mechanisms,
integrate seamlessly in your existing test infrastructure.

## Minimum Viable Snippet

Write a code example, wrapped in 'pre' tags, including where to find the test method:

    /**
     * Perform the action. For example:
     *
     * <pre class="doctest:mypackage.MyDocTest#testMyMethod">
     * Object myResult = myObject.myMethod("Hello, world!");
     * </pre>
     */
    public Object myMethod(String myArgument);


Add JavaDoc-Test as a dependency:

    <dependency>
        <groupId>com.jakewins</groupId>
        <artifactId>javadoc-test</artifactId>
        <version>1.0</version>
        <scope>test</scope>
    </dependency>


And then write a test for it:

    @RunWith( DocTestRunner.class )
    class MyDocTest
    {
        public void testMyMethod( DocSnippet codeSnippet )
        {
            // Given a variable 'myObject' is available to the script
            codeSnippet.set( "myObject", new MyObject() );

            // When I compile and run the example
            codeSnippet.run();

            // Then the result variable in the snippet should contain the things I expect
            assertNotNull( codeSnippet.get("myResult") );
        }
    }

For more details, refer to the ["DocTest" tests](https://github.com/jakewins/javadoctest/tree/master/src/test/java/javadoctest), which all use `javadoc-test` to test itself.

## Contributions

Contributions are super welcome - but do reach out first, so that we're not working on the same thing or building
in different directions!

## Release

    mvn clean versions:set deploy -P release  -DnewVersion=<version>
    git tag <version>
    git push

## License

The MIT License (MIT)

Copyright (c) <year> <copyright holders>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.