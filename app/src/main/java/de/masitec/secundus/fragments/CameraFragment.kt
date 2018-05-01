package de.masitec.secundus.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import de.masitec.secundus.R
import java.util.*


class CameraFragment : Fragment() {
    companion object {
        val fragmentPermissions = arrayOf(
                Manifest.permission.CAMERA
        )
    }

    private var surfaceView: SurfaceView? = null
    private var camera: CameraDevice? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surfaceView = view.findViewById(R.id.camera_surface_view)
    }

    override fun onResume() {
        super.onResume()
        openCamera(cameraDeviceCallback)
    }

    override fun onPause() {
        closeCamera()
        super.onPause()
    }


    @SuppressLint("MissingPermission")
    private fun openCamera(stateCallback: CameraDevice.StateCallback) {
        val activity = activity ?: return

        requestPermissions(activity) {
            val cameraManager = activity.getSystemService(CameraManager::class.java)
            val cameraId = cameraManager.cameraIdList.firstOrNull {
                val characteristics = cameraManager.getCameraCharacteristics(it)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

                facing == CameraCharacteristics.LENS_FACING_BACK
            }

            if (cameraId != null) {
                cameraManager.openCamera(cameraId, stateCallback, null)
            }
        }
    }

    val cameraDeviceCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            val surfaceView = surfaceView

            if (surfaceView != null) {
                camera.createCaptureSession(listOf(surfaceView.holder.surface), cameraSessionCallback, null)
            }

            this@CameraFragment.camera = camera
        }

        override fun onClosed(camera: CameraDevice?) {
            this@CameraFragment.camera = null

            super.onClosed(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
        }

        override fun onError(camera: CameraDevice, error: Int) {
        }
    }

    val cameraSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
        }

        override fun onConfigured(session: CameraCaptureSession) {
            val surfaceView = surfaceView

            if (surfaceView != null) {
                val requestBuilder = session.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                requestBuilder.addTarget(surfaceView.holder.surface)
                val request = requestBuilder.build()
                session.setRepeatingRequest(request, null, null)
            }
        }
    }

    private fun closeCamera() {
    }


    val random = Random()
    val pendingThunks: MutableMap<Int, () -> Unit> = HashMap()

    private fun requestPermissions(activity: FragmentActivity, onGranted: () -> Unit) {
        val missingPermissions = fragmentPermissions.filter {
            activity.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        val requiresFurtherPermissions = missingPermissions.isEmpty()

        if (!requiresFurtherPermissions) {
            val requestCode = synchronized(pendingThunks) {
                random.ints()
                        .filter { !pendingThunks.containsKey(it) }
                        .findFirst()
                        .asInt
            }

            pendingThunks[requestCode] = onGranted
            requestPermissions(missingPermissions, requestCode)
        } else {
            onGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            pendingThunks.remove(requestCode)?.invoke()
        } else {
            val missingPermissions = grantResults.zip(permissions)
                    .filter { it.first != PackageManager.PERMISSION_GRANTED }

            // @ToDo: message
        }
    }
}
