package dev.erdragh.astralbot.commands.discord

import dev.erdragh.astralbot.util.GifWriter
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

object HeadpatCommand : HandledSlashCommand {
    private const val USER_OPTION = "user"
    override val command: SlashCommandData = Commands.slash("headpat", "Headpats a user")
        .addOption(OptionType.USER, USER_OPTION, "The user whose avatar will be headpat.", true)

    private val ANIMATION = floatArrayOf(-.05f, .1f, .2f, .19f, .1f)
    private val FRAMES: Array<BufferedImage> = Array(5) { ImageIO.read(this::class.java.getResourceAsStream("/headpat/pet$it.gif")) }
    private val RENDERING_HINTS = RenderingHints(mapOf(
        RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
        RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY
    ))

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue()

        val user = event.getOption(USER_OPTION)?.asUser
        if (user == null) {
            event.hook.sendMessage("You have to specify a user to headpat.").queue()
            return
        }

        val url = URL(user.effectiveAvatarUrl)
        val avatar = ImageIO.read(url)

        val stream: ByteArrayOutputStream

        ByteArrayOutputStream().use { output ->
            ImageIO.createImageOutputStream(output).use { out ->
                GifWriter(
                    out, BufferedImage.TYPE_INT_ARGB,
                    timeBetweenFramesMS = 50, loopContinuously = true, transparent = true
                ).use { gifWriter ->
                    for (i in FRAMES.indices) {
                        val frame = BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB)
                        val graphics = frame.graphics as Graphics2D
                        // set rendering hints to slightly improve quality
                        graphics.setRenderingHints(RENDERING_HINTS)

                        val offset1 = Math.round(ANIMATION[i] * 64)
                        val offset2 = Math.round((-ANIMATION[i]) * 64)

                        // draw avatar
                        graphics.drawImage(avatar, 2, 32 + offset1, 128 - offset2, 128 - 32 - offset1, null)
                        // draw hand
                        graphics.drawImage(FRAMES[i], 0, 0, 128, 128, null)

                        gifWriter.write(frame)

                        graphics.dispose()
                    }
                }
                out.flush()

                stream = output
            }
        }

        event.hook.sendFiles(FileUpload.fromData(stream.toByteArray(), "headpat.gif")).queue()
    }
}