package auth

import java.util.LinkedHashMap
import org.apache.commons.io.FileUtils
import java.io.File
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import crypt.encryptFile
import crypt.decryptFile
import java.util.Map
import java.util.List
import java.util.LinkedList
import sun.tools.jconsole.Plotter.Unit

class AuthDb(val dbPath : String) {
    // login -> user
    public val users : Map<String, User> = LinkedHashMap<String, User>()

    fun load() {
        users.clear()
        for (line in FileUtils.readLines(File(dbPath))) {
            val split = line.sure().split(" ").sure()
            val login = split[0].sure().fromB64()
            val password = split[1].sure().fromB64()
            val role = if (Integer.parseInt(split[2]) == 0) Role.ADMIN else Role.USER
            val lastModified = Long.parseLong(split[3])

            users.put(login, User(login, password, role, lastModified))
        }
    }

    fun save() = FileUtils.writeLines(File(dbPath), users.b64Values())
}

enum class Role {
    ADMIN
    USER
}

class User(val login : String, val password : String, val role : Role, val lastModified : Long) {
    val passwordStrange : String = "100%"

    //todo think about password hashing

    fun toString() = "${login} ${password} ${if (role == Role.ADMIN) 0 else 1} $lastModified"

    fun asB64String() = "${login.toB64()} ${password.toB64()} ${if (role == Role.ADMIN) 0 else 1} $lastModified"
}

fun Map<String, User>.b64Values() : List<String> {
    val result = LinkedList<String>()

    for (value in this.values()) {
        result.add(value.asB64String())
    }

    return result
}

fun String.toB64() = Base64.encodeBase64URLSafeString(this.getBytes()).sure()

fun String.fromB64() = StringUtils.newStringUtf8(Base64.decodeBase64(this.getBytes())).sure()

fun checkCredentials(val login : String, val password : String, val key : String = "test key", val dbPath : String = "database") : Boolean {
    decryptFile(key, File(dbPath))
    val db = AuthDb(dbPath)
    db.load()
    val result = db.users.get(login)?.password.equals(password)
    encryptFile(key, File(dbPath))

    if (db.users.size == 0) {
        return true
    }

    return result
}

fun main(args : Array<String>) {
    val db = AuthDb("database")
    db.users.put("admin", User("admin", "admin", Role.ADMIN, 0))
    db.save()
    encryptFile("test key", File("database"))
}
