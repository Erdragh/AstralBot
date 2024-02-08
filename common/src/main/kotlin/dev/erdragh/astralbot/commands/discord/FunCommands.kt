package dev.erdragh.astralbot.commands.discord

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

object HeadpatCommand : HandledSlashCommand {
    private const val USER_OPTION = "user"
    override val command: SlashCommandData = Commands.slash("headpat", "Headpats a user")
        .addOption(OptionType.USER, USER_OPTION, "The user whose avatar will be headpat.", true)
    val headpatBaseImage = ImageIO.read(this.javaClass.getResource("/headpat.png"))

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue()

        val user = event.getOption(USER_OPTION)?.asUser
        if (user == null) {
            event.hook.sendMessage("You have to specify a user to headpat.").queue()
            return
        }

        val url = URL(user.effectiveAvatarUrl)
        val avatar = ImageIO.read(url)
        val headpatImage = BufferedImage(headpatBaseImage.width, headpatBaseImage.height, BufferedImage.TYPE_INT_ARGB)

        val graphics = headpatImage.createGraphics()

        val xOffset = 20
        val yOffset = 20
        graphics.drawImage(
            avatar,
            xOffset,
            yOffset,
            headpatImage.width - xOffset,
            headpatImage.height - yOffset,
            Color(0, 0, 0, 0),
            null
        )
        graphics.drawImage(
            headpatBaseImage,
            0,
            0,
            headpatBaseImage.width,
            headpatBaseImage.height,
            Color(0, 0, 0, 0),
            null
        )

        graphics.dispose()
        val byteStream = ByteArrayOutputStream()
        ImageIO.write(headpatImage, "png", byteStream)
        event.hook.sendFiles(FileUpload.fromData(byteStream.toByteArray(), "headpat.png")).queue()
    }

}