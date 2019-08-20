package br.ufpe.cin.android.calculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private fun setButtonClick (bid: Int, txt: CharSequence, field: EditText) {
        findViewById<Button>(bid).setOnClickListener { field.append(txt) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<EditText>(R.id.text_calc)
        val display = findViewById<TextView>(R.id.text_info)

        setButtonClick(R.id.btn_0, "0", input)
        setButtonClick(R.id.btn_1, "1", input)
        setButtonClick(R.id.btn_2, "2", input)
        setButtonClick(R.id.btn_3, "3", input)
        setButtonClick(R.id.btn_4, "4", input)
        setButtonClick(R.id.btn_5, "5", input)
        setButtonClick(R.id.btn_6, "6", input)
        setButtonClick(R.id.btn_7, "7", input)
        setButtonClick(R.id.btn_8, "8", input)
        setButtonClick(R.id.btn_9, "9", input)
        setButtonClick(R.id.btn_Add, "+", input)
        setButtonClick(R.id.btn_Subtract, "-", input)
        setButtonClick(R.id.btn_Multiply, "*", input)
        setButtonClick(R.id.btn_Divide, "/", input)
        setButtonClick(R.id.btn_Power, "^", input)
        setButtonClick(R.id.btn_LParen, "(", input)
        setButtonClick(R.id.btn_RParen, ")", input)
        setButtonClick(R.id.btn_Dot, ".", input)

        findViewById<Button>(R.id.btn_Clear).setOnClickListener {
            input.setText("")
        }
        findViewById<Button>(R.id.btn_Equal).setOnClickListener {
            val result: Double
            try {
                result = eval(input.text.toString())
                display.setText(input.text.append(" = ").append(result.toString()).toString())
                input.setText("")
            } catch (err: RuntimeException) {
                Toast.makeText(applicationContext, err.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }


    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
