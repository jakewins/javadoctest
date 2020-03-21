package javadoctest.engine;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

final class JavadocTestEngineDescriptor extends EngineDescriptor implements
    Node<JavadocTestEngineExecutionContext> {

  JavadocTestEngineDescriptor(UniqueId uniqueId) {
    super(uniqueId, "Javadoc Test Engine");
  }
}
