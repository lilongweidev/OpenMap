package com.llw.openmap

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.llw.openmap.databinding.ActivityMainBinding
import org.osmdroid.views.CustomZoomButtonsController.Visibility

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
            zoomController.setVisibility(Visibility.NEVER)
            setMultiTouchControls(true)
            overlayManager.tilesOverlay.isEnabled = true
        }
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
}