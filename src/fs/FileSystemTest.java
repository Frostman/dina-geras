package fs;

import org.junit.Assert;
import org.junit.Test;

public class FileSystemTest {

    @Test
    public void testStringMatchesPattern() {
        checkStringMatchesPattern("asd.txt", "a*", true);
        checkStringMatchesPattern("asd.txt", "*t", true);
        checkStringMatchesPattern("asd.txt", "a*x*", true);
        checkStringMatchesPattern("асд.тхт", "а*х*", true);
        checkStringMatchesPattern("asd.txt", "*a*x*", true);
        checkStringMatchesPattern("asd.txt", "*s*x*", true);
        checkStringMatchesPattern("asd.txt", "***s***x***", true);
        checkStringMatchesPattern("asd.txt", "***d***t***", true);
        checkStringMatchesPattern("asd.txt", "*a", false);
        checkStringMatchesPattern("asd.txt", "s****t", false);
        checkStringMatchesPattern("asd.txt", "*a*", true);
        checkStringMatchesPattern("asd.txt", "*t*t", true);
    }

    private void checkStringMatchesPattern(String str, String pattern, boolean expected) {
        Assert.assertEquals(str + " :: " + pattern, expected, fs.namespace.matchesPattern(str, pattern));
    }

    @Test
    public void testLongInRange() {
        //todo impl

    }
}
