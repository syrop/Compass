/*
 * Copyright (C) 2019 Wiktor Nizio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you like this program, consider donating bitcoin: bc1qncxh5xs6erq6w4qz3a7xl7f50agrgn3w58dsfp
 */

package pl.org.seva.compass.main.init

import android.content.Context
import android.location.Geocoder
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.*
import pl.org.seva.compass.compass.CompassViewModel
import pl.org.seva.compass.location.LocationChannelFactory
import pl.org.seva.compass.main.Permissions
import pl.org.seva.compass.rotation.RotationChannelFactory
import java.util.*

val Context.module get() = KodeinModuleBuilder(this).build()

inline fun <reified T : Any> instance() = Kodein.global.instance<T>()

class KodeinModuleBuilder(private val ctx: Context) {

    fun build() = Kodein.Module("main") {
        bind<Bootstrap>() with singleton { Bootstrap() }
        bind<Geocoder>() with singleton { Geocoder(ctx, Locale.getDefault()) }
        bind<Permissions>() with singleton { Permissions() }
        bind<LocationChannelFactory>() with singleton { LocationChannelFactory(ctx) }
        bind<RotationChannelFactory>() with singleton { RotationChannelFactory(ctx) }
        bind<CompassViewModel>() with provider { CompassViewModel(instance(), instance()) }
    }
}
