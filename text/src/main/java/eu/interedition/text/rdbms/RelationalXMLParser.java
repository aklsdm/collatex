package eu.interedition.text.rdbms;

import com.google.common.base.Joiner;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.QNameRepository;
import eu.interedition.text.Range;
import eu.interedition.text.xml.XMLParser;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.util.Map;

public class RelationalXMLParser extends XMLParser {
  private static final Joiner PATH_JOINER = Joiner.on('.');

  protected SessionFactory sessionFactory;
  protected QNameRepository nameRepository;
  protected RelationalAnnotationFactory annotationFactory;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void setNameRepository(QNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  public void setAnnotationFactory(RelationalAnnotationFactory annotationFactory) {
    this.annotationFactory = annotationFactory;
  }

  protected Annotation startAnnotation(Session session, QName name, Map<QName, String> attrs, int start,
                                       Iterable<Integer> nodePath) {
    attrs.put(XMLParser.NODE_PATH_NAME, PATH_JOINER.join(nodePath));

    AnnotationRelation annotation = new AnnotationRelation();
    annotation.setText((TextRelation) session.target);
    annotation.setName(nameRepository.get(name));
    annotation.setRange(new Range(start, start));
    annotation.setSerializableData((Serializable) attrs);
    return annotation;
  }

  protected void endAnnotation(Annotation annotation, int offset) {
    annotation.getRange().setEnd(offset);
    sessionFactory.getCurrentSession().save(annotation);
  }

  @Override
  protected void newOffsetDelta(Session session, Range textRange, Range sourceRange) {
    annotationFactory.create(session.target, OFFSET_DELTA_NAME,//
            textRange).setSerializableData(sourceRange);
  }

  protected void newXMLEventBatch() {
    org.hibernate.Session session = sessionFactory.getCurrentSession();
    session.flush();
    session.clear();
  }
}
