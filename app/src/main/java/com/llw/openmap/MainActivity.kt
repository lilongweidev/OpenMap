package com.llw.openmap

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.llw.openmap.databinding.ActivityMainBinding
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController.Visibility
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MinimapOverlay
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

class MainActivity : AppCompatActivity() {

    // 定位权限
    private val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    // 权限申请
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (!result.containsValue(false)) initMap() else requestPermission()
        }

    private lateinit var binding: ActivityMainBinding

    private val TAG = "MainActivity"
    // 是否定位
    private var isLocation = false
    // 定位管理器
    private lateinit var locationManager: LocationManager
    // 标记
    private var mMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 创建位置管理器实例
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onResume() {
        super.onResume()
        // 检查权限
        checkPermission()
    }

    /**
     * 初始化地图
     */
    private fun initMap() {
        binding.mapView.apply {
            setTileSource(Config.TDTCIA_W)  // 设置瓦片地图资源
            minZoomLevel = 5.0              // 最小缩放级别
            maxZoomLevel = 20.0             // 最大缩放级别
            isTilesScaledToDpi = true       // 图块是否缩放到 DPI
            // 设置默认的地图中心点
            controller.apply {
                setZoom(12.0)
                setCenter(Config.defaultGeoPoint)
            }
            // 缩放控件显示-淡入淡出
            zoomController.setVisibility(Visibility.SHOW_AND_FADEOUT)
            this.isAnimating
            setMultiTouchControls(true)
            // 覆盖管理器配置
            overlayManager.apply {
                tilesOverlay.isEnabled = true
                add(object : Overlay() {
                    override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                        // 获取投影对象后进行坐标转换再切换地图中心位置
                        mapView?.projection?.let { proj ->
                            val geoPoint = proj.fromPixels(e!!.x.toInt(), e.y.toInt()) as GeoPoint
                            Log.d(TAG, "onSingleTapConfirmed: 切换地图中心位置")
                            changeMapCenter(geoPoint)
                        }
                        return super.onSingleTapConfirmed(e, mapView)
                    }
                })
                // 添加比例尺
                add(ScaleBarOverlay(binding.mapView).apply {
                    setAlignBottom(true) // 底部对齐
                    setScaleBarOffset(100, 10) // 设置偏移量
                })
                // 添加指南针
                add(CompassOverlay(this@MainActivity, binding.mapView).apply {
                    enableCompass()
                })
                // 添加经纬度网格线
//                add(LatLonGridlineOverlay2())
                // 启用旋转手势
                add(RotationGestureOverlay(binding.mapView).apply { isEnabled = true })
                // 添加小地图
                add(MinimapOverlay(this@MainActivity, binding.mapView.tileRequestCompleteHandler).apply {
                    val dm = resources.displayMetrics
                    width = dm.widthPixels / 4
                    height = dm.heightPixels / 4
                    // 设置小地图资源
                    setTileSource(Config.TDTCIA_W)
                })
            }
        }
        // 开始定位
        startLocation()
    }

    /**
     * 检查权限
     */
    private fun checkPermission() {
        permissions.forEach { permission ->
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                // 请求权限
                requestPermission()
                return
            }
        }
        // 初始化地图
        initMap()
    }

    /**
     * 请求权限
     */
    private fun requestPermission() {
        permissionLauncher.launch(permissions)
    }

    /**
     * 定位监听
     */
    private val locationListener = LocationListener { location ->
        // 更新位置
        val latitude = location.latitude
        val longitude = location.longitude
        // 更新地图中心点
        changeMapCenter(GeoPoint(latitude, longitude))
        // 停止定位
        stopLocation()
    }

    /**
     * 开始定位
     */
    @SuppressLint("MissingPermission")
    private fun startLocation() {
        if (!isLocation){
            // 注册位置监听器
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            isLocation = !isLocation
        }
    }

    /**
     * 停止定位
     */
    private fun stopLocation() {
        if (isLocation) {
            // 停止位置更新
            locationManager.removeUpdates(locationListener)
            isLocation = !isLocation
        }
    }

    /**
     * 修改地图中心点
     */
    private fun changeMapCenter(geoPoint: GeoPoint) {
        Log.d(TAG, "changeMapCenter: $geoPoint")
        binding.mapView.apply {
            // 移除标点
            if (mMarker != null) {
                overlays.remove(mMarker)
            }
            mMarker = Marker(this).apply {
                title = "Marker"
                position = geoPoint
                icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_marker)
            }
            // 添加标点
            overlays.add(mMarker)

            controller.apply {
                setZoom(14.0)
                setCenter(geoPoint)
            }
        }
    }
}