package javadoctest.engine;

import static javadoctest.engine.JavadocTestEngine.JAVADOC_TEST_JUNIT_ENGINE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;
import org.opentest4j.TestAbortedException;

class JavadocTestEngineTest {

  private static final Path FIXTURE_ROOT_PATH = Paths.get("./src/test/java/javadoctest/engine/fixture");

  @BeforeAll
  static void checkFixtureRootExists() {
    assertThat(FIXTURE_ROOT_PATH).isDirectory();
  }

  @Test
  void noDocTests() {
    EngineTestKit
        .engine(JAVADOC_TEST_JUNIT_ENGINE_ID)
        .selectors(selectFile(FIXTURE_ROOT_PATH.resolve("FixtureNoTests.java").toFile()))
        .execute()
        .testEvents()
        .assertStatistics(stats -> stats.started(0));
  }

  @Test
  void simpleDocTest() {
    Events testEvents = EngineTestKit
        .engine(JAVADOC_TEST_JUNIT_ENGINE_ID)
        .selectors(selectFile(FIXTURE_ROOT_PATH.resolve("FixtureDocTestSimple.java").toFile()))
        .execute()
        .testEvents();

    // Two passing doc-tests in that file
    testEvents.assertStatistics(stats -> stats.started(2).succeeded(2));

    testEvents.assertThatEvents()
        .haveExactly(1, event(test("doc-test:0"/* "FixtureDocTestSimple" */),
            finishedSuccessfully()));

    testEvents.assertThatEvents()
        .haveExactly(1, event(test("doc-test:1"/* "#NAME" */),
            finishedSuccessfully()));
  }

  @Test
  void simpleDocTestSelectedMultipleTimesShallBeExecutedOnce() {
    EngineExecutionResults executionResults = EngineTestKit
        .engine(JAVADOC_TEST_JUNIT_ENGINE_ID)
        .selectors(selectFile(FIXTURE_ROOT_PATH.resolve("FixtureDocTestSimple.java").toFile()),
            selectFile(FIXTURE_ROOT_PATH.resolve("./FixtureDocTestSimple.java").toFile()))
        .execute();

    executionResults
        .containerEvents()
        // The root engine container + a single source file container
        .assertStatistics(stats -> stats.started(2).succeeded(2));

    executionResults
        .testEvents()
        // Two passing doc-tests in that file
        .assertStatistics(stats -> stats.started(2).succeeded(2));
  }

  @Test
  void exceptionsInDocTests() {
    Events testEvents = EngineTestKit
        .engine(JAVADOC_TEST_JUNIT_ENGINE_ID)
        .selectors(selectFile(FIXTURE_ROOT_PATH.resolve("FixtureExceptions.java").toFile()))
        .execute()
        .testEvents();

    // Check the basic stats
    testEvents.assertStatistics(stats -> stats.started(4).succeeded(0));

    // Check the source (id) of the tests
    testEvents.assertThatEvents()
        .haveExactly(4, event(test("doctest"), finishedWithFailure()));

    assertHasException(testEvents, RuntimeException.class, "doctest 1");
    assertHasException(testEvents, IOException.class, "doctest 2");
    assertHasException(testEvents, AssertionError.class, "doctest 3");
    assertHasException(testEvents, TestAbortedException.class, "doctest 4");
  }

  private void assertHasException(Events testEvents, Class<? extends Throwable> exceptionClass,
      String messageSubstring) {
    testEvents.assertThatEvents()
        .haveExactly(1, event(test(), finishedWithFailure(instanceOf(exceptionClass),
            message(messageSubstring))));
  }

  @Test
  void compilationFailureDocTest() {
    Events testEvents = EngineTestKit
        .engine(JAVADOC_TEST_JUNIT_ENGINE_ID)
        .selectors(selectFile(FIXTURE_ROOT_PATH.resolve("FixtureCompilationFailure.java").toFile()))
        .execute()
        .testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).succeeded(0));

    testEvents.assertThatEvents()
        .haveExactly(1, event(
            test("doc-test:0"/* "FixtureCompilationFailure" */),
            finishedWithFailure(message(m -> m.contains("Compilation failed")),
                message(m -> m.contains("Not a statement you'd expect in Java source.")))));
  }

  @Test
  void allDocTestsSelectedByDirectory() {
    // A single root container + 3 non-empty source containers
    int expectedContainers = 1 + 3;
    EngineTestKit
        .engine(JAVADOC_TEST_JUNIT_ENGINE_ID)
        .selectors(selectDirectory(FIXTURE_ROOT_PATH.toFile()))
        .execute()
        .containerEvents()
        .assertStatistics(stats -> stats.started(expectedContainers));
  }
}
