package eu.interedition.text;

import java.util.Set;


public interface QNameRepository {

  QName get(QName name);

  Set<QName> get(Set<QName> name);
}
