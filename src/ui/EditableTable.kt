package ui.et

import java.util.ArrayList
import java.util.List
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellEditor
import javax.swing.WindowConstants

class EditableTable(val columns : List<Column>) {
    val table = JTable();

    {
        table.setAutoCreateRowSorter(true)
    }

    fun getScrollPane(x : Int, y : Int, width : Int, height : Int) : JScrollPane {
        val scroll = JScrollPane(table)
        scroll.setBounds(x, y, width, height)
        return scroll
    }

    fun getScrollPane(width : Int, height : Int) : JScrollPane {
        val scroll = JScrollPane(table)
        scroll.setSize(width, height)
        return scroll
    }

    var data : List<List<Value>> = ArrayList<List<Value>>()
    set(newData) {
        $data = newData
        table.setModel(object : AbstractTableModel() {
            public override fun getRowCount() = data.size()
            public override fun getColumnCount() = columns.size()
            public override fun getValueAt(row : Int, column : Int) = data[row][column]
            public override fun isCellEditable(row : Int, column : Int) = columns[column].editable
            public override fun getColumnName(column : Int) = columns[column].name

            public override fun setValueAt(newValue : Any?, row : Int, column : Int) {
                data[row][column] = if (newValue is String) StringValue(newValue) else newValue as Value
                fireTableCellUpdated(row, column)
            }
        })
        var idx = 0
        for (column in columns) {
            table.getColumnModel()!!.getColumn(idx++)!!.setCellEditor(column.editor)
        }
    }

}

class Column(val name : String, val editable : Boolean = true, val editor : TableCellEditor? = null)

trait Value

trait ValueEditor

class StringValue(val str : String) : Value {
    fun toString() : String = str
}

class PasswordValue(val password : String) : Value {
    fun toString() : String = password
}

fun String.times(count : Int) : String {
    val builder = StringBuilder()
    for (i in 1..count) {
        builder.append(this)
    }

    return builder.toString()!!
}

fun main(args : Array<String>) {
    val frame = JFrame("test")
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
