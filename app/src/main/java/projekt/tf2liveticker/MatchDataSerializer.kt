package projekt.tf2liveticker

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import projekt.tf2liveticker.Models.MatchDataList
import java.io.InputStream
import java.io.OutputStream

object MatchDataSerializer : Serializer<MatchDataList> {

    override suspend fun readFrom(input: InputStream): MatchDataList {
        return try {
            Json.decodeFromString(deserializer = MatchDataList.serializer(),
                string = input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(
        t: MatchDataList,
        output: OutputStream
    ) {
        output.write(
            Json.encodeToString(t)
                .encodeToByteArray()
        )
    }

    override val defaultValue: MatchDataList
        get() = MatchDataList()

}