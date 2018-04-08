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
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import de.masitec.secundus.R


class CameraFragment : Fragment() {
    companion object {
        val fragmentPermissionRequestId = 14508
        val fragmentPermissions = arrayOf(
                Manifest.permission.CAMERA
        )
    }

    private var surfaceView: SurfaceView? = null
    private var session: Session? = null
    private var userRequestedInstall = true

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
        openCamera()
        openSession()
    }

    override fun onPause() {
        closeSession()
        closeCamera()
        super.onPause()
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        val activity = activity

        if (activity != null && requestPermissions(activity)) {
            val cameraManager = activity.getSystemService(CameraManager::class.java)
            val cameraId = cameraManager.cameraIdList.firstOrNull {
                val characteristics = cameraManager.getCameraCharacteristics(it)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

                facing == CameraCharacteristics.LENS_FACING_BACK
            }

            if (cameraId != null) {
                cameraManager.openCamera(cameraId, cameraDeviceCallback, null)
            }
        }
    }

    val cameraDeviceCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice?) {
            val surfaceView = surfaceView

            if (camera != null && surfaceView != null) {
                camera.createCaptureSession(listOf(surfaceView.holder.surface), cameraSessionCallback, null)
            }
        }

        override fun onDisconnected(camera: CameraDevice?) {
        }

        override fun onError(camera: CameraDevice?, error: Int) {
        }
    }

    val cameraSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession?) {
        }

        override fun onConfigured(session: CameraCaptureSession?) {
            val surfaceView = surfaceView
            if (session != null && surfaceView != null) {
                val requestBuilder = session.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                requestBuilder.addTarget(surfaceView.holder.surface)
                val request = requestBuilder.build()
                session.setRepeatingRequest(request, null, null)
            }
        }
    }

    private fun closeCamera() {
    }


    private fun requestPermissions(activity: FragmentActivity): Boolean {
        val missingPermissions = fragmentPermissions.filter {
            activity.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        val requiresFurtherPermissions = missingPermissions.isEmpty()

        if (!requiresFurtherPermissions) {
            requestPermissions(missingPermissions, fragmentPermissionRequestId)
        }

        return requiresFurtherPermissions
    }


    private fun openSession() {
        if (session == null) {
            val requestResult =
                    try {
                        ArCoreApk.getInstance().requestInstall(activity, userRequestedInstall)
                    } catch (e: UnavailableDeviceNotCompatibleException) {
                    } catch (e: UnavailableUserDeclinedInstallationException) {
                    }

            when (requestResult) {
                ArCoreApk.InstallStatus.INSTALLED -> session = Session(activity)
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> userRequestedInstall = false
            }
        }
    }

    private fun closeSession() {
        val sessionBak = session

        session = null

        if (sessionBak != null) {
            // ... close session ...
        }
    }
}
