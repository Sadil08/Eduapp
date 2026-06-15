package com.eduapp.backend.media;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Jackson serializer for image-URL DTO fields: emits a short-lived signed URL instead of
 * the stored (now-private) reference. Apply with
 * {@code @JsonSerialize(using = SignedUrlSerializer.class)} on the field.
 */
public class SignedUrlSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(MediaUrlSigner.sign(value));
    }
}
