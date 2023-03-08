package michaelbukachi.common

import org.koin.dsl.module

val commonModules = module {
    single { JobManager(get()) }
    single { DateUtils() }
    single { NotificationHelper(get()) }
}
