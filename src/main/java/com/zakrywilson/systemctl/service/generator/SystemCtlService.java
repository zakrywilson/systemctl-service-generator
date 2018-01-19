package com.zakrywilson.systemctl.service.generator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Zach Wilson
 */
public class SystemCtlService {

    private long id;
    private String serviceFileContent;

    public SystemCtlService() {}

    public SystemCtlService(long id, String serviceFileContent) {
        this.id = id;
        this.serviceFileContent = serviceFileContent;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    @JsonProperty
    public String getServiceFileContent() {
        return serviceFileContent;
    }

}
