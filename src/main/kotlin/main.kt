import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.promise
import kotlin.browser.window
import kotlin.js.Promise

fun chaos(arg: Int) {
    (1..5).forEach {
        if (it == 3) {
            console.log(arg)
            throw Exception("no one cares")
        }
    }
}

suspend fun firstLevel(): Promise<Int> {
    return GlobalScope.promise {
        (1..5).forEach {
            delay(it.toLong())
            chaos(it)
        }
        1
    }
}

suspend fun middle(): Int {
    delay(1)
    return firstLevel().await()
}

suspend fun waitMe() {
    delay(1000)
    console.log("GET READY")
    delay(1000)
    val qwe = middle()
    console.log(qwe)
}

fun main() {
    val a = GlobalScope.promise {
        waitMe()
    }
//    console.log(::isSubmissionError, isSubmissionError("??"))
//    window.asDynamic().their = ::isSubmissionError
    window.asDynamic().myPromise = a
}
