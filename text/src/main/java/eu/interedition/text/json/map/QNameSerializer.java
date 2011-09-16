package eu.interedition.text.json.map;

import eu.interedition.text.QName;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class QNameSerializer extends JsonSerializer<QName> {

  @Override
  public Class<QName> handledType() {
    return QName.class;
  }

  @Override
  public void serialize(QName value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
    serialize(value, jgen, (Map<URI, String>) null);
  }

  public static void serialize(QName value, JsonGenerator jgen, Map<URI, String> namespaceMapping) throws IOException {
    jgen.writeStartArray();

    final URI ns = value.getNamespaceURI();
    if (ns == null) {
      jgen.writeNull();
    } else if (namespaceMapping != null && namespaceMapping.containsKey(ns)) {
      jgen.writeString(namespaceMapping.get(ns));
    } else {
      jgen.writeString(ns.toString());
    }

    jgen.writeString(value.getLocalName());

    jgen.writeEndArray();

  }
}