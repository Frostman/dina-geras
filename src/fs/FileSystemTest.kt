package fs

import org.junit.Assert
import org.junit.Test

public open class FileSystemTest() {

    [Test]
    public open fun testStringMatchesPattern() : Unit {
        checkStringMatchesPattern("asd.txt", "a*", true)
        checkStringMatchesPattern("asd.txt", "*t", true)
        checkStringMatchesPattern("asd.txt", "a*x*", true)
        checkStringMatchesPattern("асд.тхт", "а*х*", true)
        checkStringMatchesPattern("asd.txt", "*a*x*", true)
        checkStringMatchesPattern("asd.txt", "*s*x*", true)
        checkStringMatchesPattern("asd.txt", "***s***x***", true)
        checkStringMatchesPattern("asd.txt", "***d***t***", true)
        checkStringMatchesPattern("asd.txt", "*a", false)
        checkStringMatchesPattern("asd.txt", "s****t", false)
        checkStringMatchesPattern("asd.txt", "*a*", true)
        checkStringMatchesPattern("asd.txt", "*t*t", true)
    }

    private open fun checkStringMatchesPattern(str : String?, pattern : String?, expected : Boolean) : Unit {
        Assert.assertEquals(str + " :: " + pattern, expected, str!!.matchesPattern(pattern!!))
    }
}
