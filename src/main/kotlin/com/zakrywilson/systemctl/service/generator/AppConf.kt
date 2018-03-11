package com.zakrywilson.systemctl.service.generator

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration

/**
 * App configuration.
 *
 * @author Zach Wilson
 */
class AppConf : Configuration() {

    @get:JsonProperty
    @set:JsonProperty
    var serviceName: String = ""

    @get:JsonProperty
    @set:JsonProperty
    var serviceVersion: String = ""

    @get:JsonProperty
    @set:JsonProperty
    var appHomeDir: String = ""

    @get:JsonProperty
    @set:JsonProperty
    var deliveryDir: String = ""

}
