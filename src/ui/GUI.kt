package ui

import javax.swing.JDialog
import java.awt.Frame
import java.awt.Toolkit
import javax.swing.JTextField
import java.awt.event.FocusListener
import java.awt.event.FocusEvent
import javax.swing.text.JTextComponent
import java.awt.Color
import javax.swing.JPasswordField
import javax.swing.JButton
import javax.swing.JWindow
import javax.swing.WindowConstants
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import auth.User
import auth.Role
import auth.checkCredentials
import javax.swing.JLabel

fun main(args : Array<String>) {
    val result = askForCredentials(null, {checkCredentials(it._1, it._2)})

    println("auth: ${result._1}")

    System.exit(0)
}

fun askForCredentials(val parent : Frame? = null, checker : (#(String, String))->Boolean) : #(Boolean, String, String) {
    val dialog = JDialog(parent, true)

    dialog.setTitle("Pleasr, enter login and password")
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    dialog.setResizable(false)
    dialog.getContentPane()?.setLayout(null)
    val width = 320
    val height = 170
    dialog.setSize(width, height)
    val screenSize = Toolkit.defaultToolkit?.getScreenSize().sure()
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
    errorLabel.setHorizontalTextPosition(0);
    errorLabel.setVerticalTextPosition(0);
    errorLabel.setForeground(Color.RED)

    dialog.getContentPane()?.add(errorLabel)


    val okButton = JButton("Ok")
    okButton.setBounds(57, 112, 100, 24)
    dialog.getContentPane()?.add(okButton)

    val cancelButton = JButton("Cancel")
    cancelButton.setBounds(163, 112, 100, 24)
    dialog.getContentPane()?.add(cancelButton)


    dialog.addWindowListener(object : WindowAdapter() {
        override fun windowGainedFocus(e: WindowEvent?) {
            okButton.requestFocusInWindow();
        }
    });

    var auth = false

    val clickHandler = object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            when (e?.getSource()) {
                okButton ->
                    if(!checker(#(loginField.getText().sure(), passwordField.getText().sure()))) {
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

    return #(auth, loginField.getText().sure(), passwordField.getText().sure())
}

class PlaceHolder(val component : JTextComponent, val placeholder : String) : FocusListener {
    var componentColor : Color? = null
    var placeholderMode = false

    {
        focusLost(null)
    }

    override fun focusGained(e: FocusEvent? = null) {
        if (placeholderMode) {
            component.setForeground(componentColor);
            component.setText("")
            placeholderMode = false
        }
    }

    override fun focusLost(e: FocusEvent?) {
        componentColor = component.getForeground()
        if (component.getText()?.trim()?.length == 0) {
            component.setForeground(Color.GRAY);
            component.setText(placeholder);
            placeholderMode = true
        }
    }
}
