/*
 * Copyright 2013, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib2.analysis;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.reflection.ReflectionClassDef;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.util.collection.ListUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPath {

    private final TypeProto unknownClass;

    private List<ClassProvider> classProviders;
    private final boolean checkPackagePrivateAccess;
    public final int oatVersion;

    public static final int NOT_ART = -1;
    public static final int NOT_SPECIFIED = -2;

    /**
     * Creates a new ClassPath instance that can load classes from the given providers
     *
     * @param classProviders A varargs array of ClassProviders. When loading a class, these providers will be searched
     *                       in order
     */
    public ClassPath(ClassProvider... classProviders) throws IOException {
        this(Arrays.asList(classProviders), false, NOT_ART);
    }

    /**
     * Creates a new ClassPath instance that can load classes from the given providers
     *
     * @param classProviders An iterable of ClassProviders. When loading a class, these providers will be searched in
     *                       order
     */
    public ClassPath(Iterable<ClassProvider> classProviders) throws IOException {
        this(classProviders, false, NOT_ART);
    }

    /**
     * Creates a new ClassPath instance that can load classes from the given providers
     *
     * @param classProviders An iterable of ClassProviders. When loading a class, these providers will be searched in
     *                       order
     * @param checkPackagePrivateAccess Whether checkPackagePrivateAccess is needed, enabled for ONLY early API 17 by
     *                                  default
     * @param oatVersion The applicable oat version, or NOT_ART
     */
    public ClassPath( Iterable<? extends ClassProvider> classProviders, boolean checkPackagePrivateAccess,
                     int oatVersion) {
        // add fallbacks for certain special classes that must be present
        unknownClass = new UnknownClassProto(this);
        classLoader.put(unknownClass.getType(), unknownClass);
        this.checkPackagePrivateAccess = checkPackagePrivateAccess;
        this.oatVersion = oatVersion;

        loadPrimitiveType("Z");
        loadPrimitiveType("B");
        loadPrimitiveType("S");
        loadPrimitiveType("C");
        loadPrimitiveType("I");
        loadPrimitiveType("J");
        loadPrimitiveType("F");
        loadPrimitiveType("D");
        loadPrimitiveType("L");

        this.classProviders = ListUtil.newArrayList(classProviders);
        this.classProviders.add(getBasicClasses());
    }

    private void loadPrimitiveType(String type) {
        classLoader.put(type, new PrimitiveProto(this, type));
    }

    private static ClassProvider getBasicClasses() {
        // fallbacks for some special classes that we assume are present
        return new DexClassProvider(new ImmutableDexFile(Opcodes.getDefault(), ListUtil.of(
                new ReflectionClassDef(Class.class),
                new ReflectionClassDef(Cloneable.class),
                new ReflectionClassDef(Object.class),
                new ReflectionClassDef(Serializable.class),
                new ReflectionClassDef(String.class),
                new ReflectionClassDef(Throwable.class))));
    }

    public boolean isArt() {
        return oatVersion != NOT_ART;
    }


    public TypeProto getClass( CharSequence type) {
        return classLoader.get(type.toString());
    }

    private final Map<String, TypeProto> classLoader = new HashMap<String, TypeProto>() {
        @Override
        public TypeProto get(Object obj){
            String type = obj.toString();
            TypeProto exist = super.get(type);
            if(exist == null){
                exist = loadProto(type);
                super.put(type, exist);
            }
            return exist;
        }
        private TypeProto loadProto(String type) {
            if (type.charAt(0) == '[') {
                return new ArrayProto(ClassPath.this, type);
            } else {
                return new ClassProto(ClassPath.this, type);
            }
        }
    };


    public ClassDef getClassDef(String type) {
        for (ClassProvider provider: classProviders) {
            ClassDef classDef = provider.getClassDef(type);
            if (classDef != null) {
                return classDef;
            }
        }
        throw new UnresolvedClassException("Could not resolve class %s", type);
    }


    public TypeProto getUnknownClass() {
        return unknownClass;
    }

    public boolean shouldCheckPackagePrivateAccess() {
        return checkPackagePrivateAccess;
    }



    public OdexedFieldInstructionMapper getFieldInstructionMapper() {
        if(mapper == null){
            mapper = new OdexedFieldInstructionMapper(isArt());
        }
        return mapper;
    }
    private OdexedFieldInstructionMapper mapper;
}
