package com.androidvip.hebf.ui.base.binding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

typealias FragmentInflater<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

