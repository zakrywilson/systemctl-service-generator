package com.zakrywilson.systemctl.service.generator.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Contains service information.
 *
 * @author Zach Wilson
 */
data class ServiceInfo(@JsonProperty val name: String, @JsonProperty val version: String)
