package de.masitec.secundus.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import de.masitec.secundus.R


class SmellButtonFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_smell_button, container, false)
        val button = view.findViewById<Button>(R.id.smell_button)

        button.setOnClickListener { v ->
            v.toString()
        }

        return view
    }
}
