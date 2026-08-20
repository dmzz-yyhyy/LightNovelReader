package indi.dmzz_yyhyy.lightnovelreader.utils.md

class MDLine(var listTypeName: MDLineType, level: Int, content: String) {
    private var level: Int = 0
    private val content: StringBuilder

    init {
        this.level = level
        this.content = StringBuilder(content)
    }

    fun create(line: String): MDLine {
        var spaces = 0
        while ((spaces < line.length) && (line[spaces] == ' ')) {
            spaces++
        }
        var content: String = line.substring(spaces)

        val newLevel: Int = spaces / 4

        if (content.isNotEmpty()) {
            if (content.matches("^[0-9]+\\.\\s.*".toRegex())) {
                var c = 0
                while ((c < content.length) && (Character.isDigit(content[c]))) {
                    c++
                }
                return MDLine(MDLineType.Ordered, newLevel, content.substring(c + 2))
            } else if (content.matches("^([*+\\-])\\s.*".toRegex())) {
                return MDLine(MDLineType.Unordered, newLevel, content.substring(2))
            } else if (content.matches("^#+.*".toRegex())) {
                var c = 0
                while ((c < content.length) && (content[c] == '#')) {
                    c++
                }
                val headerType: MDLineType = when (c) {
                    1 -> MDLineType.Head1
                    2 -> MDLineType.Head2
                    else -> MDLineType.Head3
                }

                while ((c < content.length) && (content[c] == ' ')) {
                    c++
                }

                return MDLine(headerType, newLevel, content.substring(c))
            }
        }

        content = line.substring(4 * newLevel)

        return MDLine(MDLineType.None, newLevel, content)
    }

    private fun getLevel(): Int {
        return level
    }

    override fun toString(): String {
        val newLine: StringBuilder = StringBuilder()
        for (j in 0 until getLevel()) {
            newLine.append("    ")
        }

        when (listTypeName) {
            MDLineType.Ordered -> {
                newLine.append(1.toString()).append(". ")
            }

            MDLineType.Unordered -> {
                newLine.append("* ")
            }

            MDLineType.Head1 -> {
                newLine.append("# ")
            }

            MDLineType.Head2 -> {
                newLine.append("## ")
            }

            MDLineType.Head3 -> {
                newLine.append("### ")
            }

            MDLineType.HR -> {
                newLine.append("----")
            }

            MDLineType.None -> {}
        }

        var contentStr: String = getContent()
        if (listTypeName == MDLineType.Unordered) {
            contentStr = contentStr.replace("^\n".toRegex(), "")
        }
        newLine.append(contentStr)

        return newLine.toString()
    }

    fun getContent(): String {
        return content.toString()
    }

    fun append(appendContent: String) {
        if (content.isEmpty()) {
            var i = 0
            while (i < appendContent.length && Character.isWhitespace(appendContent[i])) {
                i++
            }
            content.append(appendContent.substring(i))
        } else {
            content.append(appendContent)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is MDLine && other.listTypeName == this.listTypeName
    }

    override fun hashCode(): Int {
        var result = listTypeName.hashCode()
        result = 31 * result + level
        result = 31 * result + content.hashCode()
        return result
    }

    enum class MDLineType {
        Ordered, Unordered, None, Head1, Head2, Head3, HR
    }
}