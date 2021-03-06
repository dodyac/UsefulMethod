package com.acxdev.commonFunction.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.acxdev.commonFunction.common.*
import com.acxdev.commonFunction.common.Toast
import com.acxdev.commonFunction.util.Preference.Companion.readPrefsBoolean
import com.acxdev.commonFunction.util.Toast.Companion.toasty
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.play.core.review.ReviewManagerFactory
import com.sanojpunchihewa.updatemanager.UpdateManager
import com.sanojpunchihewa.updatemanager.UpdateManagerConstant
import com.thefinestartist.finestwebview.FinestWebView
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*

class FunctionX {
    @Suppress("DEPRECATION")
    companion object {

        const val gone: Int = View.GONE
        const val visible: Int = View.VISIBLE
        const val invisible: Int = View.INVISIBLE

        fun Context.getWidth(percent: Int): Int {
            val displayMetrics = DisplayMetrics()
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return (displayMetrics.widthPixels / 100) * percent
        }

        fun Context.isNetworkAvailable(): Boolean {
            var result = false
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.activeNetwork ?: return false
                val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                result = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } else {
                connectivityManager.run {
                    connectivityManager.activeNetworkInfo?.run {
                        result = when (type) {
                            ConnectivityManager.TYPE_WIFI -> true
                            ConnectivityManager.TYPE_MOBILE -> true
                            ConnectivityManager.TYPE_ETHERNET -> true
                            else -> false
                        }
                    }
                }
            }
            return result
        }

        fun Activity.webView(url: String, @ColorRes color: Int) {
            FinestWebView.Builder(this).toolbarColorRes(color).swipeRefreshColorRes(color).show(url)
        }

        fun Activity.webView(@StringRes url: Int, @ColorRes color: Int) {
            FinestWebView.Builder(this).toolbarColorRes(color).swipeRefreshColorRes(color).show(getString(url))
        }

        fun Context.openPDFDocument(filename: String) {
            val pdfIntent = Intent(Intent.ACTION_VIEW)
            pdfIntent.setDataAndType(Uri.parse(filename), "application/pdf")
            pdfIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(Intent.createChooser(pdfIntent, "Open PDF"))
        }

        fun Context.cropError(data: Intent?) {
            val cropError = data?.let { UCrop.getError(it) }
            if (cropError != null) toasty(Toast.ERROR, cropError.message!!)
        }

        fun Activity.startCrop(uri: Uri?) {
            val destination = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()
            UCrop.of(uri!!, Uri.fromFile(File(cacheDir, destination))).withAspectRatio(1f, 1f)
                .withMaxResultSize(512, 512).start(this)
        }

        fun Fragment.startCrop(uri: Uri?) {
            val destination = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()
            UCrop.of(uri!!, Uri.fromFile(File(context?.cacheDir, destination))).withAspectRatio(1f, 1f)
                .withMaxResultSize(512, 512).start(context!!, this)
        }

        fun Context.getCompatActivity(): AppCompatActivity? {
            return when (this) {
                is AppCompatActivity -> this
                is ContextWrapper -> baseContext.getCompatActivity()
                else -> null
            }
        }

        fun Fragment.putExtra(bundle: String, secondData: String? = null, secondBundle: String? = null, thirdData: String? = null, thirdBundle: String? = null): Fragment {
            val args = Bundle()
            args.putString("data", bundle)
            args.putString(secondData, secondBundle)
            args.putString(thirdData, thirdBundle)
            arguments = args
            return this
        }

        fun Context.showSheetWithExtra(bottomSheet: BottomSheetDialogFragment, bundle: String? = null) {
            val args = Bundle()
            args.putString("data", bundle)
            bottomSheet.arguments = args
            bottomSheet.show((this as FragmentActivity).supportFragmentManager, bottomSheet.tag)
        }

        fun setThreadPolicy(){
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        fun AppCompatActivity.checkUpdateImmediately(){
            val updateManager = UpdateManager.Builder(this).mode(UpdateManagerConstant.IMMEDIATE)
            updateManager.start()
        }

        fun Context.showRate(){
            val manager = ReviewManagerFactory.create(this)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener {
                if (it.isSuccessful) {
                    val reviewInfo = it.result
                    val flow = manager.launchReviewFlow(this as Activity, reviewInfo)
                    flow.addOnCompleteListener {
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }
                } else {
                    // There was some problem, continue regardless of the result.
                    // you can show your own rate dialog alert and redirect user to your app page
                    // on play store.
                }
            }
        }

        fun AppCompatActivity.lockSize(configuration: Configuration?, smallestWidth: Int) {
            if (configuration != null) {
                Log.d("TAG", "adjustDisplayScale: " + configuration.densityDpi)
                configuration.densityDpi = smallestWidth
                val metrics = resources.displayMetrics
                val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.defaultDisplay.getMetrics(metrics)
                metrics.scaledDensity = configuration.densityDpi * metrics.density
                resources.updateConfiguration(configuration, metrics)
            }
        }

        fun Context.openSettings(applicationID: String) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", applicationID, null)
            intent.data = uri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        fun Context.useCurrentTheme(){
            AppCompatDelegate.setDefaultNightMode(if(readPrefsBoolean(ConstantX.DARK_MODE))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }

        fun Context.getScreenResolution(): Int {
            val wm = getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            println("Screen Resolution- width: $width, height: $height")
            return width
        }

        fun AppCompatActivity.scaleScreen(configuration: Configuration, sizeHD: Int, sizeFHD: Int){
            Log.d("TAG", "adjustDisplayScaleBefore: ${configuration.densityDpi}")
            if(getScreenResolution() >= 1080) lockSize(configuration, sizeFHD)
            else lockSize(configuration, sizeHD)
        }
    }
}
