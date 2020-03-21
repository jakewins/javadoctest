package javadoctest.engine.fixture;

/**
 * On a class level:
 *
 * <p><pre class="doctest">
 *   {@code
 *     int parsed = Integer.parseInt("1");
 *     assert parsed == 1;
 *   }
 * </pre>
 */
public class FixtureDocTestSimple {

  /**
   * On a field level:
   *
   * <p><pre class="doctest">
   *   {@code
   *     assert "field".length() == 5;
   *   }
   * </pre>
   */
  public static final String NAME = "Simple";
}
