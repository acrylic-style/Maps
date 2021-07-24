package xyz.acrylicstyle.maps

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.plugin.java.JavaPlugin
import java.awt.image.BufferedImage
import java.lang.NumberFormatException
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class MapsPlugin: JavaPlugin() {
    companion object {
        lateinit var instance: MapsPlugin
        val frames = ArrayList<BufferedImage>()
    }

    init {
        instance = this
    }

    override fun onEnable() {
        getCommand("mload")?.setExecutor { sender, _, _, _ ->
            if (sender !is Player) return@setExecutor true
            Thread {
                sender.sendMessage("${ChatColor.GREEN}Loading frames...")
                frames.clear()
                frames.addAll(ImageUtil.readFrames())
                sender.sendMessage("${ChatColor.GREEN}Loaded ${frames.size} frames")
            }.start()
            return@setExecutor true
        }
        getCommand("mstart")?.setExecutor { sender, _, _, args ->
            if (sender !is Player) return@setExecutor true
            if (frames.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}Frames aren't loaded. Please do /mload to load them.")
                return@setExecutor true
            }
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}/mstart <fps: 1-120>")
                return@setExecutor true
            }
            val fps = try {
                Integer.parseInt(args[0])
            } catch (_: NumberFormatException) {
                sender.sendMessage("${ChatColor.RED}/mstart <fps: 1-120>")
                return@setExecutor true
            }
            if (fps <= 0 || fps > 120) {
                sender.sendMessage("${ChatColor.RED}/mstart <fps: 1-120>")
                return@setExecutor true
            }
            MapsRenderer.currentFrame = frames[0]
            val mapView = Bukkit.createMap(sender.world)
            mapView.renderers.clear()
            mapView.scale = MapView.Scale.FARTHEST
            mapView.addRenderer(MapsRenderer)
            val stack = ItemStack(Material.FILLED_MAP)
            stack.itemMeta?.let {
                (it as MapMeta).mapView = mapView
                stack.itemMeta = it
            }
            sender.inventory.addItem(stack)
            Thread {
                Thread.sleep(5000)
                var currentFrame = 0
                val obj = Object()
                while (currentFrame < frames.size) {
                    val start = System.nanoTime()
                    val frame = frames[currentFrame++]
                    MapsRenderer.currentFrame = frame
                    val end = System.nanoTime()
                    val ims = floor((1000.toDouble() / fps)).toLong()
                    val the666666 = (((1000.toDouble() / fps) - ims) * 1000000).toInt()
                    var dms = ims - (end - start) / 1000000
                    var dns = the666666 - (end - start).toInt() % 1000000
                    if (dns < 0) {
                        dns += 1000000
                        dms--
                    }
                    if (dms >= 0 || dns >= 0) {
                        synchronized(obj) {
                            obj.wait(max(0, min(ims, dms)), dns)
                        }
                    }
                }
            }.start()
            return@setExecutor true
        }
    }
}
