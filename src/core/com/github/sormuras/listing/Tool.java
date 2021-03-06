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

import static java.lang.Character.isISOControl;
import static java.util.Collections.addAll;
import static java.util.Collections.reverse;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.lang.model.element.Modifier;

/**
 * Common tools.
 *
 * @author Christian Stein
 */
public interface Tool {

  static Pattern TRIM_RIGHT_PATTERN = Pattern.compile("^\\s+$");

  static void assume(boolean condition, String format, Object... args) {
    if (!condition) throw new IllegalArgumentException(String.format(format, args));
  }

  /**
   * Creates a joined name string for the given components.
   *
   * @param packageName the name of the package, can be empty
   * @param names the list of names, can be empty
   * @return all names joined to a single string
   */
  static String canonical(String packageName, List<String> names) {
    check(packageName, "packageName");
    check(names, "names");
    assume(!(packageName.isEmpty() && names.isEmpty()), "packageName and names are empty");
    if (names.isEmpty()) {
      return packageName;
    }
    StringBuilder builder = new StringBuilder();
    if (!packageName.isEmpty()) {
      builder.append(packageName).append(".");
    }
    if (names.size() == 1) {
      return builder.append(names.get(0)).toString();
    }
    return builder.append(String.join(".", names)).toString();
  }

  static <T> T check(T reference, String referenceName) {
    if (reference == null) throw new NullPointerException(referenceName + " must not be null");
    return reference;
  }

  static ElementType elementOf(Member member) {
    if (member instanceof Constructor) return ElementType.CONSTRUCTOR;
    if (member instanceof Field) return ElementType.FIELD;
    if (member instanceof Method) return ElementType.METHOD;
    throw new AssertionError("unexpected member: " + member);
  }

  /**
   * Escape Sequences for Character and String Literals.
   *
   * The character and string escape sequences allow for the representation of some nongraphic
   * characters without using Unicode escapes, as well as the single quote, double quote, and
   * backslash characters, in character literals (§3.10.4) and string literals (§3.10.5).
   *
   * https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.10.6
   */
  static String escape(char c) {
    switch (c) {
      case '\b': /* \u0008: backspace (BS) */
        return "\\b";
      case '\t': /* \u0009: horizontal tab (HT) */
        return "\\t";
      case '\n': /* \u000a: linefeed (LF) */
        return "\\n";
      case '\f': /* \u000c: form feed (FF) */
        return "\\f";
      case '\r': /* \u000d: carriage return (CR) */
        return "\\r";
      case '\"': /* \u0022: double quote (") */
        return "\"";
      case '\'': /* \u0027: single quote (') */
        return "\\'";
      case '\\': /* \u005c: backslash (\) */
        return "\\\\";
      default:
        return isISOControl(c) ? String.format("\\u%04x", (int) c) : Character.toString(c);
    }
  }

  /** Returns the string literal representing {@code value}, including wrapping double quotes. */
  static String escape(String value) {
    StringBuilder result = new StringBuilder(value.length() + 2);
    result.append('"');
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      // trivial case: single quote must not be escaped
      if (c == '\'') {
        result.append("'");
        continue;
      }
      // trivial case: double quotes must be escaped
      if (c == '\"') {
        result.append("\\\"");
        continue;
      }
      // default case: just let character escaper do its work
      result.append(escape(c));
    }
    result.append('"');
    return result.toString();
  }

  static Set<Modifier> modifiers(int mod) {
    Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    if (java.lang.reflect.Modifier.isAbstract(mod)) modifiers.add(Modifier.ABSTRACT);
    if (java.lang.reflect.Modifier.isFinal(mod)) modifiers.add(Modifier.FINAL);
    if (java.lang.reflect.Modifier.isNative(mod)) modifiers.add(Modifier.NATIVE);
    if (java.lang.reflect.Modifier.isPrivate(mod)) modifiers.add(Modifier.PRIVATE);
    if (java.lang.reflect.Modifier.isProtected(mod)) modifiers.add(Modifier.PROTECTED);
    if (java.lang.reflect.Modifier.isPublic(mod)) modifiers.add(Modifier.PUBLIC);
    if (java.lang.reflect.Modifier.isStatic(mod)) modifiers.add(Modifier.STATIC);
    if (java.lang.reflect.Modifier.isStrict(mod)) modifiers.add(Modifier.STRICTFP);
    if (java.lang.reflect.Modifier.isSynchronized(mod)) modifiers.add(Modifier.SYNCHRONIZED);
    if (java.lang.reflect.Modifier.isTransient(mod)) modifiers.add(Modifier.TRANSIENT);
    if (java.lang.reflect.Modifier.isVolatile(mod)) modifiers.add(Modifier.VOLATILE);
    return modifiers;
  }

  static String packageOf(Class<?> type) {
    check(type, "type");
    // trivial case: package is attached to the type
    Package packageOfType = type.getPackage();
    if (packageOfType != null) {
      return packageOfType.getName();
    }
    // trivial case: no package by definition
    if (type.isArray() || type.isPrimitive()) { // || type.isAnonymousClass()) {
      // TODO investigate type.isLocalClass() || type.isMemberClass() || type.isSynthetic()
      return "";
    }
    return packageOf(type.getCanonicalName());
  }

  // find last '.' and return first part
  static String packageOf(String typeName) {
    String name = typeName;
    int lastDot = name.lastIndexOf('.');
    if (lastDot == -1) {
      return "";
    }
    name = name.substring(0, lastDot);
    if (name.isEmpty()) {
      throw new AssertionError("empty package name of type named: " + typeName);
    }
    return name;
  }

  static List<String> simpleNames(Class<?> type, String... additionalNames) {
    check(type, "type");
    List<String> names = new ArrayList<>();
    while (type != null) {
      names.add(type.getSimpleName());
      type = type.getEnclosingClass();
    }
    reverse(names);
    addAll(names, additionalNames);
    return names;
  }

  static String trimRight(String text) {
    return TRIM_RIGHT_PATTERN.matcher(text).replaceAll("");
  }
}
