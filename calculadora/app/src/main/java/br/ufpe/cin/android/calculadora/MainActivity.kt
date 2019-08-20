package br.ufpe.cin.android.calculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    //adiciona ao botão bid um listener
    //o listener adiciona txt ao input
    private fun setButtonListenerToAppend (bid: Int, txt: CharSequence, input: EditText) {
        findViewById<Button>(bid).setOnClickListener { input.append(txt) }
    }

    //adiciona o listener apropriado a cada botão de texto
    private fun setExpressionBuildingButtonListeners (input: EditText) {
        setButtonListenerToAppend(R.id.btn_0, "0", input)
        setButtonListenerToAppend(R.id.btn_3, "3", input)
        setButtonListenerToAppend(R.id.btn_1, "1", input)
        setButtonListenerToAppend(R.id.btn_2, "2", input)
        setButtonListenerToAppend(R.id.btn_4, "4", input)
        setButtonListenerToAppend(R.id.btn_5, "5", input)
        setButtonListenerToAppend(R.id.btn_6, "6", input)
        setButtonListenerToAppend(R.id.btn_7, "7", input)
        setButtonListenerToAppend(R.id.btn_8, "8", input)
        setButtonListenerToAppend(R.id.btn_9, "9", input)
        setButtonListenerToAppend(R.id.btn_Add, "+", input)
        setButtonListenerToAppend(R.id.btn_Subtract, "-", input)
        setButtonListenerToAppend(R.id.btn_Multiply, "*", input)
        setButtonListenerToAppend(R.id.btn_Divide, "/", input)
        setButtonListenerToAppend(R.id.btn_Power, "^", input)
        setButtonListenerToAppend(R.id.btn_LParen, "(", input)
        setButtonListenerToAppend(R.id.btn_RParen, ")", input)
        setButtonListenerToAppend(R.id.btn_Dot, ".", input)
    }

    //limpa o campo de input
    private fun setClearButtonListener(input: EditText) {
        findViewById<Button>(R.id.btn_Clear).setOnClickListener {
            input.setText("")
        }
    }

    //avalia a expressão
    //se tudo der certo, exibe a expressão e sua avaliação no display e limpa o campo de input
    //senão, exibe um toast com a mensagem de erro
    private fun setEvaluateButtonListener(input: EditText, display: TextView) {
        findViewById<Button>(R.id.btn_Equal).setOnClickListener {
            val result: Double
            try {
                result = eval(input.text.toString())
                display.setText(input.text.append(" = ").append(result.toString()).toString())
            } catch (err: RuntimeException) {
                Toast.makeText(applicationContext, err.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    //adiciona todos os listeners necessários
    private fun setButtonListeners() {
        val input = findViewById<EditText>(R.id.text_calc)
        val display = findViewById<TextView>(R.id.text_info)

        setExpressionBuildingButtonListeners(input)
        setClearButtonListener(input)
        setEvaluateButtonListener(input, display)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setButtonListeners()
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
