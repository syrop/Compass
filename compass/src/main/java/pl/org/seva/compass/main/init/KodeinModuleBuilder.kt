package pl.org.seva.compass.main.init

import android.content.Context
import android.location.Geocoder
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.multiton
import org.kodein.di.generic.singleton
import pl.org.seva.compass.BuildConfig
import pl.org.seva.compass.location.LocationChannelFactory
import pl.org.seva.compass.main.Permissions
import pl.org.seva.compass.rotation.RotationChannelFactory
import java.util.*
import java.util.logging.Filter
import java.util.logging.Logger

val Context.module get() = KodeinModuleBuilder(this).build()

inline fun <reified T : Any> instance() = Kodein.global.instance<T>()

inline fun <reified A, reified T : Any> instance(arg: A) = Kodein.global.instance<A, T>(arg = arg)

class KodeinModuleBuilder(private val ctx: Context) {

    fun build() = Kodein.Module("main") {
        bind<Bootstrap>() with singleton { Bootstrap() }
        bind<Logger>() with multiton { tag: String ->
            Logger.getLogger(tag)!!.apply {
                if (!BuildConfig.DEBUG) {
                    filter = Filter { false }
                }
            }
        }
        bind<Geocoder>() with singleton { Geocoder(ctx, Locale.getDefault()) }
        bind<Permissions>() with singleton { Permissions() }
        bind<LocationChannelFactory>() with singleton { LocationChannelFactory(ctx) }
        bind<RotationChannelFactory>() with singleton { RotationChannelFactory(ctx) }
    }
}
