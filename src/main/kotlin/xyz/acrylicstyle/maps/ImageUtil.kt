package xyz.acrylicstyle.maps

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object ImageUtil {
    fun readFrames(): List<BufferedImage> {
        val list = ArrayList<BufferedImage>()
        val file = File(MapsPlugin.instance.dataFolder, "frames")
        file.mkdirs()
        file.listFiles { f -> f.extension == "bmp" }?.forEach { f ->
            try {
                list.add(ImageIO.read(f))
            } catch (e: Exception) {
                MapsPlugin.instance.logger.warning("Failed to process frame ${file.name}")
                e.printStackTrace()
            }
        }
        return list
    }
}
