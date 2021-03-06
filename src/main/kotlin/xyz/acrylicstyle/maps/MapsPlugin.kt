package xyz.acrylicstyle.maps

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapPalette
import org.bukkit.map.MapView
import org.bukkit.plugin.java.JavaPlugin
import java.awt.image.BufferedImage
import java.lang.NumberFormatException
import java.util.Timer
import java.util.TimerTask
import kotlin.math.floor

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
                val list = ImageUtil.readFrames()
                sender.sendMessage("${ChatColor.GREEN}Processing ${list.size} frames")
                frames.addAll(list.map { MapPalette.resizeImage(it) })
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
            var currentFrame = 0
            var actualFps = 0
            val timer = Timer()
            Thread {
                Thread.sleep(5000)
                timer.scheduleAtFixedRate(object: TimerTask() {
                    override fun run() {
                        val color = if (actualFps != fps) ChatColor.GOLD else ChatColor.GREEN
                        sender.sendMessage("${color}Processed $actualFps frames in last second")
                        currentFrame += fps - actualFps
                        actualFps = 0
                    }
                }, 1000, 1000)
                val obj = Object()
                val ims = floor((1000.toDouble() / (fps + 1))).toLong()
                val the666666 = (((1000.toDouble() / (fps + 1)) - ims) * 1000000).toInt()
                while (currentFrame < frames.size) {
                    sender.sendMessage("$currentFrame")
                    MapsRenderer.currentFrame = frames[currentFrame++]
                    actualFps++
                    synchronized(obj) {
                        obj.wait(ims, the666666)
                    }
                }
                timer.cancel()
            }.start()
            return@setExecutor true
        }
    }
}
