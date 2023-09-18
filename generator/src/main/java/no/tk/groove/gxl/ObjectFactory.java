package no.tk.groove.gxl;

import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java element interface
 * generated in the groove.gxl package.
 *
 * <p>An ObjectFactory allows you to programatically construct new instances of the Java
 * representation for XML content. The Java representation of XML content can consist of schema
 * derived interfaces and classes representing the binding of schema type definitions, element
 * declarations and model groups. Factory methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes
   * for package: groove.gxl
   */
  public ObjectFactory() {}

  /** Create an instance of {@link Set } */
  public Set createSet() {
    return new Set();
  }

  /** Create an instance of {@link Locator } */
  public Locator createLocator() {
    return new Locator();
  }

  /** Create an instance of {@link Bool } */
  public Bool createBool() {
    return new Bool();
  }

  /** Create an instance of {@link Int } */
  public Int createInt() {
    return new Int();
  }

  /** Create an instance of {@link Float } */
  public Float createFloat() {
    return new Float();
  }

  /** Create an instance of {@link String } */
  public String createString() {
    return new String();
  }

  /** Create an instance of {@link Enum } */
  public Enum createEnum() {
    return new Enum();
  }

  /** Create an instance of {@link Seq } */
  public Seq createSeq() {
    return new Seq();
  }

  /** Create an instance of {@link Bag } */
  public Bag createBag() {
    return new Bag();
  }

  /** Create an instance of {@link Tup } */
  public Tup createTup() {
    return new Tup();
  }

  /** Create an instance of {@link Relend } */
  public Relend createRelend() {
    return new Relend();
  }

  /** Create an instance of {@link Attr } */
  public Attr createAttr() {
    return new Attr();
  }

  /** Create an instance of {@link Type } */
  public Type createType() {
    return new Type();
  }

  /** Create an instance of {@link Graph } */
  public Graph createGraph() {
    return new Graph();
  }

  /** Create an instance of {@link Node } */
  public Node createNode() {
    return new Node();
  }

  /** Create an instance of {@link Edge } */
  public Edge createEdge() {
    return new Edge();
  }

  /** Create an instance of {@link Rel } */
  public Rel createRel() {
    return new Rel();
  }

  /** Create an instance of {@link Gxl } */
  public Gxl createGxl() {
    return new Gxl();
  }
}
