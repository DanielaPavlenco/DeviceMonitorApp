package com.example.devicemonitorapp.models

import com.example.devicemonitorapp.models.App

class OpApp(var op: String?, app: App) : App() {
    var isOpEnabled: Boolean = false

    init {
        this.icon = app.icon
        this.label = app.label
        this.packageName = app.packageName
    }
}
