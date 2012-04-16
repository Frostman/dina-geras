package ui

import auth.AuthDb
import auth.Role
import auth.User
import auth.checkCredentials
import crypt.encryptFile
import java.awt.Color
import java.awt.Frame
import java.awt.Toolkit
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.util.ArrayList
import java.util.List
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPasswordField
import javax.swing.JTextField
import javax.swing.WindowConstants
import javax.swing.text.JTextComponent
import ui.et.Column
import ui.et.EditableTable
import ui.et.PasswordValue
import ui.et.StringValue
import ui.et.Value

fun main(args : Array<String>) {
    val dbPath = "database"
    val key = "test key"

    if(!File(dbPath).exists()) {
        val db = AuthDb("database")
        db.users.put("admin", User("admin", "admin", Role.ADMIN, 0))
        db.save()
        encryptFile(key, File("database"))
    }

    val result = askForCredentials(null, {checkCredentials(it._1, it._2, "test key", dbPath)})

    println("auth: ${result._1}")

    if (result._1 == true) {
        showMainWindow()
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

fun showMainWindow() {
    val frame = JFrame("Users list")
    frame.setLayout(null)
    frame.setBounds(100, 100, 600, 600)
    frame.setResizable(false)
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

    val table = EditableTable(arrayList(Column("column_1"), Column("column_2")))
    frame.add(table.getScrollPane(10, 10, 580, 560))

    val data = ArrayList<List<Value>>()
    data.add(arrayList(StringValue("test_1_1"), PasswordValue("test_2_1")))
    data.add(arrayList(StringValue("test_1_2"), PasswordValue("test_2_2")))
    data.add(arrayList(StringValue("test_1_3"), PasswordValue("test_2_3")))
    data.add(arrayList(StringValue("test_1_4"), PasswordValue("test_2_4")))
    table.data = data

    frame.setVisible(true)
}
