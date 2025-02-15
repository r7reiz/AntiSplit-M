/*
 * Copyright 2012, Google Inc.
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

package org.jf.dexlib2.dexbacked;

import org.jf.dexlib2.HiddenApiRestriction;
import org.jf.dexlib2.base.reference.BaseMethodReference;
import org.jf.dexlib2.dexbacked.raw.MethodIdItem;
import org.jf.dexlib2.dexbacked.raw.ProtoIdItem;
import org.jf.dexlib2.dexbacked.raw.TypeListItem;
import org.jf.dexlib2.dexbacked.reference.DexBackedMethodReference;
import org.jf.dexlib2.dexbacked.util.AnnotationsDirectory;
import org.jf.dexlib2.dexbacked.util.FixedSizeList;
import org.jf.dexlib2.dexbacked.util.ParameterIterator;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.util.AbstractForwardSequentialList;
import org.jf.util.collection.EmptyList;
import org.jf.util.collection.EmptySet;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DexBackedMethod extends BaseMethodReference implements Method {

    public final DexBackedDexFile dexFile;

    public final DexBackedClassDef classDef;

    public final int accessFlags;

    private final int codeOffset;
    private final int parameterAnnotationSetListOffset;
    private final int methodAnnotationSetOffset;
    private final int hiddenApiRestrictions;

    public final int methodIndex;
    private final int startOffset;

    private int methodIdItemOffset;
    private int protoIdItemOffset;
    private int parametersOffset = -1;

    public DexBackedMethod( DexBackedDexFile dexFile,
                            DexReader reader,
                            DexBackedClassDef classDef,
                           int previousMethodIndex,
                           int hiddenApiRestrictions) {
        this.dexFile = dexFile;
        this.classDef = classDef;
        startOffset = reader.getOffset();

        // large values may be used for the index delta, which cause the cumulative index to overflow upon
        // addition, effectively allowing out of order entries.
        int methodIndexDiff = reader.readLargeUleb128();
        this.methodIndex = methodIndexDiff + previousMethodIndex;
        this.accessFlags = reader.readSmallUleb128();
        this.codeOffset = reader.readSmallUleb128();
        this.hiddenApiRestrictions = hiddenApiRestrictions;

        this.methodAnnotationSetOffset = 0;
        this.parameterAnnotationSetListOffset = 0;
    }

    public DexBackedMethod( DexBackedDexFile dexFile,
                            DexReader reader,
                            DexBackedClassDef classDef,
                           int previousMethodIndex,
                            AnnotationsDirectory.AnnotationIterator methodAnnotationIterator,
                            AnnotationsDirectory.AnnotationIterator paramaterAnnotationIterator,
                           int hiddenApiRestrictions) {
        this.dexFile = dexFile;
        this.classDef = classDef;
        startOffset = reader.getOffset();

        // large values may be used for the index delta, which cause the cumulative index to overflow upon
        // addition, effectively allowing out of order entries.
        int methodIndexDiff = reader.readLargeUleb128();
        this.methodIndex = methodIndexDiff + previousMethodIndex;
        this.accessFlags = reader.readSmallUleb128();
        this.codeOffset = reader.readSmallUleb128();
        this.hiddenApiRestrictions = hiddenApiRestrictions;

        this.methodAnnotationSetOffset = methodAnnotationIterator.seekTo(methodIndex);
        this.parameterAnnotationSetListOffset = paramaterAnnotationIterator.seekTo(methodIndex);
    }

    public int getMethodIndex() { return methodIndex; }

    @Override
    public String getDefiningClass() { return classDef.getType(); }
    @Override
    public int getAccessFlags() { return accessFlags; }


    @Override
    public String getName() {
        return dexFile.getStringSection().get(
                dexFile.getBuffer().readSmallUint(getMethodIdItemOffset() + MethodIdItem.NAME_OFFSET));
    }


    @Override
    public String getReturnType() {
        return dexFile.getTypeSection().get(
                dexFile.getBuffer().readSmallUint(getProtoIdItemOffset() + ProtoIdItem.RETURN_TYPE_OFFSET));
    }


    @Override
    public List<? extends MethodParameter> getParameters() {
        int parametersOffset = getParametersOffset();
        if (parametersOffset > 0) {
            final List<String> parameterTypes = getParameterTypes();

            return new AbstractForwardSequentialList<MethodParameter>() {
                 @Override public Iterator<MethodParameter> iterator() {
                    return new ParameterIterator(parameterTypes,
                            getParameterAnnotations(),
                            getParameterNames());
                }

                @Override
    public int size() {
                    return parameterTypes.size();
                }
            };
        }
        return EmptyList.of();
    }


    public List<? extends Set<? extends DexBackedAnnotation>> getParameterAnnotations() {
        return AnnotationsDirectory.getParameterAnnotations(dexFile, parameterAnnotationSetListOffset);
    }


    public Iterator<String> getParameterNames() {
        DexBackedMethodImplementation methodImpl = getImplementation();
        if (methodImpl != null) {
            return methodImpl.getParameterNames(null);
        }
        return EmptySet.<String>of().iterator();
    }


    @Override
    public List<String> getParameterTypes() {
        final int parametersOffset = getParametersOffset();
        if (parametersOffset > 0) {
            final int parameterCount = dexFile.getDataBuffer().readSmallUint(parametersOffset + TypeListItem.SIZE_OFFSET);
            final int paramListStart = parametersOffset + TypeListItem.LIST_OFFSET;
            return new FixedSizeList<String>() {

                @Override
                public String readItem(final int index) {
                    return dexFile.getTypeSection().get(dexFile.getDataBuffer().readUshort(paramListStart + 2*index));
                }
                @Override
    public int size() { return parameterCount; }
            };
        }
        return EmptyList.of();
    }


    @Override
    public Set<? extends Annotation> getAnnotations() {
        return AnnotationsDirectory.getAnnotations(dexFile, methodAnnotationSetOffset);
    }


    @Override
    public Set<HiddenApiRestriction> getHiddenApiRestrictions() {
        if (hiddenApiRestrictions == DexBackedClassDef.NO_HIDDEN_API_RESTRICTIONS) {
            return EmptySet.of();
        } else {
            return EnumSet.copyOf(HiddenApiRestriction.getAllFlags(hiddenApiRestrictions));
        }
    }

    
    @Override
    public DexBackedMethodImplementation getImplementation() {
        if (codeOffset > 0) {
            return dexFile.createMethodImplementation(dexFile, this, codeOffset);
        }
        return null;
    }

    private int getMethodIdItemOffset() {
        if (methodIdItemOffset == 0) {
            methodIdItemOffset = dexFile.getMethodSection().getOffset(methodIndex);
        }
        return methodIdItemOffset;
    }

    private int getProtoIdItemOffset() {
        if (protoIdItemOffset == 0) {
            int protoIndex = dexFile.getBuffer().readUshort(getMethodIdItemOffset() + MethodIdItem.PROTO_OFFSET);
            protoIdItemOffset = dexFile.getProtoSection().getOffset(protoIndex);
        }
        return protoIdItemOffset;
    }

    private int getParametersOffset() {
        if (parametersOffset == -1) {
            parametersOffset = dexFile.getBuffer().readSmallUint(
                    getProtoIdItemOffset() + ProtoIdItem.PARAMETERS_OFFSET);
        }
        return parametersOffset;
    }

    /**
     * Skips the reader over the specified number of encoded_method structures
     *
     * @param reader The reader to skip
     * @param count The number of encoded_method structures to skip over
     */
    public static void skipMethods( DexReader reader, int count) {
        for (int i=0; i<count; i++) {
            reader.skipUleb128();
            reader.skipUleb128();
            reader.skipUleb128();
        }
    }

    /**
     * Calculate and return the private size of a method definition.
     *
     * Calculated as: method_idx_diff + access_flags + code_off +
     * implementation size + reference size
     *
     * @return size in bytes
     */
    public int getSize() {
        int size = 0;

        DexReader reader = dexFile.getDataBuffer().readerAt(startOffset);
        reader.readLargeUleb128(); //method_idx_diff
        reader.readSmallUleb128(); //access_flags
        reader.readSmallUleb128(); //code_off
        size += reader.getOffset() - startOffset;

        DexBackedMethodImplementation impl = getImplementation();
        if (impl != null) {
            size += impl.getSize();
        }

        DexBackedMethodReference methodRef = new DexBackedMethodReference(dexFile, methodIndex);
        size += methodRef.getSize();

        return size;
    }
}
