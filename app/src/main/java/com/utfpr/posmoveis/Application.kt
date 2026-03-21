package com.utfpr.posmoveis
import android.app.Application
import com.utfpr.posmoveis.database.DatabaseBuilder

class Application : Application(){

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init(){
        DatabaseBuilder.getInstance(this)

    }
}