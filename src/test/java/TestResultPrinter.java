import java.util.ArrayList;

/**
 * Utility class for handling and formatting test results.
 */
public class TestResultPrinter {

    private static ArrayList<String> testResultSTRS = new ArrayList<>();

    /**
     * Adds a formatted test result to the list.
     *
     * @param testName Name of the test.
     * @param expected The expected result.
     * @param actual   The actual result.
     * @param passed   Whether the test passed or failed.
     */
    public static void printTestResult(String testName, Object expected, Object actual, boolean passed) {
        String formattedResult = String.format(
                "| %-20s | %-10s | %-10s | %-8s |",
                testName,
                expected,
                actual,
                passed ? "✅ PASS" : "❌ FAIL"
        );

        testResultSTRS.add(formattedResult);
    }

    /**
     * Prints all stored test results.
     */
    public static void printResults() {
        if (!testResultSTRS.isEmpty()) {
            System.out.println("\nTest Results:");
            System.out.println("| Test Name            | Expected   | Actual     | Result   |");
            System.out.println("|----------------------|------------|------------|----------|");
            for (String s : testResultSTRS) {
                System.out.println(s);
            }
        } else {
            System.out.println("No test results to display.");
        }
    }

    /**
     * Clears the stored results. (Optional, useful for reusing)
     */
    public static void clearResults() {
        testResultSTRS.clear();
    }
}
