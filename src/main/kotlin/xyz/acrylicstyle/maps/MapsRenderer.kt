package xyz.acrylicstyle.maps

import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.image.BufferedImage

object MapsRenderer: MapRenderer() {
    lateinit var currentFrame: BufferedImage

    override fun render(mapView: MapView, mapCanvas: MapCanvas, player: Player) {
        mapCanvas.drawImage(0, 0, MapPalette.resizeImage(currentFrame))
    }
}
