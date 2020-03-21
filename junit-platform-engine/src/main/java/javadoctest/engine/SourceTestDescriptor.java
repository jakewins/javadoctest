package javadoctest.engine;

import java.nio.file.Path;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.FileSource;

final class SourceTestDescriptor extends JavadocTestDescriptor {

  SourceTestDescriptor(UniqueId sourceUid, Path javaSourcePath) {
    super(sourceUid, getDisplayName(javaSourcePath), testSource(javaSourcePath));
  }

  private static String getDisplayName(Path javaSourcePath) {
    return javaSourcePath.getFileName().toString();
  }

  private static FileSource testSource(Path javaSourcePath) {
    return FileSource.from(javaSourcePath.toFile());
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }
}
