package com.zakrywilson.systemctl.service.generator.models

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Named
import com.zakrywilson.systemctl.service.generator.AppConf

/**
 * @author Zach Wilson
 */
class Module : AbstractModule() {

    override fun configure() {}

    @Provides
    @Named("appHomeDir")
    fun provideAppHomeDir(conf: AppConf) : String {
        return conf.appHomeDir
    }

    @Provides
    @Named("deliveryDir")
    fun providedDeliveryDir(conf: AppConf) : String {
        return conf.deliveryDir
    }

}