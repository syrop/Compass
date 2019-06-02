package pl.org.seva.compass.main

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import pl.org.seva.compass.main.init.bootstrap
import pl.org.seva.compass.main.init.module

@Suppress("unused")
class CompassApplication : Application() {

    init { Kodein.global.addImport(module) }

    override fun onCreate() {
        super.onCreate()
        bootstrap.boot()
    }
}
