package com.github.sormuras.javaunit;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.ElementType;

import org.junit.Test;

public class MethodParameterTest {

  @Test
  public void simple() {
    assertEquals("int i", MethodParameter.of(int.class, "i").list());
    assertEquals("int... ia1", MethodParameter.of(int[].class, "ia1").setVariable(true).list());
    assertEquals("int[]... ia2", MethodParameter.of(int[][].class, "ia2").setVariable(true).list());
    assertEquals(
        "final T t",
        new MethodParameter()
            .setType(new TypeVariable().setName("T"))
            .setName("t")
            .setFinal(true)
            .list());
    assertEquals(ElementType.PARAMETER, new MethodParameter().getAnnotationTarget());
  }
}
