/*
 * Copyright (C) 2016 Christian Stein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.sormuras.listing;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.lang.model.SourceVersion;

/**
 * A class or method is generic if it declares one or more type variables (§4.4).
 * <p>
 * These type variables are known as the type parameters of the class. The type parameter section
 * follows the class name and is delimited by angle brackets.
 *
 * @author Christian Stein
 * @see https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.1.2
 */
public class TypeParameter implements Listable, Annotated<TypeParameter> {

  private final List<JavaAnnotation> annotations = new ArrayList<>();
  private final List<ClassType> bounds = new ArrayList<>();
  private TypeVariable boundTypeVariable = null;
  private final String name;

  public TypeParameter(String name) {
    this.name = Tool.check(name, "name");
    Tool.assume(SourceVersion.isIdentifier(name), "expected legal identifier, but got: " + name);
  }

  /**
   * Add (additional) bound(s) to the list of bounds and clears bound type variable.
   */
  public TypeParameter addBounds(JavaType<?>... bounds) {
    return addBounds(Arrays.stream(bounds).map(t -> (ClassType) t).toArray(ClassType[]::new));
  }

  public TypeParameter addBounds(ClassType... bounds) {
    this.boundTypeVariable = null;
    Collections.addAll(this.bounds, bounds);
    this.bounds.removeIf(ct -> ct.isJavaLangObject());
    return this;
  }

  /**
   * {TypeParameterModifier} Identifier [TypeBound]
   */
  @Override
  public Listing apply(Listing listing) {
    listing.add(toAnnotationsListable());
    listing.add(name);
    if (boundTypeVariable == null && bounds.isEmpty()) {
      return listing;
    }
    listing.add(" extends ");
    if (boundTypeVariable != null) {
      listing.add(boundTypeVariable);
    } else {
      listing.add(bounds, " & ");
    }
    return listing;
  }

  @Override
  public List<JavaAnnotation> getAnnotations() {
    return annotations;
  }

  @Override
  public ElementType getAnnotationTarget() {
    return ElementType.TYPE_PARAMETER;
  }

  public List<ClassType> getBounds() {
    return bounds;
  }

  public Optional<TypeVariable> getBoundTypeVariable() {
    return Optional.ofNullable(boundTypeVariable);
  }

  public String getName() {
    return name;
  }

  /**
   * Set single type variable as bound and clears all other bounds.
   */
  public TypeParameter setBoundTypeVariable(String typeVariableName) {
    return setBoundTypeVariable(new TypeVariable().setName(typeVariableName));
  }

  /**
   * Set single type variable as bound and clears all other bounds.
   */
  public TypeParameter setBoundTypeVariable(TypeVariable boundTypeVariable) {
    this.boundTypeVariable = boundTypeVariable;
    this.bounds.clear();
    return this;
  }
}
