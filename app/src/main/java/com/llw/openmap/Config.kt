package com.llw.openmap

import android.util.Log
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex

object Config : TileSourceFactory() {

    const val MAP_KEY = "f951c7d1b85975379f6ee20bb264abba"

    // 默认GeoPoint
    val defaultGeoPoint = GeoPoint(39.909, 116.39742)

    /**
     * 天地图 有标注电子地图
     */
    var TDTCIA_W: OnlineTileSourceBase = object : XYTileSource(
        "Tian Di Tu CIA",
        0, 20, 256, "",
        arrayOf(
            "http://t0.tianditu.com/DataServer?T=cia_w&tk=$MAP_KEY",
            "http://t1.tianditu.com/DataServer?T=cia_w&tk=$MAP_KEY",
            "http://t2.tianditu.com/DataServer?T=cia_w&tk=$MAP_KEY",
            "http://t3.tianditu.com/DataServer?T=cia_w&tk=$MAP_KEY",
            "http://t4.tianditu.com/DataServer?T=cia_w&tk=$MAP_KEY",
            "http://t5.tianditu.com/DataServer?T=cia_w&tk=$MAP_KEY",
            "http://t6.tianditu.com/DataServer?T=cia_w&tk=$MAP_KEY",
            "http://t7.tianditu.com/DataServer?T=cia_w&tk=$MAP_KEY"
        )
    ) {
        override fun getTileURLString(pMapTileIndex: Long) =
            ("$baseUrl&X=${MapTileIndex.getX(pMapTileIndex)}&Y=${MapTileIndex.getY(pMapTileIndex)}&L=${MapTileIndex.getZoom(pMapTileIndex)}")
    }
}