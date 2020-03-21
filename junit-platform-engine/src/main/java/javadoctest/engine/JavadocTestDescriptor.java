package javadoctest.engine;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

abstract class JavadocTestDescriptor extends AbstractTestDescriptor implements
    Node<JavadocTestEngineExecutionContext> {

  protected JavadocTestDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
    super(uniqueId, displayName, source);
  }
}
