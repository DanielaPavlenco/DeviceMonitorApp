package com.example.devicemonitorapp.utils

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.devicemonitorapp.R
import com.example.devicemonitorapp.databinding.ContextBottomSheetBinding
import com.example.devicemonitorapp.show
import com.example.devicemonitorapp.toPx
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.Serializable

typealias SheetOption = ContextBottomSheet.Option

class ContextBottomSheet : BottomSheetDialogFragment() {
    private var _binding: ContextBottomSheetBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(title: String? = "", options: ArrayList<Option>): ContextBottomSheet = ContextBottomSheet().apply {
            arguments = Bundle().apply {
                putString("title", title)
                putSerializable("options", options)
            }
        }
    }

    interface OnOptionClickListener {
        fun onOptionClick(tag: String)
    }

    data class Option(var title: String, val tag: String, @DrawableRes var icon: Int?) : Serializable

    var onOptionClickListener: OnOptionClickListener = object : OnOptionClickListener {
        override fun onOptionClick(tag: String) { dismiss() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ContextBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val title = it.getString("title", "")
            if (title.isNotEmpty()) {
                binding.contextBottomSheetTitle.show()
                binding.contextBottomSheetTitle.text = title
            }

            val options = it.getSerializable("options") as ArrayList<*>?
            options?.forEach { option ->
                (option as Option?)?.let { opt ->
                    binding.contextBottomSheetOptionsContainer.addView(generateOptionView(opt))
                }
            }
        }
    }

    private fun generateOptionView(option: Option): LinearLayout {
        val textColor = TypedValue()
        val backgroundDrawable = TypedValue()
        context?.theme?.resolveAttribute(R.attr.colorOnSurface, textColor, true)
        context?.theme?.resolveAttribute(R.attr.selectableItemBackground, backgroundDrawable, true)

        val rootLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                48.toPx(context)
            )
            gravity = Gravity.CENTER_VERTICAL
            tag = option.tag
            setPadding(16.toPx(context), 0, 16.toPx(context), 0)
            setBackgroundResource(backgroundDrawable.resourceId)
            setOnClickListener {
                onOptionClickListener.onOptionClick(option.tag)
            }
        }

        option.icon?.let {
            rootLayout.addView(ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(24.toPx(context), 24.toPx(context)).apply {
                    setMargins(0, 0, 16.toPx(context), 0)
                }
                setImageResource(it)

                if (option.tag == "uninstall") {
                    setColorFilter(ContextCompat.getColor(context, R.color.colorError), PorterDuff.Mode.SRC_IN)
                } else {
                    setColorFilter(ContextCompat.getColor(context, textColor.resourceId), PorterDuff.Mode.SRC_IN)
                }
            })
        }

        rootLayout.addView(TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
            text = option.title
            if (option.tag == "uninstall") {
                setTextColor(ContextCompat.getColor(context, R.color.colorError))
            } else {
                setTextColor(ContextCompat.getColor(context, textColor.resourceId))
            }
        })

        return rootLayout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
