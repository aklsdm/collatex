package eu.interedition.text.json.map;

import eu.interedition.text.QName;
import eu.interedition.text.mem.SimpleQName;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;
import java.net.URI;

import static org.codehaus.jackson.JsonToken.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class QNameDeserializer extends JsonDeserializer<QName> {
  @Override
  public QName deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    if (!START_ARRAY.equals(jp.getCurrentToken())) {
      throw new JsonParseException("QName: Expected start of array", jp.getCurrentLocation());
    }

    JsonToken token = jp.nextToken();
    if (!VALUE_STRING.equals(token) && !VALUE_NULL.equals(token)) {
      throw new JsonParseException("QName: Expected string or null as namespace", jp.getCurrentLocation());
    }

    final URI namespace = VALUE_NULL.equals(token) ? null : URI.create(jp.getText());

    token = jp.nextToken();
    if (!VALUE_STRING.equals(token)) {
      throw new JsonParseException("QName: Expected string as local name", jp.getCurrentLocation());
    }
    final SimpleQName name = new SimpleQName(namespace, jp.getText());

    if (!END_ARRAY.equals(jp.nextToken())) {
      throw new JsonParseException("QName: Expected end of array", jp.getCurrentLocation());
    }

    return name;
  }
}
