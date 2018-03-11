package com.zakrywilson.systemctl.service.generator

import com.hubspot.dropwizard.guice.GuiceBundle
import com.zakrywilson.systemctl.service.generator.models.Module
import com.zakrywilson.systemctl.service.generator.models.ServiceInfo
import com.zakrywilson.systemctl.service.generator.resources.ServiceRegistrationResource
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import mu.KotlinLogging

/**
 * Runs the app.
 *
 * @author Zach Wilson
 */
class App : Application<AppConf>() {

    private val log = KotlinLogging.logger {}

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            App().run(*args)
        }
    }

    override fun initialize(bootstrap: Bootstrap<AppConf>) {
        with(bootstrap) {
            addBundle(
                GuiceBundle
                        .newBuilder<AppConf>()
                        .addModule(Module())
                        .enableAutoConfig(this.javaClass.`package`.name)
                        .setConfigClass(AppConf::class.java)
                        .build())
        }
    }

    override fun run(conf: AppConf, environment: Environment) {
        val appHomeDir = conf.appHomeDir
        val deliveryDir = conf.deliveryDir
        val serviceName = conf.serviceName
        val serviceVersion = conf.serviceVersion

        log.trace { "Service name: '$serviceName'" }
        log.trace { "Version: '$serviceVersion'" }
        log.trace { "App home directory: '$appHomeDir'" }
        log.trace { "Delivery directory: '$deliveryDir'" }

        val resource = ServiceRegistrationResource(ServiceInfo(serviceName, serviceVersion),
                appHomeDir,
                deliveryDir)

        environment.jersey().register(resource)
    }

}
