package foo.bar.baz.lib

class Lib {
    companion object {
        fun format(v: Int): String {
            return v.toString()
        }

        fun format(v: Float): String {
            return v.toString()
        }
    }
}
