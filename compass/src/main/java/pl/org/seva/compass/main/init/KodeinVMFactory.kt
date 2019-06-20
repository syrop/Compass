package pl.org.seva.compass.main.init

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.Kodein
import org.kodein.di.TT
import org.kodein.di.conf.global
import org.kodein.di.direct

object KodeinVMFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
            Kodein.global.direct.Instance(TT(modelClass))
}
