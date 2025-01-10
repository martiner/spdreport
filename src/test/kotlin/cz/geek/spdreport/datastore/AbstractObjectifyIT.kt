package cz.geek.spdreport.datastore

import com.googlecode.objectify.ObjectifyService
import cz.geek.spdreport.ItHelper.objectifyPort
import cz.geek.spdreport.ItHelper.objectifyProject
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FreeSpec

abstract class AbstractObjectifyIT(body: FreeSpec.() -> Unit) : FreeSpec(body) {

    override suspend fun beforeSpec(spec: Spec) {
        ObjectifyConfiguration(objectifyPort, objectifyProject).init()
    }

    companion object {
        fun objectify(block: () -> Unit) {
            ObjectifyService.begin().use {
                block()
            }
        }
    }
}
