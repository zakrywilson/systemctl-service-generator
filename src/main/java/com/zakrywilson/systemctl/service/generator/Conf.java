package com.zakrywilson.systemctl.service.generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Zach Wilson
 */
public class Conf extends Configuration {

    @NotEmpty
    private String home;

    @JsonProperty
    public String getHome() {
        return home;
    }

    @JsonProperty
    public void setHome(String home) {
        this.home = home;
    }

}
