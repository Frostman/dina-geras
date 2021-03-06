package ui

import auth.AuthDb
import auth.Role
import auth.User
import auth.checkCredentials
import crypt.decryptFile
import crypt.encryptFile
import fs.ContentCriterion
import fs.FileInfo
import fs.LastModifiedCriterion
import fs.NameCriterion
import fs.SizeCriterion
import fs.searchFiles
import java.awt.Color
import java.awt.Frame
import java.awt.Toolkit
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.util.ArrayList
import java.util.Date
import java.util.List
import java.util.Map
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPasswordField
import javax.swing.JTextField
import javax.swing.WindowConstants
import javax.swing.text.JTextComponent
import ui.et.Column
import ui.et.EditableTable
import ui.et.StringValue
import ui.et.Value

public val key : String = "test key"

fun main(args : Array<String>) {
    val dbPath = "database"

    if(!File(dbPath).exists()) {
        val db = AuthDb("database")
        db.users.put("admin", User("admin", "admin", Role.ADMIN, 0, "0%"))

        db.confs.put("max password life time", "525600")
        db.confs.put("min password life time", "1")
        db.confs.put("min password length", "3")
        db.confs.put("password meets requirments", "true")
        db.confs.put("password bruteforce speed", "60000000")
        db.confs.put("alphabet size", "64")

        db.save()
        encryptFile(key, File("database"))
    }

    val result = askForCredentials(null, {checkCredentials(it._1, it._2, key, dbPath)})

    println("auth: ${result._1}")

    if (result._1 == true) {
        showVariantsWindow(null, result._2)
    } else {
        System.exit(- 1)
    }
}

fun askForCredentials(val parent : Frame? = null, checker : (#(String, String))->Boolean) : #(Boolean, String, String) {
    val dialog = JDialog(parent, true)

    dialog.setTitle("Enter login and password")
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    dialog.setResizable(false)
    dialog.getContentPane()?.setLayout(null)
    val width = 320
    val height = 170
    dialog.setSize(width, height)
    val screenSize = Toolkit.getDefaultToolkit()?.getScreenSize()!!
    val x = (screenSize.width - width) / 2
    val y = (screenSize.height - height) / 2
    dialog.setLocation(x.toInt(), y.toInt())

    val loginField = JTextField()
    loginField.setBounds(60, 10, 200, 24)
    loginField.addFocusListener(PlaceHolder(loginField, "login"))
    dialog.getContentPane()?.add(loginField)

    val passwordField = JPasswordField()
    passwordField.setBounds(60, 44, 200, 24)
    passwordField.addFocusListener(PlaceHolder(passwordField, "password"))
    dialog.getContentPane()?.add(passwordField)

    val errorLabel = JLabel("")
    errorLabel.setBounds(60, 78, 200, 24)
    errorLabel.setHorizontalTextPosition(0)
    errorLabel.setVerticalTextPosition(0)
    errorLabel.setForeground(Color.RED)

    dialog.getContentPane()?.add(errorLabel)

    val okButton = JButton("Ok")
    okButton.setBounds(57, 112, 100, 24)
    dialog.getContentPane()?.add(okButton)

    val cancelButton = JButton("Cancel")
    cancelButton.setBounds(163, 112, 100, 24)
    dialog.getContentPane()?.add(cancelButton)

    //    dialog.addWindowListener(object : WindowAdapter() {
    //        override fun windowGainedFocus(e : WindowEvent?) {
    //// todo           okButton.requestFocusInWindow()
    //        }
    //    })

    var auth = false

    val clickHandler = object : MouseAdapter() {
        public override fun mouseClicked(e : MouseEvent?) {
            when (e?.getSource()) {
                okButton ->
                    if(!checker(#(loginField.getText()!!, passwordField.getText()!!))) {
                        errorLabel.setText("Incorrect login-password pair")
                    } else {
                        auth = true
                        dialog.hide()
                    }
                cancelButton -> dialog.hide()
                else -> println("unknown click source")
            }
        }
    }

    okButton.addMouseListener(clickHandler)
    cancelButton.addMouseListener(clickHandler)

    dialog.setVisible(true)

    return #(auth, loginField.getText()!!, passwordField.getText()!!)
}

class PlaceHolder(val component : JTextComponent, val placeholder : String) : FocusListener {
    var componentColor : Color? = null
    var placeholderMode = false

    {
        focusLost(null)
    }

    public override fun focusGained(e : FocusEvent? = null) {
        if (placeholderMode) {
            component.setForeground(componentColor)
            component.setText("")
            placeholderMode = false
        }
    }

    public override fun focusLost(e : FocusEvent?) {
        componentColor = component.getForeground()
        if (component.getText()?.trim()?.length == 0) {
            component.setForeground(Color.GRAY)
            component.setText(placeholder)
            placeholderMode = true
        }
    }
}

fun showVariantsWindow(val parent : Frame? = null, val username : String) {
    val user = auth.getUser(username)!!

    val dialog = JDialog(parent, true)

    dialog.setTitle("What do you want?")
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    dialog.setResizable(false)
    dialog.getContentPane()?.setLayout(null)

    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

    val width = 320
    val height = 190
    dialog.setSize(width, height)
    val screenSize = Toolkit.getDefaultToolkit()?.getScreenSize()!!
    val x = (screenSize.width - width) / 2
    val y = (screenSize.height - height) / 2
    dialog.setLocation(x.toInt(), y.toInt())

    val loginField = JTextField("login: " + username)
    loginField.setBounds(60, 10, 200, 24)
    loginField.setEditable(false)
    dialog.getContentPane()?.add(loginField)

    val roleField = JTextField("role: " + if (user.role == Role.ADMIN) "admin" else "user")
    roleField.setBounds(60, 44, 200, 24)
    roleField.setEditable(false)
    dialog.getContentPane()?.add(roleField)

    val usersButton = JButton("Users")
    usersButton.setBounds(60, 78, 80, 25)
    if(user.role != Role.ADMIN) {
        usersButton.setEnabled(false)
    }
    dialog.getContentPane()?.add(usersButton)

    val confButton = JButton("Config")
    confButton.setBounds(60, 108, 80, 25)
    if(user.role != Role.ADMIN) {
        confButton.setEnabled(false)
    }
    dialog.getContentPane()?.add(confButton)

    val exitButton = JButton("Exit")
    exitButton.setBounds(60, 138, 80, 25)
    dialog.getContentPane()?.add(exitButton)

    val encryptButton = JButton("Encrypt")
    encryptButton.setBounds(180, 78, 80, 25)
    dialog.getContentPane()?.add(encryptButton)

    val decryptButton = JButton("Decrypt")
    decryptButton.setBounds(180, 108, 80, 25)
    dialog.getContentPane()?.add(decryptButton)

    val searchButton = JButton("Search")
    searchButton.setBounds(180, 138, 80, 25)
    dialog.getContentPane()?.add(searchButton)

    val clickHandler = object : MouseAdapter() {
        public override fun mouseClicked(e : MouseEvent?) {
            when (e?.getSource()) {
                usersButton -> {
                    if(user.role == Role.ADMIN) {
                        showUsersListWindow(username)
                        dialog.dispose()
                    }
                }

                confButton -> {
                    if(user.role == Role.ADMIN) {
                        showConfWindow(username)
                        dialog.dispose()
                    }
                }

                exitButton -> {
                    System.exit(- 1)
                }

                encryptButton -> {
                    val fc = JFileChooser()
                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY)
                    fc.setMultiSelectionEnabled(false)
                    if(JFileChooser.APPROVE_OPTION == fc.showOpenDialog(dialog)){
                        val file = fc.getSelectedFile()!!
                        encryptFile(user.login + "#" + user.password, file, File(file.getAbsolutePath() + ".enc"))
                    }
                }

                decryptButton -> {
                    val fc = JFileChooser()
                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY)
                    fc.setMultiSelectionEnabled(false)
                    if(JFileChooser.APPROVE_OPTION == fc.showOpenDialog(dialog)){
                        val file = fc.getSelectedFile()!!
                        decryptFile(user.login + "#" + user.password, file, false, File(file.getAbsolutePath() + ".dec"))
                    }
                }

                searchButton -> {
                    showSearchWindow(user.login + "#" + user.password, user.login)
                    dialog.dispose()
                }

                else -> println("unknown click source")
            }
        }
    }

    usersButton.addMouseListener(clickHandler)
    confButton.addMouseListener(clickHandler)
    exitButton.addMouseListener(clickHandler)
    encryptButton.addMouseListener(clickHandler)
    decryptButton.addMouseListener(clickHandler)
    searchButton.addMouseListener(clickHandler)

    dialog.setVisible(true)
}

fun showUsersListWindow(val username : String) {
    val frame = JFrame("Users list")
    frame.setLayout(null)
    frame.setBounds(100, 100, 600, 650)
    frame.setResizable(false)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

    val table = EditableTable(User.columns)
    frame.add(table.getScrollPane(10, 10, 580, 560))

    val db = AuthDb("database")
    db.load(true)
    table.data = db.users.values().map{it.asColumns()}

    val saveButton = JButton("Save")
    saveButton.setBounds(10, 590, 100, 30)
    frame.add(saveButton)

    val closeButton = JButton("Menu")
    closeButton.setBounds(150, 590, 100, 30)
    frame.add(closeButton)

    val addButton = JButton("Add")
    addButton.setBounds(350, 590, 100, 30)
    frame.add(addButton)

    val remButton = JButton("Remove")
    remButton.setBounds(490, 590, 100, 30)
    frame.add(remButton)

    val clickHandler = object : MouseAdapter() {
        public override fun mouseClicked(e : MouseEvent?) {
            when (e?.getSource()) {
                saveButton -> {
                    db.users.clear()
                    for (user in table.getObjects{User.fromColumns(this)}) {
                        db.users.put(user.login, user)
                    }
                    db.save(true)
                }

                closeButton -> {
                    frame.setVisible(false)
                    frame.dispose()
                    showVariantsWindow(null, username)
                }

                addButton -> {
                    table.data.add(User("NAME", "PASSWORD", Role.USER, System.currentTimeMillis(), "0%").asColumns())
                    table.data = table.data
                }

                remButton -> {
                    table.removeSelected()
                }

                else -> println("unknown click source")
            }
        }
    }

    saveButton.addMouseListener(clickHandler)
    closeButton.addMouseListener(clickHandler)
    addButton.addMouseListener(clickHandler)
    remButton.addMouseListener(clickHandler)

    frame.setVisible(true)
}

fun showConfWindow(val username : String) {
    val frame = JFrame("Configuration")
    frame.setLayout(null)
    frame.setBounds(100, 100, 600, 650)
    frame.setResizable(false)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

    val table = EditableTable(confColumns)
    frame.add(table.getScrollPane(10, 10, 580, 560))

    val db = AuthDb("database")
    db.load(true)
    table.data = db.confs.entrySet().map{it.asColumns()}

    val saveButton = JButton("Save")
    saveButton.setBounds(10, 590, 100, 30)
    frame.add(saveButton)

    val closeButton = JButton("Menu")
    closeButton.setBounds(150, 590, 100, 30)
    frame.add(closeButton)

    val clickHandler = object : MouseAdapter() {
        public override fun mouseClicked(e : MouseEvent?) {
            when (e?.getSource()) {
                saveButton -> {
                    db.confs.clear()
                    for (pair in table.getObjects{this}) {
                        db.confs.put((pair[0] as StringValue).str, (pair[1] as StringValue).str)
                    }
                    db.save(true)
                }

                closeButton -> {
                    frame.setVisible(false)
                    frame.dispose()
                    showVariantsWindow(null, username)
                }

                else -> println("unknown click source")
            }
        }
    }

    saveButton.addMouseListener(clickHandler)
    closeButton.addMouseListener(clickHandler)

    frame.setVisible(true)
}

fun showSearchWindow(val key : String, val username : String) {
    val frame = JFrame("Search")
    frame.setLayout(null)
    frame.setBounds(100, 100, 600, 280)
    frame.setResizable(false)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

    val rootPathLabel = JLabel("Root path")
    rootPathLabel.setBounds(10, 10, 200, 30)
    frame.add(rootPathLabel)

    val rootPathField = JTextField()
    rootPathField.setBounds(210, 10, 350, 30)
    rootPathField.addFocusListener(PlaceHolder(rootPathField, "smth like: C:\\Users\\Dina\\"))
    frame.add(rootPathField)

    val fileNameLabel = JLabel("File name")
    fileNameLabel.setBounds(10, 50, 200, 30)
    frame.add(fileNameLabel)

    val fileNameField = JTextField()
    fileNameField.setBounds(210, 50, 350, 30)
    fileNameField.addFocusListener(PlaceHolder(fileNameField, "smth like: file*00.* or *.txt"))
    frame.add(fileNameField)

    val fileSizeLabel = JLabel("File size (min-max), KB")
    fileSizeLabel.setBounds(10, 90, 200, 30)
    frame.add(fileSizeLabel)

    val fileSizeField = JTextField("0 - 1024")
    fileSizeField.setBounds(210, 90, 350, 30)
    frame.add(fileSizeField)

    val dateLabel = JLabel("File last change (min-max)")
    dateLabel.setBounds(10, 130, 200, 30)
    frame.add(dateLabel)

    var dateField = JTextField()
    dateField.setBounds(210, 130, 350, 30)
    dateField.addFocusListener(PlaceHolder(dateField, "1.1.2000-1.1.2020"))
    frame.add(dateField)

    val substringLabel = JLabel("Substring")
    substringLabel.setBounds(10, 170, 200, 30)
    frame.add(substringLabel)

    val substringField = JTextField()
    substringField.setBounds(210, 170, 350, 30)
    substringField.addFocusListener(PlaceHolder(substringField, "some substring to search in file"))
    frame.add(substringField)

    val searchButton = JButton("Search")
    searchButton.setBounds(10, 220, 100, 30)
    frame.add(searchButton)

    val saveButton = JButton("Save as")
    saveButton.setBounds(150, 220, 100, 30)
    frame.add(saveButton)

    val encryptFlag = JCheckBox("Encrypt result?");
    encryptFlag.setBounds(290, 220, 200, 30)
    frame.add(encryptFlag)

    val closeButton = JButton("Menu")
    closeButton.setBounds(490, 220, 100, 30)
    frame.add(closeButton)

    val clickHandler = object : MouseAdapter() {
        public override fun mouseClicked(e : MouseEvent?) {
            when (e?.getSource()) {
                searchButton -> {
                    JOptionPane.showMessageDialog(frame, "${doSearch().size} file(s) has been found", "Search results", JOptionPane.INFORMATION_MESSAGE)
                }

                saveButton -> {
                    val fc = JFileChooser()
                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY)
                    fc.setMultiSelectionEnabled(false)
                    if(JFileChooser.APPROVE_OPTION == fc.showOpenDialog(frame)) {
                        val file = fc.getSelectedFile()!!
                        val results = doSearch()
                        val sb = StringBuilder()
                        for(result in results) {
                            sb.append(result.toString())!!.append("\n")
                        }
                        file.writeText(sb.toString()!!)

                        if(encryptFlag.isSelected()) {
                            encryptFile(key, file)
                        }
                    }
                }

                closeButton -> {
                    frame.setVisible(false)
                    frame.dispose()
                    showVariantsWindow(null, username)
                }

                else -> println("unknown click source")
            }
        }

        fun doSearch() : List<FileInfo> {
            try{
                val rootPath = File(rootPathField.getText()!!)
                if(!rootPath.isDirectory()) {
                    JOptionPane.showMessageDialog(frame, "Root path is not a folder", "Error", JOptionPane.ERROR_MESSAGE)
                    return ArrayList<FileInfo>()
                }

                val fileName = fileNameField.getText()!!
                if(fileName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "File name is not specified", "Error", JOptionPane.ERROR_MESSAGE)
                    return ArrayList<FileInfo>()
                }

                val sizeRange = fileSizeField.getText()!!.split("-")!!
                if(sizeRange.size != 2) {
                    JOptionPane.showMessageDialog(frame, "Size range is incorrect or not specified", "Error", JOptionPane.ERROR_MESSAGE)
                    return ArrayList<FileInfo>()
                }

                val minSize = sizeRange[0]!!.trim().toLong() * 1024
                val maxSize = sizeRange[1]!!.trim().toLong() * 1024

                val dateRange = dateField.getText()!!.split("-")!!
                if(dateRange.size != 2) {
                    JOptionPane.showMessageDialog(frame, "Date range is incorrect or not specified", "Error", JOptionPane.ERROR_MESSAGE)
                    return ArrayList<FileInfo>()
                }
                val minDateArr = dateRange[0]!!.split("[.]")!!
                val minDate = Date(minDateArr[2]!!.toInt() - 1900, minDateArr[1]!!.toInt() - 1, minDateArr[0]!!.toInt())
                val maxDateArr = dateRange[1]!!.split("[.]")!!
                val maxDate = Date(maxDateArr[2]!!.toInt() - 1900, maxDateArr[1]!!.toInt() - 1, maxDateArr[0]!!.toInt())

                val substring = substringField.getText()!!

                return searchFiles(rootPath.getAbsolutePath()!!, NameCriterion(fileName), SizeCriterion(minSize, maxSize), LastModifiedCriterion(minDate.getTime(), maxDate.getTime()), ContentCriterion(if (substring.equals("some substring to search in file")) "" else substring))
            }catch(e : Exception) {
                JOptionPane.showMessageDialog(frame, "Internal error", "Error", JOptionPane.ERROR_MESSAGE)
                e.printStackTrace()
            }

            return ArrayList<FileInfo>()
        }
    }

    searchButton.addMouseListener(clickHandler)
    saveButton.addMouseListener(clickHandler)
    closeButton.addMouseListener(clickHandler)

    frame.setVisible(true)
}

fun Map.Entry<String, String>.asColumns() : List<Value> = arrayList(StringValue(this.getKey()), StringValue(this.getValue()))

val confColumns = arrayList(Column("Key", false), Column("Value"))
