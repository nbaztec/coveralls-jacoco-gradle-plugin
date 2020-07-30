package foo.bar.baz

import foo.bar.baz.internal.Util

fun main() {
    val a = 1
    val b = 2
    val c = 3

    if (Util.square(add(a, b)) < Util.square(add(b, c))) {
        println("OK")
    } else {
        println("FAIL")
    }
}

fun add(a: Int, b: Int): Int {
    return a + b
}
