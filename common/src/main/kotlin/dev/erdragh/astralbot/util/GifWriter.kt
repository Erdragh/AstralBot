package dev.erdragh.astralbot.util

import java.awt.image.RenderedImage
import java.io.IOException
import javax.imageio.*
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.ImageOutputStream

/**
 * A simple utility for creating gifs.
 *
 * @author femmeromantic
 */
class GifWriter(
    os: ImageOutputStream?, imageType: Int,
    timeBetweenFramesMS: Int, loopContinuously: Boolean, transparent: Boolean
) : AutoCloseable {
    private val writer: ImageWriter =
        ImageIO.getImageWritersBySuffix("gif").next() ?: throw IOException("No GIF Image Writers Exist!")
    private val imageWriteParam: ImageWriteParam = writer.defaultWriteParam
    private val metadata: IIOMetadata = writer.getDefaultImageMetadata(
        ImageTypeSpecifier.createFromBufferedImageType(imageType),
        imageWriteParam
    )

    init {
        val root = metadata.getAsTree(metadata.nativeMetadataFormatName) as IIOMetadataNode
        setGifAttributes(root, timeBetweenFramesMS, transparent, loopContinuously)
        metadata.setFromTree(metadata.nativeMetadataFormatName, root)
        writer.output = os
        writer.prepareWriteSequence(null)
    }

    fun write(img: RenderedImage) {
        writer.writeToSequence(IIOImage(img, null, metadata), imageWriteParam)
    }

    override fun close() {
        writer.endWriteSequence()
    }

    private fun getOrCreate(root: IIOMetadataNode, name: String): IIOMetadataNode =
        (0 until root.length)
            .map { root.item(it) as IIOMetadataNode }
            .firstOrNull { it.nodeName == name }
            ?: IIOMetadataNode(name).also { root.appendChild(it) }

    private fun setGifAttributes(
        root: IIOMetadataNode, timeBetweenFramesMS: Int, transparent: Boolean, loopContinuously: Boolean
    ) {
        getOrCreate(root, "GraphicControlExtension").apply {
            setAttribute("disposalMethod", "restoreToBackgroundColor")
            setAttribute("userInputFlag", "FALSE")
            setAttribute("transparentColorFlag", if (transparent) "TRUE" else "FALSE")
            setAttribute("delayTime", (timeBetweenFramesMS / 10).toString())
            setAttribute("transparentColorIndex", "0")
        }
        getOrCreate(root, "CommentExtensions").setAttribute("CommentExtension", "Test Comment")

        val appEN = getOrCreate(root, "ApplicationExtensions")
        val loop = if (loopContinuously) 0 else 1
        IIOMetadataNode("ApplicationExtension").apply {
            setAttribute("applicationID", "NETSCAPE")
            setAttribute("authenticationCode", "2.0")
            userObject = byteArrayOf(0x1, loop.toByte(), 0)
            appEN.appendChild(this)
        }
    }
}