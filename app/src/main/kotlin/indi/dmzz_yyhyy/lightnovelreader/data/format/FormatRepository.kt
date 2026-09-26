package indi.dmzz_yyhyy.lightnovelreader.data.format

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.FormattingRuleDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.FormattingRuleEntity
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.content.component.data.TextData
import io.nightfish.lightnovelreader.api.explore.ExploreDisplayBook
import io.nightfish.lightnovelreader.api.text.ComponentProcessor
import io.nightfish.lightnovelreader.api.text.TextProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.regex.PatternSyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormatRepository @Inject constructor(
    private val formattingRuleDao: FormattingRuleDao,
) : TextProcessor {
    override val enabled = true
    private val processorMap = mutableMapOf<String, SnapshotStateList<FormattingRule>>()

    init {
        runBlocking(Dispatchers.IO) {
            replaceRules(formattingRuleDao.getAllBookRuleEntity())
        }
        CoroutineScope(Dispatchers.IO).launch {
            formattingRuleDao.getAllBookRuleEntityFlow().collect { formattingRuleEntities ->
                replaceRules(formattingRuleEntities)
            }
        }
    }

    private fun replaceRules(formattingRuleEntities: List<FormattingRuleEntity>) {
        processorMap.values.forEach { it.clear() }
        formattingRuleEntities.forEach { formattingRule ->
            processorMap.getOrPut(formattingRule.bookId) { mutableStateListOf() }.add(
                FormattingRule(
                    id = formattingRule.id,
                    name = formattingRule.name,
                    isRegex = formattingRule.isRegex,
                    match = formattingRule.match,
                    replacement = formattingRule.replacement,
                    isEnabled = formattingRule.isEnabled,
                )
            )
        }
    }

    private fun String.unescapeControlChars(): String {
        return this.replace(Regex("""\\[\\"tnrbf]|\\u[0-9a-fA-F]{4}""")) {
            when (it.value) {
                "\\n" -> "\n"
                "\\t" -> "\t"
                "\\r" -> "\r"
                "\\b" -> "\b"
                "\\f" -> "\u000c"
                "\\\"" -> "\""
                "\\\\" -> "\\"
                else -> {
                    if (it.value.startsWith("\\u")) {
                        it.value.substring(2).toInt(16).toChar().toString()
                    } else it.value
                }
            }
        }
    }

    private fun String.replaceTextWithRegex(regex: Regex, replaced: String): String {
        var result = this
        for (matchResult in regex.findAll(this)) {
            var progressedReplacedText = replaced
            matchResult.groups.forEachIndexed { index, matchGroup ->
                matchGroup?.value?.let {
                    progressedReplacedText = progressedReplacedText.replace("$$index", it)
                }
            }
            result = result.replace(matchResult.value, progressedReplacedText)
        }
        return result
    }

    private fun processText(bookId: String, text: String): String {
        var result = text
        for (formattingRule in processorMap.getOrDefault("", emptyList()).filter { it.isEnabled }) {
            try {
                result = if (formattingRule.isRegex) result.replaceTextWithRegex(
                    Regex(formattingRule.match.unescapeControlChars()),
                    formattingRule.replacement.unescapeControlChars()
                )
                else result.replace(
                    formattingRule.match.unescapeControlChars(),
                    formattingRule.replacement.unescapeControlChars()
                )
            } catch (_: PatternSyntaxException) {
            }
        }
        for (formattingRule in processorMap.getOrDefault(bookId, emptyList())
            .filter { it.isEnabled }) {
            try {
                result = if (formattingRule.isRegex) result.replaceTextWithRegex(
                    Regex(formattingRule.match.unescapeControlChars()),
                    formattingRule.replacement.unescapeControlChars()
                )
                else result.replace(
                    formattingRule.match.unescapeControlChars(),
                    formattingRule.replacement.unescapeControlChars()
                )
            } catch (_: PatternSyntaxException) {
            }
        }
        return result
    }

    suspend fun getFormattingRules(id: Int): FormattingRule? {
        val formattingRule = formattingRuleDao.getBookRuleEntity(id) ?: return null
        return FormattingRule(
            id = formattingRule.id,
            name = formattingRule.name,
            isRegex = formattingRule.isRegex,
            match = formattingRule.match,
            replacement = formattingRule.replacement,
            isEnabled = formattingRule.isEnabled
        )
    }

    suspend fun insertRule(bookId: String, formattingRule: FormattingRule) {
        formattingRuleDao.insertRuleEntity(
            FormattingRuleEntity(
                bookId = bookId,
                name = formattingRule.name,
                isRegex = formattingRule.isRegex,
                match = formattingRule.match,
                replacement = formattingRule.replacement,
                isEnabled = formattingRule.isEnabled
            )
        )
    }

    suspend fun updateRule(bookId: String, formattingRule: FormattingRule) {
        formattingRule.id ?: return
        formattingRuleDao.insertRuleEntity(
            FormattingRuleEntity(
                id = formattingRule.id,
                bookId = bookId,
                name = formattingRule.name,
                isRegex = formattingRule.isRegex,
                match = formattingRule.match,
                replacement = formattingRule.replacement,
                isEnabled = formattingRule.isEnabled,
            )
        )
    }

    suspend fun deleteRule(ruleId: Int) {
        formattingRuleDao.deleteRule(ruleId)
    }

    fun getFormattingMap(): Map<String, List<FormattingRule>> = processorMap

    fun getStateBookFormattingRules(bookId: String): List<FormattingRule> {
        if (!processorMap.contains(bookId)) processorMap[bookId] = mutableStateListOf()
        return processorMap[bookId]!!
    }

    override fun processText(text: String): String = processText("", text)

    override fun processChapterContent(
        bookId: String,
        chapterContent: ChapterContent,
        componentProcessor: ComponentProcessor,
    ): ChapterContent {
        return chapterContent.copy(
            content = componentProcessor.apply {
                process { data: TextData<*> ->
                    data.processText { text -> processText(bookId, text) }
                }
            }.get(),
        )
    }

    override fun processExploreBooksRow(exploreDisplayBook: ExploreDisplayBook): ExploreDisplayBook =
        exploreDisplayBook.copy(
            title = processText("", exploreDisplayBook.title),
            author = processText("", exploreDisplayBook.author),
        )

    override fun processBookInformation(bookInformation: BookInformation): BookInformation =
        bookInformation.copy(
            title = processText(bookInformation.id, bookInformation.title),
            subtitle = processText(bookInformation.id, bookInformation.subtitle),
            author = processText(bookInformation.id, bookInformation.author),
            description = processText(bookInformation.id, bookInformation.description),
            publishingHouse = processText(bookInformation.id, bookInformation.publishingHouse),
        )

    override fun processBookVolumes(bookVolumes: BookVolumes): BookVolumes = bookVolumes.copy(
        volumes = bookVolumes.volumes.map { volume ->
            volume.copy(
                volumeTitle = processText(bookVolumes.bookId, volume.volumeTitle),
                chapters = volume.chapters.map {
                    it.copy(
                        title = processText(bookVolumes.bookId, it.title)
                    )
                })
        })
}
