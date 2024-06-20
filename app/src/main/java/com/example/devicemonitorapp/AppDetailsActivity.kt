package com.example.devicemonitorapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.devicemonitorapp.base.BaseActivity
import com.example.devicemonitorapp.databinding.ActivityAppDetailsBinding
import com.example.devicemonitorapp.models.App
import com.example.devicemonitorapp.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File

class AppDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityAppDetailsBinding
    private lateinit var packagesManager: PackagesManager

    private var sdSize: Long = 0
    private var internalSize: Long = 0
    private var appSize: Long = 0
    private lateinit var apkFile: File
    private var aPackageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        packagesManager = PackagesManager(this)

        val app = intent.getSerializableExtra("APP_DETAILS") as App?
        if (app != null) {
            aPackageName = app.packageName

            supportActionBar?.title = app.label
            binding.appDetailsIcon.setImageDrawable(packagesManager.getAppIcon(app.packageName))
            binding.appDetailsPackageName.text = app.packageName
            binding.appDetailsVersion.text = "v${app.versionName}"
            if (!app.isEnabled) binding.appDetailsDisable.setText(R.string.enable)

            lifecycleScope.launch(workerContext) {
                val supportsAppOps = RootUtils.executeSync("which appops").isNotEmpty()

                val pathString = Utils.runCommand(
                    "pm path ${aPackageName!!}",
                    getString(android.R.string.unknownName)
                ).replace("package:", "")

                apkFile = File(pathString)
                if (apkFile.exists()) {
                    appSize = runCatching {
                        RootUtils.executeWithOutput(
                            "du -s ${apkFile.parentFile.absolutePath} | awk '{print $1}'",
                            "0"
                        ).toLong()
                    }.getOrElse {
                        runCatching {
                            RootUtils.executeWithOutput(
                                "du -s ${apkFile.parentFile.absolutePath} | awk '{print $1}'",
                                "0"
                            ).toLong()
                        }.getOrDefault(0)
                    }
                }

                try {
                    sdSize = FileUtils.getFileSize(File("${Environment.getExternalStorageDirectory()}/Android/data/$aPackageName")) / 1024
                    internalSize = RootUtils.executeWithOutput("du -s /data/data/$aPackageName | awk '{print $1}'", "0").toLong()
                } catch (e: Exception) {
                    try {
                        sdSize = FileUtils.getFileSize(File("${Environment.getExternalStorageDirectory()}/Android/data/$aPackageName")) / 1024
                        internalSize = RootUtils.executeWithOutput("du -s /data/data/$aPackageName | awk '{print $1}'", "0").toLong()
                    } catch (ex: Exception) {
                        sdSize = 0
                        internalSize = 0
                    }
                }

                runSafeOnUiThread {
                    val finalSize = (sdSize + internalSize + appSize) / 1024
                    binding.appDetailsPathSum.text = pathString
                    binding.appDetailsStorageSum.text = "$finalSize MB"

                    binding.appDetailsProgress.visibility = View.GONE
                    binding.appDetailsDetailLayout.visibility = View.VISIBLE

                    if (!supportsAppOps) {
                        binding.appDetailsAppOps.visibility = View.GONE
                        Logger.logWarning("Appops is not supported", this@AppDetailsActivity)
                    }
                }
            }

            binding.appDetailsStorage.setOnClickListener {
                MaterialAlertDialogBuilder(this@AppDetailsActivity)
                    .setTitle(R.string.storage)
                    .setMessage("SD: ${if (sdSize >= 1024) "${(sdSize / 1024)}MB\n" else "${sdSize}KB\n"}Internal: ${if (internalSize >= 1024) "${(internalSize / 1024)}MB\n" else "${internalSize}KB\n"}App: ${appSize / 1024}MB")
                    .setPositiveButton(R.string.clear_data) { _, _ ->
                        runCommand("pm clear ${aPackageName!!}")
                        Logger.logInfo("Cleared data of the package: ${aPackageName!!}", this)
                    }
                    .setNegativeButton(R.string.close) { _, _ -> }
                    .applyAnim().also {
                        it.show()
                    }
            }

            binding.appDetailsPath.setOnClickListener {
                if (apkFile.isFile) {
                    val uri = Uri.parse(apkFile.toString())
                    val share = Intent(Intent.ACTION_SEND)
                    share.type = "application/octet-stream"
                    share.putExtra(Intent.EXTRA_STREAM, uri)
                    startActivity(Intent.createChooser(share, "Share APK File"))
                }
            }

            binding.appDetailsDisable.setOnClickListener {
                if (app.isEnabled) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.warning))
                        .setIcon(getThemedVectorDrawable(R.drawable.ic_warning))
                        .setMessage(getString(R.string.confirmation_message))
                        .setNegativeButton(R.string.cancelar) { _, _ -> }
                        .setPositiveButton(R.string.disable) { _, _ ->
                            runCommand("pm disable ${aPackageName!!}")
                            Logger.logInfo("Disabled package: ${aPackageName!!}", this)
                            Snackbar.make(binding.appDetailsDisable, R.string.package_disabled, Snackbar.LENGTH_LONG).show()
                            binding.appDetailsDisable.setText(R.string.enable)
                        }.applyAnim().also {
                            it.show()
                        }
                } else {
                    runCommand("pm enable ${aPackageName!!}")
                    Logger.logInfo("Enabled package: ${aPackageName!!}", this)
                    binding.appDetailsDisable.setText(R.string.disable)
                }
            }

            binding.appDetailsUninstall.setOnClickListener {
                Logger.logWarning("Attempting to uninstall package: ${aPackageName!!}", this)
                if (app.isSystemApp) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.warning))
                        .setIcon(getThemedVectorDrawable(R.drawable.ic_warning))
                        .setMessage(getString(R.string.confirmation_message))
                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                        .setPositiveButton(R.string.uninstall) { _, _ ->
                            lifecycleScope.launch(workerContext) {
                                val packagePathString = RootUtils.executeWithOutput("pm path ${aPackageName!!}", "", this@AppDetailsActivity).substring(8)
                                val packagePath = File(packagePathString)
                                if (packagePath.isFile) {
                                    RootUtils.deleteFileOrDir(packagePathString)
                                    RootUtils.deleteFileOrDir("${Environment.getDataDirectory()}/data/$aPackageName")

                                    Logger.logInfo("Deleted package: ${aPackageName!!}", this@AppDetailsActivity)
                                    runSafeOnUiThread {
                                        MaterialAlertDialogBuilder(this@AppDetailsActivity)
                                            .setTitle(R.string.package_uninstalled)
                                            .setMessage("Reboot your device")
                                            .setPositiveButton(android.R.string.ok) { _, _ -> }
                                            .setNeutralButton(R.string.reboot) { _, _ -> runCommand("reboot") }
                                            .applyAnim().also {
                                                it.show()
                                            }
                                    }
                                } else {
                                    runSafeOnUiThread {
                                        Snackbar.make(binding.appDetailsUninstall, "${getString(R.string.error)}: $packagePath does not exist", Snackbar.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        .applyAnim().also {
                            it.show()
                        }
                } else {
                    try {
                        val packageURI = Uri.parse("package:${aPackageName!!}")
                        val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageURI)
                        startActivity(uninstallIntent)
                    } catch (e: Exception) {
                        Toast.makeText(this@AppDetailsActivity, "Could not launch uninstall dialog for package: $aPackageName. Reason: ${e.message}", Toast.LENGTH_LONG).show()
                        Logger.logError("Could not launch uninstall dialog for package: $aPackageName. Reason: ${e.message}", this@AppDetailsActivity)
                    }
                }
            }

        } else {
            Logger.logWTF("Failed to show app details because no app was provided to begin with", this)
            Toast.makeText(this, R.string.failed, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
