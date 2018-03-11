package com.zakrywilson.systemctl.service.generator.resources

import com.codahale.metrics.annotation.Timed
import com.google.inject.Inject
import com.google.inject.name.Named
import com.zakrywilson.systemctl.service.generator.models.ServiceInfo
import com.zakrywilson.systemctl.service.generator.models.SystemCtlService
import com.zakrywilson.systemctl.service.generator.exceptions.ServiceDeregistrationException
import com.zakrywilson.systemctl.service.generator.exceptions.ServiceRegistrationException
import com.zakrywilson.systemctl.service.generator.exceptions.ServiceStartException
import com.zakrywilson.systemctl.service.generator.exceptions.ServiceStopException
import mu.KotlinLogging
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * SystemCtl registration resource.
 *
 * @author Zach Wilson
 */
@Path("/service-registration")
@Produces(MediaType.APPLICATION_JSON)
class ServiceRegistrationResource @Inject constructor(
        @Named("serviceInfo") private val serviceInfo: ServiceInfo,
        @Named("appHomeDir") private val appHomeDir: String,
        @Named("deliveryDir") private val deliveryDir: String) {

    private val log = KotlinLogging.logger {}
    private val counter: AtomicLong = AtomicLong()

    private var serviceMap: Map<String, SystemCtlService> = HashMap()

    @GET()
    @Path("/info")
    fun ping(): Response {
        return Response.ok(serviceInfo).build()
    }

    @POST
    @Timed
    fun registerService(@QueryParam("service-name") serviceName: Optional<String>,
                        @QueryParam("systemctl-file") serviceFileContents: Optional<String>,
                        @QueryParam("jar-file-name") serviceJarFileName: Optional<String>)
                        : Response {
        log.trace { "Received request to register a service" }

        // If anything is missing, return a 404
        if (!serviceName.isPresent || !serviceFileContents.isPresent || !serviceJarFileName.isPresent) {
            log.debug { "Missing required parameters for service registration request" }
            return Response.status(404).build()
        }

        log.isDebugEnabled.run {
            log.debug {
                """Service name: '${serviceName.get()}',
                   Service JAR location: '${serviceJarFileName.get()}'
                   Service file contents: '${serviceFileContents.get()}'
                """.trimIndent()
            }
        }

        val service = SystemCtlService(counter.incrementAndGet(),
                serviceName.get(),
                serviceFileContents.get(),
                serviceJarFileName.get())

        // Check if service already exists; if service exists, shut it down and de-register it
        if (serviceMap.containsKey(service.name)) {
            log.debug { "SystemCtl service '${service.name}' exists" }

            stopService(service.name)
            log.debug { "Successfully stopped SystemCtl service: '${service.name}'" }

            deRegisterService(service.name, appHomeDir, service.jarFileName)
            log.debug { "Successfully disabled old SystemCtl service '${service.name}'" }
        }

        // Register new service
        registerService(service, appHomeDir, deliveryDir, service.jarFileName)
        serviceMap = serviceMap.plus(Pair(service.name, service))
        log.isDebugEnabled.run {
            log.debug {
                """Successfully added new SystemCtl service '${service.name}' to the cache:
                   ${serviceMap[service.name]}
                """.trimIndent() }
        }

        // Start the new service
        startService(service.name)
        log.debug { "Successfully started SystemCtl service: '${service.name}'" }

        return Response.accepted().build()
    }

    @Throws(ServiceStartException::class)
    private fun startService(name: String) {
        log.debug { "Starting '$name'" }
        try {
            execute("systemctl", "start", name)
        } catch (e: Exception) {
            throw ServiceStartException("Failed to start SystemCtl service '$name': ", e)
        }
    }

    @Throws(ServiceStopException::class)
    private fun stopService(name: String) {
        log.debug { "Stopping '$name'" }
        try {
            execute("systemctl", "stop", name)
        } catch (e: Exception) {
            throw ServiceStopException("Failed to stop SystemCtl service '$name': ", e)
        }
    }

    @Throws(ServiceDeregistrationException::class)
    private fun deRegisterService(name: String, appHomeDir: String, jarFileName: String) {
        log.debug { "De-registering '$name'" }
        try {
            // Disable the service
            execute("systemctl", "disable", name)

            // Delete the JAR
            val oldJar = File(appHomeDir, jarFileName)
            log.debug { "Deleting old JAR file: '$oldJar'" }
            Files.deleteIfExists(oldJar.toPath())
        } catch (e: Exception) {
            throw ServiceDeregistrationException("Failed to de-register SystemCtl service '$name': ", e)
        }
    }

    @Throws(ServiceRegistrationException::class)
    private fun registerService(service: SystemCtlService,
                                appHomeDir: String,
                                deliveryDir: String,
                                jarFileName: String) {
        log.debug { "Registering '${service.name}'" }
        try {
            // Move the new JAR to the app directory
            val sourceJar = File(deliveryDir, jarFileName).toPath()
            val targetJar = File(appHomeDir, jarFileName).toPath()
            log.debug { "Moving JAR '$sourceJar' to '$targetJar'" }
            Files.copy(sourceJar, targetJar, StandardCopyOption.REPLACE_EXISTING)

            // Enable the service
            execute("systemctl", "enable", service.name)
        } catch (e: Exception) {
            throw ServiceRegistrationException("Failed to register SystemCtl service '${service.name}': ", e)
        }
    }

    @Throws(IOException::class)
    private fun execute(vararg commands: String) {
        log.isTraceEnabled.run {
            log.trace("Executing the following commands: {}", Arrays.asList(*commands))
        }

        val outputFile = Files.createTempFile(
                String.format("registration-service_output-log%s", Instant.now().toEpochMilli()),
                null)

        val process = ProcessBuilder(*commands)
                .inheritIO()
                .directory(File(System.getProperty("java.io.tmpdir")))
                .redirectOutput(ProcessBuilder.Redirect.to(outputFile.toFile()))
                .start()

        log.isDebugEnabled.run {
            log.trace { "Obtaining process execution output..." }
            Files.readAllLines(outputFile, UTF_8)
                    .filter { it.isNotBlank() }
                    .joinToString(separator = System.lineSeparator())
                    .let {
                        log.debug { "System command process output: ${System.lineSeparator()}$it" }
                    }
        }

        log.trace("Waiting for processes to complete...")
        try {
            process.waitFor()
            log.debug("Successfully executed commands")
        } catch (e: InterruptedException) {
            log.warn { "Interrupted while waiting for process to complete for commands: '$commands'" }
        }
    }

}
