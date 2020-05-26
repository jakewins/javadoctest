package javadoctest.engine.fixture;

import java.io.IOException;
import org.opentest4j.TestAbortedException;

public class FixtureExceptions {

  /**
   * Doc tests throwing exceptions. Currently hidden under {@code if (true) ...}
   * so that we don't get "unreachable statement" compilation exception.
   *
   * <h3>1 Runtime exception</h3>
   *
   * <p><pre class="doctest">
   *   {@code
   *     if (true) {
   *       throw new RuntimeException("doctest 1");
   *     }
   *   }
   * </pre>
   *
   * <h3>2 Checked exception</h3>
   *
   * <p><pre class="doctest">
   *   {@code
   *     if (true) {
   *       throw new IOException("doctest 2");
   *     }
   *   }
   * </pre>
   *
   * <h3>3 AssertionError test</h3>
   *
   * <p><pre class="doctest">
   *   {@code
   *     if (true) {
   *       throw new AssertionError("doctest 3");
   *     }
   *   }
   * </pre>
   *
   * <h3>4 TestAbortedException test</h3>
   *
   * <p><pre class="doctest">
   *   {@code
   *     if (true) {
   *       // Must not abort the doc test — must fail one!
   *       throw new TestAbortedException("doctest 4");
   *     }
   *   }
   * </pre>
   */
  @SuppressWarnings("unused") // Import the classes used in doctests by declaring their variables.
  public void exceptions() {
    IOException e1;
    TestAbortedException e2;
  }
}
