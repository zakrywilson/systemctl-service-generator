package com.zakrywilson.systemctl.service.generator;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * @author Zach Wilson
 */
public class App extends Application<Conf> {

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void initialize(Bootstrap<Conf> bootstrap) {
        // nothing to do quite yet
    }

    public void run(Conf conf, Environment environment) {
        final Daemon resource = new Daemon();
        environment.jersey().register(resource);
    }

}
