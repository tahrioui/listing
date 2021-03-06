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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ClassType extends ReferenceType<ClassType> {

  public static ClassType of(Class<?> type) {
    return new ClassType(JavaName.of(type));
  }

  public static ClassType of(Class<?> type, Class<?>... arguments) {
    TypeArgument[] args = stream(arguments).map(TypeArgument::new).toArray(TypeArgument[]::new);
    return new ClassType(JavaName.of(type), args);
  }

  public static ClassType of(String... names) {
    return new ClassType(JavaName.of(names));
  }

  private final List<ClassName> names;
  private final String packageName;
  private final JavaName typeName;

  public ClassType(JavaName typeName, TypeArgument... typeArguments) {
    this.typeName = typeName;
    this.packageName = typeName.getPackageName();
    this.names = typeName.getSimpleNames().stream().map(ClassName::new).collect(toList());
    Tool.assume(!names.isEmpty(), "not a single class name given by: %s", typeName);
    Collections.addAll(getTypeArguments(), typeArguments);
  }

  @Override
  public Listing apply(Listing listing) {
    if (!getPackageName().isEmpty()) {
      listing.add(getPackageName()).add('.');
    }
    return listing.add(getNames(), ".");
  }

  @Override
  public List<JavaAnnotation> getAnnotations() {
    return names.get(names.size() - 1).getAnnotations();
  }

  @Override
  public ElementType getAnnotationTarget() {
    return ElementType.TYPE_USE;
  }

  public Optional<ClassType> getEnclosingClassType() {
    if (names.size() == 1) {
      return Optional.empty();
    }
    return Optional.of(new ClassType(typeName.getEnclosing().get()));
  }

  public List<ClassName> getNames() {
    return names;
  }

  public String getPackageName() {
    return packageName;
  }

  public List<TypeArgument> getTypeArguments() {
    return names.get(names.size() - 1).getTypeArguments();
  }

  public JavaName getTypeName() {
    return typeName;
  }

  @Override
  public boolean isJavaLangObject() {
    return getTypeName().isJavaLangObject();
  }

  @Override
  public String toClassName() {
    StringBuilder builder = new StringBuilder();
    if (!getPackageName().isEmpty()) {
      builder.append(getPackageName()).append('.');
    }
    builder.append(getNames().stream().map(n -> n.getName()).collect(joining("$")));
    return builder.toString();
  }
}
