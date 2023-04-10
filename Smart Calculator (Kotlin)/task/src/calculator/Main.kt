package calculator

import java.math.BigInteger
import kotlin.math.pow

fun main() = Calculator().getUserInput()

class Calculator() {

    companion object{
        private val precedenceMap = mutableMapOf("+" to 0,
                                                 "-" to 0,
                                                 "*" to 1,
                                                 "/" to 1,
                                                 "^" to 2)
        private val variables = mutableMapOf<String, BigInteger>()
        private val infixList = mutableListOf<String>()
        private val invalidVarRegex = Regex("(\\d+\\w+)+|(\\w+\\d+)+|(\\d+\\w+\\d+)+|(\\w+\\d+\\w+)+")
        private val validRegex = Regex("\\-*\\d+")
    }

    fun getUserInput(){
        while(true){
            val input = readln()
            when {
                input.contains("=") -> assign(input)
                input.isDigit() -> println("$input")
                invalidVarRegex.matches(input) -> println("Invalid identifier")
                Regex("\\s*\\w+|\\w+\\s*|\\s*\\w\\s*").matches(input) -> println(variables[input.trim()] ?: "Unknown variable")
                input.contains("/exit") -> println("Bye!").also { return }
                input.contains("/help") -> println("Smart calculator")
                input.startsWith("/") -> println("Unknown command")
                input.count{it.toString().isLeftParenthesis()} != input.count{it.toString().isRightParenthesis()} -> println("Invalid expression")
                input.isEmpty() -> continue
                else -> {
                    try {
                        val data = cleanData(input)
                        println(calculate(data.split(Regex("(?=[\\+\\-\\*\\/\\^\\(\\)])|(?<=[\\+\\-\\*\\/\\^\\(\\)])"))
                            .filter { it.isNotEmpty() && !it.isSpace() }.map { it.trim() })
                        )

                    } catch (e: Exception) {
                        println("Invalid expression")
                    }
                }
            }
            infixList.clear()
        }
    }


    private fun cleanData(input: String): String{
        var data = input
        data = checkForVariables(data)
        data = data.convertToUnaryOperators()
        return data
    }

    private fun checkForVariables(input: String) =
        input.split("").filter { it.isNotEmpty() }.
                joinToString("") {element ->
                    if(variables[element] != null)
                        variables[element].toString()
                    else
                        element.trim()
                }


    private fun assign(input: String){
        val(key, value) = input.split("=").map { it.trim() }
        if(value in variables.keys)
            variables[key] = variables[value]!!
        else if(invalidVarRegex.matches(key))
            println("Invalid identifier").also { return }
         else if(!validRegex.matches(value) || input.count{it.toString() == "="} > 1)
            println("Invalid assignment").also { return }
        else if(value.isDigit())
            variables[key] = value.toBigInteger()
        else
            println("Invalid assignment")
    }

    private fun calculate(userInput: List<String>): BigInteger? {
        val tempStack = ArrayDeque(listOf<String>())
        convertToInfixList(userInput)
        for(element in infixList){
            if(element.isDigit())
                tempStack.addLast(element)
            else if(element.isOperator()){
                val num2 = tempStack.removeLast().toBigInteger()
                val num1 = tempStack.removeLast().toBigInteger()
                val result = num1.operator(element, num2)
                tempStack.addLast(result.toString())
            }
        }
        return tempStack.last().toBigInteger()
    }

    private fun convertToInfixList(list: List<String>){
        val tempStack = ArrayDeque(listOf<String>())
        for(element in list){
            when {
                element.isDigit() -> infixList.add(element)
                tempStack.isEmpty() || tempStack.last().isLeftParenthesis() -> tempStack.addLast(element)
                element.isLeftParenthesis() -> tempStack.addLast(element)
                element.isRightParenthesis() -> {
                    while(!tempStack.last().isLeftParenthesis()){
                        infixList.add(tempStack.last())
                        tempStack.removeLast()
                    }
                    tempStack.removeLast()
                }
                precedenceMap[element]!! > precedenceMap[tempStack.last()]!! -> tempStack.addLast(element)

                precedenceMap[element]!! <= precedenceMap[tempStack.last()]!! -> {
                    while(tempStack.isNotEmpty() &&
                        !tempStack.last().isLeftParenthesis() &&
                            precedenceMap[element]!! <= precedenceMap[tempStack.last()]!!
                            ){
                                infixList.add(tempStack.last())
                                tempStack.removeLast()
                        }

                    tempStack.addLast(element)
                }
            }
        }
        while(tempStack.isNotEmpty()){
            if(tempStack.last().isOperator())
                infixList.add(tempStack.last())
            tempStack.removeLast()
        }
    }
}

fun String.convertToUnaryOperators() =
    this.split(Regex("(?=[\\d)])|(?<=[\\d)])")).filter { it.isNotEmpty() && it != " " }.map { it.trim() }
        .joinToString("") {
                                        if (it.matches(Regex("\\+{2,}"))) "+"
                                        else if (it.matches(Regex("\\-{2,}")) && it.length % 2 == 0) "+"
                                        else if (it.matches(Regex("\\-{2,}"))) "-"
                                        else if(it.contains("+-")) "-"
                                        else it }


fun String.isDigit(): Boolean = this.toBigIntegerOrNull() != null

fun String.isOperator(): Boolean = this in ("+-*/^")
fun String.isLeftParenthesis(): Boolean = this == "("
fun String.isRightParenthesis(): Boolean = this == ")"
fun String.isSpace(): Boolean = this == " "

fun BigInteger.operator(stringOperator: String, secondNumber: BigInteger): BigInteger = when {
        stringOperator.contains("+") -> this.plus(secondNumber)
        stringOperator.contains("*") -> this.multiply(secondNumber)
        stringOperator.contains("/") -> this.divide(secondNumber)
        stringOperator.contains("^") -> this.pow(secondNumber.toInt())
        else -> this - secondNumber
    }

