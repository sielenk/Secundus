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
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import de.masitec.secundus.R
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import java.util.*


class CameraFragment : Fragment() {
    companion object {
        private const val FRAGMENT_PERMISSION_REQUEST_ID = 14508
        private const val QUEUE_SIZE = 10

        private val fragmentPermissions = arrayOf(
                Manifest.permission.CAMERA
        )
    }

    private val timer = Timer("arCore update trigger")
    private val positionQueue: Queue<Anchor> = ArrayDeque()
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

        val session = session
        if (session != null) {
            session.resume()
            timer.schedule(object : TimerTask() {
                override fun run() = onTimerTick(session)
            }, 0, 100)
        }
    }

    override fun onPause() {
        this.session?.pause()
        timer.cancel()

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
            requestPermissions(missingPermissions, FRAGMENT_PERMISSION_REQUEST_ID)
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
        this.session = null
    }

    private fun onTimerTick(session: Session) {
        val frame = session.update()
        val cameraAnchor = session.createAnchor(frame.camera.pose)

        if (positionQueue.size == QUEUE_SIZE) {
            positionQueue.remove().detach()
        }

        positionQueue.offer(cameraAnchor)

        if (positionQueue.size == QUEUE_SIZE) {
            val positions = positionQueue
                    .map { anchor ->
                        anchor.pose.let {
                            Vector3D(
                                    it.tx().toDouble(),
                                    it.ty().toDouble(),
                                    it.tz().toDouble())
                        }
                    }.toTypedArray()

            val target = positions
                    .flatMap { point -> listOf(point.x, point.y, point.z) }
                    .toDoubleArray()

            val foo = LeastSquaresFactory.create(
                    Model(positions),
                    ArrayRealVector(target, false),
                    ArrayRealVector(doubleArrayOf(0.0, 0.0, 0.0, 0.0), false),
                    null,
                    1000,
                    1000)

            val optimizer: LeastSquaresOptimizer = LevenbergMarquardtOptimizer()
                    .withCostRelativeTolerance(1.0e-12)
                    .withParameterRelativeTolerance(1.0e-12)

            val optimum = optimizer.optimize(foo)

            optimum.point
        }
    }
}

data class RadiusVector(val vector: Vector3D) {
    val length = vector.norm
    val direction = (1 / length) * vector
}

data class Model(val observations: Array<Vector3D>) : MultivariateJacobianFunction {
    override fun value(parameter: RealVector): org.apache.commons.math3.util.Pair<RealVector, RealMatrix> {
        val n = observations.size
        val oneOverN = 1.0 / n

        val center = Vector3D(
                parameter.getEntry(0),
                parameter.getEntry(1),
                parameter.getEntry(2))

        val radiusVectors = Array(n) { RadiusVector(observations[it] - center) }

        val radius = oneOverN * radiusVectors.fold(0.0) { acc, v -> acc + v.length }
        val residuals = DoubleArray(n) { radiusVectors[it].length - radius }

        val offset = oneOverN * radiusVectors.fold(Vector3D.ZERO) { acc, v -> acc + v.direction }
        val jacobian = Array(n) { (radiusVectors[it].direction - offset).negate().toArray() }

        return org.apache.commons.math3.util.Pair(
                ArrayRealVector(residuals, false),
                Array2DRowRealMatrix(jacobian, false))
    }
}

operator fun Vector3D.plus(other: Vector3D): Vector3D = this.add(other)
operator fun Vector3D.minus(other: Vector3D): Vector3D = this.subtract(other)
operator fun Double.times(v: Vector3D): Vector3D = v.scalarMultiply(this)
