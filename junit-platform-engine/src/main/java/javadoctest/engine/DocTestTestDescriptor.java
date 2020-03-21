package javadoctest.engine;

import javadoctest.DocSnippet;
import javadoctest.internal.ExtractedDocTest;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

final class DocTestTestDescriptor extends JavadocTestDescriptor {

  private final ExtractedDocTest extractedDocTest;

  protected DocTestTestDescriptor(UniqueId uniqueId, ExtractedDocTest extractedDocTest,
      TestSource source) {
    super(uniqueId, getDisplayName(extractedDocTest), source);
    this.extractedDocTest = extractedDocTest;
  }

  private static String getDisplayName(ExtractedDocTest extractedDocTest) {
    return extractedDocTest.source();
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  @Override
  public JavadocTestEngineExecutionContext execute(JavadocTestEngineExecutionContext context,
      DynamicTestExecutor dynamicTestExecutor) {
    ThrowableCollector throwableCollector = new ThrowableCollector(t -> false);

    throwableCollector.execute(() -> {
      // TODO: It does not actually compile the snippet — everything happens inside run :-)
      // Compile the doc test
      DocSnippet compiled = extractedDocTest.compiled();

      // Run the code snippet
      compiled.run();
    });

    // Check there were no exceptions
    throwableCollector.assertEmpty();
    return context;
  }
}
