package com.zakrywilson.systemctl.service.generator.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a single SystemCtl service.
 *
 * @author Zach Wilson
 */
data class SystemCtlService(@JsonProperty val id: Long,
                            @JsonProperty val name: String,
                            @JsonProperty val content: String,
                            @JsonProperty val jarFileName: String)
