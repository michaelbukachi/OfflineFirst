package michaelbukachi.sync

import michaelbukachi.common.NotificationHelper
import org.koin.dsl.module

val syncModules = module {
    single { SyncMetricsManager(get(), get(), get()) }
    single { NotificationHelper(get()) }
}
