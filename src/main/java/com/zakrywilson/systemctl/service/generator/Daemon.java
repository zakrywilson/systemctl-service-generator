package com.zakrywilson.systemctl.service.generator;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Zach Wilson
 */
@Path("daemon")
@Produces(MediaType.APPLICATION_JSON)
public class Daemon {

    private final AtomicLong counter;

    public Daemon() {
        this.counter = new AtomicLong();
    }

    @POST
    @Timed
    public Response register(@QueryParam("register") Optional<String> serviceFileContents) {
        if (!serviceFileContents.isPresent()) {
            return Response.status(404).build();
        }
        SystemCtlService systemCtlService = new SystemCtlService(counter.incrementAndGet(), serviceFileContents.get());
        return Response.accepted(systemCtlService).build();
    }

}
