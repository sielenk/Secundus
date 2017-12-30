package de.masitec.secundus.fragments

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.masitec.secundus.R


class CameraFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        val manager = activity.applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (cameraId in manager.cameraIdList) {
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val keys = characteristics.keys

        }

        return inflater!!.inflate(R.layout.tab_camera, container, false)
    }
}
