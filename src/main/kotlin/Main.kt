package flashcards
//this is the farmer's code
import java.io.*

val inputLog = mutableListOf<String>()
val errCount = mutableMapOf<String, Int>()
val filePath = listOf("", "").toMutableList()
val cards = mutableMapOf<String, String>()
val menuMap = mutableMapOf(
    "add" to { cards.addCards() }, "remove" to { cards.removeCards() }, "import" to { cards.importCards() },
    "export" to { cards.exportCards() }, "ask" to { cards.askCards() }, "log" to { cards.logCards() },
    "hardest card" to { cards.hardestCards() }, "reset stats" to { clearError() }
)

fun input(prompt: String) = filePath[0].ifEmpty { println(prompt).run { readln().trim() } }
fun inputExp(prompt: String) = filePath[1].ifEmpty { println(prompt).run { readln().trim() } }

fun readln(): String {
    val input = kotlin.io.readln(); inputLog.add(input); return input
}

fun println(value: Any?) {
    kotlin.io.println(value); inputLog.add(value.toString())
}

fun MutableMap<String, String>.addCards() {
    val k = input("The card:")
    if (k in cards) println("The card \"$k\" already exists.").also { return }
    val v = input("The definition of the card:")
    if (v in cards.values) println("The definition \"$v\" already exists.").also { return }
    this[k] = v.also { println("The pair (\"$k\":\"$v\") has been added.") }
}

fun MutableMap<String, String>.removeCards() {
    val k = input("Which card?")
    this.remove(key = k) ?: println("Can't remove \"$k\": there is no such card.").also { return }
    println("The card has been removed.")
}

fun MutableMap<String, String>.importCards() {
    val fileName = File(input("File name:"))
    if (!fileName.exists()) println("File not found.").also { return }
    FileReader(fileName).use {
        it.forEachLine { line ->
            val (k, v, e) = line.split(":"); cards[k] = v; errCount[k] = e.toInt()
        }
    }.also { println("${fileName.readLines().size} cards have been loaded.") }
}

fun MutableMap<String, String>.exportCards() {
    PrintWriter(inputExp("File name:")).use { o -> cards.forEach { (k, v) -> o.println("$k:$v:${errCount[k] ?: 0}") } }
    println("${this.size} cards have been saved.")
}

fun MutableMap<String, String>.askCards() {
    this.entries.take(input("How many times to ask?").toInt()).forEach { (k, v) ->
        val value = input("Print the definition of \"$k\":")
        val keyF = cards.entries.firstOrNull { it.value == value }?.key
        when {
            value == v -> println("Correct!")
            keyF != null -> println("Wrong. The right answer is \"$v\", but your definition is correct for \"$keyF\".")
                .also { errCount[k] = errCount.getOrElse(k) { 0 } + 1 }
            else -> println("Wrong. The right answer is \"$v\".").also { errCount[k] = errCount.getOrElse(k) { 0 } + 1 }
        }
    }
}

fun MutableMap<String, String>.logCards() =
    File(input("File name:")).writeText(inputLog.joinToString("\n")).also { println("The log has been saved.") }

fun MutableMap<String, String>.hardestCards() {
    if (errCount.isEmpty()) println("There are no cards with errors.")
    else {
        val r = errCount.toList().sortedByDescending { it.second }.let { l -> l.filter { it.second == l[0].second } }
        if (r.size == 1) {
            println("The hardest card is \"${r[0].first}\". You have ${r[0].second} errors answering it.").also { return }
        }
        println("The hardest cards are " + r.joinToString(", ") { "\"${it.first}\"" } + " You have ${r[0].second} errors answering them.")
    }
}

fun clearError() = errCount.clear().also { println("Card statistics have been reset.") }

fun menu() {
    val action = input("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
    if (action == "exit") {
        if (filePath[1].isNotBlank()) menuMap["export"]?.invoke().also { return }
    } else menuMap[action]?.invoke().also { menu() }
}

fun main(args: Array<String>) {
    filePath[0] = args.getOrNull(args.indexOf("-import").inc()) ?: ""
    filePath[1] = args.getOrNull(args.indexOf("-export").inc()) ?: ""
    if (filePath[0].isNotBlank()) menuMap["import"]?.invoke().also { filePath[0] = "" }
    menu().also { println("Bye bye!") }
}