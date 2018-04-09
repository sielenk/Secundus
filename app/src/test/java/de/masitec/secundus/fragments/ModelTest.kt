package de.masitec.secundus.fragments

import junit.framework.Assert
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.linear.ArrayRealVector
import org.junit.Test

class ModelTest {
    val observations = arrayOf(
            Vector3D(0.0, 0.0, 0.0),
            Vector3D(2.0, 0.0, 0.0),
            Vector3D(1.0, 1.0, 0.0),
            Vector3D(1.0, -1.0, 0.0),
            Vector3D(1.0, 0.0, 1.0),
            Vector3D(1.0, 0.0, -1.0))

    @Test
    fun testValue() {
        val model = Model(observations)
        val parameter = ArrayRealVector(doubleArrayOf(1.0, 0.0, 0.0), false)


        val value = model.value(parameter)


        Assert.assertEquals(value.first, ArrayRealVector(6, 0.0))
    }
}
