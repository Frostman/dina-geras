package fs

import java.io.File
import java.util.regex.Pattern
import java.util.regex.Matcher
import java.util.List
import java.util.LinkedList
import java.io.FileInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import org.apache.commons.codec.digest.DigestUtils
import org.junit.Test
import org.apache.commons.io.FileUtils

trait Criterion {
    fun check(file : File) = false
}

class NameCriterion(val namePattern : String = "") : Criterion {
    override fun check(file : File) = file.getName().sure().matchesPattern(namePattern)
}

class SizeCriterion(val minSize : Long = 0, val maxSize : Long = Long.MAX_VALUE) : Criterion {
    override fun check(file : File) = file.length().inRange(minSize, maxSize)
}

class LastModifiedCriterion(val minDate : Long = 0, val maxDate : Long = Long.MAX_VALUE) : Criterion {
    override fun check(file : File) = file.lastModified().inRange(minDate, maxDate)
}

class ContentCriterion(val str : String = "") : Criterion {
    override fun check(file: File) = file.contains(str)
}

class FileInfo(val file : File) {
    val hash = file.shaHex()

    val path : String
    get() = file.getAbsolutePath().sure()

    val name : String
    get() = file.getName().sure()

    fun toString() : String {
        return path + "  ::  " + hash
    }
}

public fun searchFiles(rootPath : String, vararg criteria : Criterion) : List<FileInfo> {
    val result = LinkedList<FileInfo>()

    fileWalk(File(rootPath), result, criteria)
    return result
}

fun fileWalk(val file : File, val result : List<FileInfo>, val criteria : Array<Criterion>) {
    if (file.isDirectory()) {
        for (subFile in file.listFiles().sure()) {
            fileWalk(subFile.sure(), result, criteria)
        }
    } else if (file.isFile()){
        var good = true
        for (criterion in criteria) {
            good = good && criterion.check(file)
        }
        if (good) result.add(FileInfo(file))
    }
}

fun File.contains(val str : String) = this.getLines().contains(str)

fun File.getLines() = FileUtils.readFileToString(this).sure()

fun File.shaHex() = DigestUtils.shaHex(FileInputStream(this))

fun Long.inRange(val min : Long, val max : Long) = (min <= this && this <= max)

fun String.matchesPattern(var pattern : String) : Boolean {
    pattern = pattern.trim()
    if (pattern.length() == 0) return true

    val sb = StringBuilder()
    for (char in pattern) {
        sb.append(if (char == '*') "[.a-zA-Z0-9а-яА-Я_-]*" else char)
    }

    return Pattern.matches(sb.toString(), this)
}
