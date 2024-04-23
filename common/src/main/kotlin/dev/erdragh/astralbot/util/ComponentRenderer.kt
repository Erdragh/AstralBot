package dev.erdragh.astralbot.util

import dev.erdragh.astralbot.config.AstralBotConfig
import dev.erdragh.astralbot.config.AstralBotTextConfig
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import org.commonmark.node.*
import org.commonmark.renderer.NodeRenderer
import java.util.Stack

class ComponentRenderer : AbstractVisitor(), NodeRenderer {
    companion object {
        private abstract class ListHolder(val parent: ListHolder?)

        private class BulletListHolder(parent: ListHolder?, list: BulletList) : ListHolder(parent) {
            val marker: String = list.marker
        }

        private class OrderedListHolder(parent: ListHolder?, list: OrderedList) : ListHolder(parent) {
            val delimiter: String = list.markerDelimiter
            var number: Int = list.markerStartNumber
        }
    }

    private var currentComponent: MutableComponent = Component.empty()
    private val prefixes: Stack<Component> = Stack()
    private var listHolder: ListHolder? = null
    private var shouldAddBlock: Boolean = false

    override fun getNodeTypes(): MutableSet<Class<out Node>> {
        return mutableSetOf(
            Document::class.java,
            Heading::class.java,
            Emphasis::class.java,
            Link::class.java,
            Paragraph::class.java,
            StrongEmphasis::class.java,
            Text::class.java,
            BlockQuote::class.java,
            Code::class.java,
            FencedCodeBlock::class.java,
            BulletList::class.java,
            OrderedList::class.java,
            ListItem::class.java,
            SoftLineBreak::class.java,
            HardLineBreak::class.java
        )
    }

    private fun childIntoCurrent(component: MutableComponent, action: () -> Unit) {
        val temp = currentComponent
        currentComponent = component
        action()
        temp.append(currentComponent)
        currentComponent = temp
    }

    fun renderToComponent(node: Node?): MutableComponent {
        this.render(node)
        return currentComponent
    }

    override fun render(node: Node?) {
        node?.accept(this)
    }

    override fun visit(document: Document?) {
        visitChildren(document)
    }

    override fun visit(heading: Heading?) {
        if (heading == null) return

        block()
        childIntoCurrent(Component.empty().withStyle(ChatFormatting.BOLD)) {
            visitChildren(heading)
        }
        block()
    }

    override fun visit(emphasis: Emphasis?) {
        childIntoCurrent(Component.empty().withStyle(ChatFormatting.ITALIC)) {
            super.visit(emphasis)
        }
    }

    override fun visit(link: Link?) {
        if (link == null) return
        this.formatLink(link, link.title, link.destination)
    }

    override fun visit(paragraph: Paragraph?) {
        visitChildren(paragraph)
        block()
    }

    override fun visit(strongEmphasis: StrongEmphasis?) {
        childIntoCurrent(Component.empty().withStyle(ChatFormatting.BOLD)) {
            super.visit(strongEmphasis)
        }
    }

    override fun visit(text: Text?) {
        if (text == null) return

        val literal = text.literal

        val matcher = urlPattern.matcher(text.literal)
        var lastEnd = 0
        for (result in matcher.results()) {
            append(literal.substring(lastEnd..<result.start()))
            formatLink(null, result.group(), result.group())
            lastEnd = result.end()
        }
        append(literal.substring(lastEnd))
    }

    override fun visit(blockQuote: BlockQuote?) {
        prefixes.push(Component.literal("> ").withStyle(ChatFormatting.DARK_GRAY).withStyle { it.withItalic(false) })
        block()
        childIntoCurrent(Component.empty().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)) {
            visitChildren(blockQuote)
        }
        prefixes.pop()
        block()
    }

    override fun visit(code: Code?) {
        if (code == null) return
        append(
            Component.literal("`${code.literal}`")
                .withStyle(ChatFormatting.YELLOW)
        )
    }

    override fun visit(fencedCodeBlock: FencedCodeBlock?) {
        if (fencedCodeBlock == null) return
        append(Component.literal(fencedCodeBlock.literal).withStyle(ChatFormatting.YELLOW))
    }

    override fun visit(bulletList: BulletList?) {
        if (bulletList == null) return
        listHolder = BulletListHolder(listHolder, bulletList)
        visitChildren(bulletList)
        listHolder = listHolder!!.parent
        block()
    }

    override fun visit(orderedList: OrderedList?) {
        if (orderedList == null) return
        listHolder = OrderedListHolder(listHolder, orderedList)
        visitChildren(orderedList)
        listHolder = listHolder!!.parent
        block()
    }

    override fun visit(listItem: ListItem?) {
        if (listItem == null) return
        val markerIndent = listItem.markerIndent
        val marker: String

        when (listHolder) {
            is BulletListHolder -> {
                marker = " ".repeat(markerIndent) + (listHolder!! as BulletListHolder).marker
            }
            is OrderedListHolder -> {
                val holder = listHolder!! as OrderedListHolder
                marker = " ".repeat(markerIndent) + holder.number + holder.delimiter
                holder.number++
            }
            else -> {
                throw IllegalStateException("Unknown list holder type: $listHolder")
            }
        }

        block()

        val contentIndent = listItem.contentIndent
        append(marker)
        append(" ".repeat(contentIndent - marker.length))
        prefixes.push(Component.literal(" ".repeat(contentIndent)))

        if (listItem.firstChild != null) {
            // not an empty list
            visitChildren(listItem)
        }
        prefixes.pop()
    }

    override fun visit(softLineBreak: SoftLineBreak?) {
        newline()
    }

    override fun visit(hardLineBreak: HardLineBreak?) {
        currentComponent.append("  ")
        newline()
    }

    private fun newline() {
        currentComponent.append("\n")
        for (prefix in prefixes.asIterable()) {
            currentComponent.append(prefix)
            println("applying prefix $prefix")
        }
    }

    private fun formatLink(node: Node?, title: String?, destination: String) {
        childIntoCurrent(Component.empty()
            .withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE)
            .withStyle {
                if (AstralBotConfig.urlAllowed(destination)) {
                    it
                        .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, destination))
                        .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(destination)))
                } else {
                    it
                        .withColor(ChatFormatting.RED)
                        .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(AstralBotTextConfig.GENERIC_BLOCKED.get())))
                }
            }
        ) {
            node?.let(::visitChildren)
            if (title != null) {
                append(title)
            }
        }
    }

    private fun block() {
        this.shouldAddBlock = true
    }

    private fun append(component: Component) {
        if (this.shouldAddBlock) {
            this.shouldAddBlock = false
            newline()
        }
        this.currentComponent.append(component)
    }

    private fun append(literal: String) {
        append(Component.literal(literal))
    }
}