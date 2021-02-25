@file:UseSerializers(IdentitySer::class)
package io.ejekta.bountiful

import io.ejekta.bountiful.config.BountifulIO
import io.ejekta.kambrik.Kambrik
import io.ejekta.kambrikx.serial.serializers.IdentitySer
import kotlinx.serialization.UseSerializers
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier


class Bountiful : ModInitializer {

    companion object {
        const val ID = "bountiful"
        fun id(str: String) = Identifier(ID, str)
        val LOGGER = Kambrik.Logging.createLogger(ID)
        val Markers = object {
            val BountyCreation = Kambrik.Logging.createMarker("BountyCreation")
        }
    }

    init {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(BountifulIO)
    }

    override fun onInitialize() {
        LOGGER.info("Common init")
        BountifulIO.loadConfig()

        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server ->
            listOf("plains", "savanna", "snowy", "taiga", "desert").forEach { villageType ->

                LOGGER.error("Registering Bounty Board Jigsaw Piece for Village Type: $villageType")

                Kambrik.Structure.addToStructurePool(
                    server,
                    Identifier("bountiful:village/common/bounty_gazebo"),
                    Identifier("minecraft:village/$villageType/houses"),
                    10_000
                )

            }
        })

    }

}