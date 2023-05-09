package com.example.permissionsapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat



class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        const val PERMISSION_REQUEST_CAMERA = 0
    }
    private var firstRequest: Boolean = true
    private lateinit var sharedPreferences: SharedPreferences

    private val btnCamera: Button by lazy { findViewById(R.id.btnOpenCamera) }

    override fun onResume() {
        super.onResume()
        if(sharedPreferences.contains("APP_PREFERENCES_FIRST_REQUEST_KEY")){
             firstRequest = sharedPreferences.getBoolean("APP_PREFERENCES_FIRST_REQUEST_KEY", true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        btnCamera.setOnClickListener {
//            Перед выполнением операции, требующей разрешения, необходимо спросить у системы, есть ли у приложения разрешение на это.
//            Т.е. подтверждал ли пользователь, что он дает приложению это разрешение.
//            Если разрешение уже есть, то выполняем операцию. Если нет, то запрашиваем это разрешение у пользователя.
//            Он вернет константу PackageManager.PERMISSION_GRANTED (если разрешение есть)
//            или PackageManager.PERMISSION_DENIED (если разрешения нет).
            if (checkSelfPermissionCompat(Manifest.permission.CAMERA) ==
                //Если разрешение есть, значит мы ранее его уже запрашивали, и пользователь подтвердил его
                PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                //Если разрешения нет, то нам надо его запросить. Это выполняется методом requestPermissions.
                requestCameraPermission()
            }
        }
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.CAMERA)) { // пользователь отклонил разрешение ранее и флаг "никогда не спрашивать" не устанавливал
            requestPermissionsCompat(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        } else { // если пользователь установил флаг "никогда больше не спрашивать" либо впервые запрашивает доступ
            if(firstRequest){
                requestPermissionsCompat(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
                firstRequest = false
            }
            else{
                Toast.makeText(
                    this,
                    "Разрешение не может быть запрошено, перейдите в настройки",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //сохраняем в настройки флаг первого запроса доступа
        val editor = sharedPreferences.edit()
        editor.putBoolean("APP_PREFERENCES_FIRST_REQUEST_KEY", firstRequest).apply()
    }




    //Решение пользователя мы получим в методе onRequestPermissionsResult
    override fun onRequestPermissionsResult(
        requestCode: Int, //
        permissions: Array<out String>,  //  В массиве permissions придут название разрешений, которые мы запрашивали
        grantResults: IntArray //  В массиве grantResults придут ответы пользователя на запросы разрешений.
    ) {

        //Проверяем, что requestСode тот же, что мы указывали в requestPermissions
        if (requestCode == PERMISSION_REQUEST_CAMERA) {

            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение предоставлено", Toast.LENGTH_LONG).show()
                startCamera()
            } else {
                Toast.makeText(this, "Разрешение не предоставлено", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startCamera() {
        val intent = Intent(this, CameraPreviewActivity::class.java)
        startActivity(intent)
    }
}


//Метод  используется для проверки того, предоставлено ли определенное разрешение.
//Этот метод возвращает перечисление Android.Content.PM.Permission , которое имеет одно из двух значений:
//Permission.Granted — указанное разрешение было предоставлено.
//Permission.Denied — указанное разрешение не было предоставлено.
fun AppCompatActivity.checkSelfPermissionCompat(permission: String) =
    ActivityCompat.checkSelfPermission(this, permission)

//shouldShowRequestPermissionRationale() вернет false в 2-х случаях:
//Когда пользователь ранее отказывал в разрешении, и  установил флажок «Никогда не спрашивать снова».
//Когда пользователь запрашивает разрешение в первый раз.
//shouldShowRequestPermissionRationale()вернет true в случае:
//Когда пользователь отклонил разрешение ранее И флаг "никогда не спрашивать" не устанавливал
fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

//метод для отправки запроса на разрешение
fun AppCompatActivity.requestPermissionsCompat(
    permissionsArray: Array<String>,
    requestCode: Int
) {
    ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
}