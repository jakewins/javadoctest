package javadoctest.engine;

import static java.util.stream.Collectors.toSet;

import com.google.auto.service.AutoService;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javadoctest.internal.DocTestExtractor;
import javadoctest.internal.ExtractedDocTest;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector.Factory;

@AutoService(TestEngine.class)
public final class JavadocTestEngine extends
    HierarchicalTestEngine<JavadocTestEngineExecutionContext> {

  static final String JAVADOC_TEST_JUNIT_ENGINE_ID = "javadoc-test-engine";

  @Override
  protected JavadocTestEngineExecutionContext createExecutionContext(ExecutionRequest request) {
    return new JavadocTestEngineExecutionContext();
  }

  @Override
  public String getId() {
    return JAVADOC_TEST_JUNIT_ENGINE_ID;
  }

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
    Set<Path> selectedJavaSourceFiles = selectJavaSourceFiles(discoveryRequest);
    return extractDocTests(uniqueId, selectedJavaSourceFiles);
  }

  private Set<Path> selectJavaSourceFiles(EngineDiscoveryRequest discoveryRequest) {
    // Select individual files
    List<FileSelector> fileSelectors = discoveryRequest.getSelectorsByType(FileSelector.class);
    Stream<Path> selectedByFileSelector = fileSelectors.stream()
        .map(FileSelector::getPath);

    // Select files in the root directories
    List<DirectorySelector> directorySelectors =
        discoveryRequest.getSelectorsByType(DirectorySelector.class);
    try (Stream<Path> selectedByRootDir = directorySelectors.stream()
        .map(DirectorySelector::getPath)
        .flatMap(JavadocTestEngine::walkDir)
        .filter(isJavaSourceFile())) {

      // Concat and deduplicate the source files selected via different means
      return Stream.concat(selectedByFileSelector, selectedByRootDir)
          // Convert them to absolute path for effective deduplication
          .map(Path::toAbsolutePath)
          .collect(toSet());
    }
  }

  private JavadocTestEngineDescriptor extractDocTests(UniqueId uniqueId,
      Set<Path> javaSourceFiles) {
    // Extract the doc tests into the test descriptors from each selected Java source file
    JavadocTestEngineDescriptor rootDescriptor = new JavadocTestEngineDescriptor(uniqueId);
    DocTestExtractor docTestExtractor = new DocTestExtractor();
    for (Path javaSourcePath : javaSourceFiles) {
      // Create the child descriptor for this source file
      UniqueId sourceUid = uniqueId.append("source-file", javaSourcePath.toString());
      SourceTestDescriptor sourceTestDescriptor = new SourceTestDescriptor(sourceUid, javaSourcePath);

      // Extract the examples
      try {
        List<ExtractedDocTest> extractedDocTests = docTestExtractor.extractFrom(javaSourcePath);
        // Do not register the source file container if there are no doctests in that file
        if (extractedDocTests.isEmpty()) {
          continue;
        }
        // Create the test descriptor for each extracted doctest
        for (int i = 0; i < extractedDocTests.size(); i++) {
          ExtractedDocTest extractedDocTest = extractedDocTests.get(i);
          // Create a UID
          // todo: Improve the ExtractedDocTest to include its position in file:
          //  Class, optional Element (field, method, etc), index.
          //  Mind nested classes and multiple classes per file!
          UniqueId docTestUid = sourceUid.append("doc-test", Integer.toString(i));

          // Create a doc test descriptor
          // todo: Improve source to include, possibly, the line number of the doc test beginning
          FileSource docTestSource = FileSource.from(javaSourcePath.toFile());
          DocTestTestDescriptor docTestTestDescriptor = new DocTestTestDescriptor(docTestUid,
              extractedDocTest, docTestSource);

          // Register in the source file container
          sourceTestDescriptor.addChild(docTestTestDescriptor);
        }

        // Register in the root container
        rootDescriptor.addChild(sourceTestDescriptor);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
    return rootDescriptor;
  }

  private static @MustBeClosed Stream<Path> walkDir(Path selectedDir) {
    try {
      return Files.walk(selectedDir);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static Predicate<Path> isJavaSourceFile() {
    return path -> path.toString().endsWith(".java");
  }

  @Override
  protected Factory createThrowableCollectorFactory(ExecutionRequest request) {
    // We do not currently support aborting doc tests
    return () -> new ThrowableCollector(t -> false);
  }
}
