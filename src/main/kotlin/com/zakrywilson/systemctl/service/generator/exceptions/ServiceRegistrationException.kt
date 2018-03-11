package com.zakrywilson.systemctl.service.generator.exceptions

/**
 * Thrown to indicate that registering a SystemCtl service has failed.
 *
 * @author Zach Wilson
 */
class ServiceRegistrationException : Exception {

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(message: String, throwable: Throwable) : super(message, throwable)

    constructor(throwable: Throwable) : super(throwable)

}
