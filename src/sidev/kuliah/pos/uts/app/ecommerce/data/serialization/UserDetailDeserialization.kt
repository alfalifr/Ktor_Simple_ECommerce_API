package sidev.kuliah.pos.uts.app.ecommerce.sidev.kuliah.pos.uts.app.ecommerce.data.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import sidev.kuliah.pos.uts.app.ecommerce.data.model.UserDetail

object UserDetailDeserialization: DeserializationStrategy<UserDetail> {
    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("user")

    override fun deserialize(decoder: Decoder): UserDetail {
        TODO("Not yet implemented")
    }

    override fun patch(decoder: Decoder, old: UserDetail): UserDetail {
        TODO("Not yet implemented")
    }
}