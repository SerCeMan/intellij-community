/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * @author max
 */
package com.intellij.psi.impl.java.stubs.impl;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.cache.TypeInfo;
import com.intellij.psi.impl.compiled.StubBuildingVisitor;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.java.stubs.PsiMethodStub;
import com.intellij.psi.impl.java.stubs.PsiParameterListStub;
import com.intellij.psi.impl.java.stubs.PsiParameterStub;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.BitUtil;
import com.intellij.util.cls.ClsFormatException;
import com.intellij.util.io.StringRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PsiMethodStubImpl extends StubBase<PsiMethod> implements PsiMethodStub {
  private final TypeInfo myReturnType;
  private final byte myFlags;
  private final StringRef myName;
  private StringRef myDefaultValueText;

  private static final int CONSTRUCTOR = 0x01;
  private static final int VARARGS = 0x02;
  private static final int ANNOTATION = 0x04;
  private static final int DEPRECATED = 0x08;
  private static final int DEPRECATED_ANNOTATION = 0x10;
  private static final int PARSED_VIA_GENERIC_SIGNATURE = 0x20;

  public PsiMethodStubImpl(StubElement parent,
                           StringRef name,
                           byte flags,
                           String signature,
                           @NotNull List<String> args,
                           @Nullable List<String> throwables,
                           String desc,
                           int modifiersMask) {
    super(parent, isAnnotationMethod(flags) ? JavaStubElementTypes.ANNOTATION_METHOD : JavaStubElementTypes.METHOD);
    myName = name;
    myDefaultValueText = null;

    new PsiModifierListStubImpl(this, modifiersMask);

    String returnType = null;
    boolean parsedViaGenericSignature = false;
    if (signature != null) {
      try {
        returnType = StubBuildingVisitor.parseMethodViaGenericSignature(signature, this, args, throwables);
        parsedViaGenericSignature = true;
      }
      catch (ClsFormatException ignored) { }
    }
    if (returnType == null) {
      returnType = StubBuildingVisitor.parseMethodViaDescription(desc, this, args);
    }

    myReturnType = TypeInfo.fromString(returnType);
    myFlags = (byte)(flags | (parsedViaGenericSignature ? PARSED_VIA_GENERIC_SIGNATURE : 0));
  }

  public PsiMethodStubImpl(StubElement parent, StringRef name, @NotNull TypeInfo returnType, byte flags, StringRef defaultValueText) {
    super(parent, isAnnotationMethod(flags) ? JavaStubElementTypes.ANNOTATION_METHOD : JavaStubElementTypes.METHOD);
    myReturnType = returnType;
    myFlags = flags;
    myName = name;
    myDefaultValueText = defaultValueText;
  }

  @Override
  public boolean isConstructor() {
    return BitUtil.isSet(myFlags, CONSTRUCTOR);
  }

  @Override
  public boolean isVarArgs() {
    return BitUtil.isSet(myFlags, VARARGS);
  }

  public boolean isParsedViaGenericSignature() {
    return BitUtil.isSet(myFlags, PARSED_VIA_GENERIC_SIGNATURE);
  }

  @Override
  public boolean isAnnotationMethod() {
    return isAnnotationMethod(myFlags);
  }

  public static boolean isAnnotationMethod(final byte flags) {
    return (flags & ANNOTATION) != 0;
  }

  @Override
  public String getDefaultValueText() {
    return StringRef.toString(myDefaultValueText);
  }

  @Override
  @NotNull
  public TypeInfo getReturnTypeText(boolean doResolve) {
    return doResolve ? myReturnType.applyAnnotations(this) : myReturnType;
  }

  @Override
  public boolean isDeprecated() {
    return (myFlags & DEPRECATED) != 0;
  }

  @Override
  public boolean hasDeprecatedAnnotation() {
    return (myFlags & DEPRECATED_ANNOTATION) != 0;
  }

  @Override
  public PsiParameterStub findParameter(final int idx) {
    PsiParameterListStub list = null;
    for (StubElement child : getChildrenStubs()) {
      if (child instanceof PsiParameterListStub) {
        list = (PsiParameterListStub)child;
        break;
      }
    }

    if (list != null) {
      final List<StubElement> params = list.getChildrenStubs();
      return (PsiParameterStub)params.get(idx);
    }

    throw new RuntimeException("No parameter(s) [yet?]");
  }

  @Override
  public String getName() {
    return StringRef.toString(myName);
  }

  public byte getFlags() {
    return myFlags;
  }

  public void setDefaultValueText(final String defaultValueText) {
    myDefaultValueText = StringRef.fromString(defaultValueText);
  }

  public static byte packFlags(boolean isConstructor,
                               boolean isAnnotationMethod,
                               boolean isVarargs,
                               boolean isDeprecated,
                               boolean hasDeprecatedAnnotation) {
    byte flags = 0;
    if (isConstructor) flags |= CONSTRUCTOR;
    if (isAnnotationMethod) flags |= ANNOTATION;
    if (isVarargs) flags |= VARARGS;
    if (isDeprecated) flags |= DEPRECATED;
    if (hasDeprecatedAnnotation) flags |= DEPRECATED_ANNOTATION;
    return flags;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PsiMethodStub[");

    if (isConstructor()) {
      builder.append("cons ");
    }
    if (isAnnotationMethod()) {
      builder.append("annotation ");
    }
    if (isVarArgs()) {
      builder.append("varargs ");
    }
    if (isDeprecated() || hasDeprecatedAnnotation()) {
      builder.append("deprecated ");
    }

    builder.append(myName).append(":").append(myReturnType);

    String defaultValue = getDefaultValueText();
    if (defaultValue != null) {
      builder.append(" default=").append(defaultValue);
    }

    builder.append("]");
    return builder.toString();
  }
}
