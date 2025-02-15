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

package org.jf.dexlib2.writer.builder;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.writer.AnnotationSection;
import org.jf.dexlib2.writer.builder.BuilderEncodedValues.BuilderEncodedValue;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class BuilderAnnotationPool extends BaseBuilderPool implements AnnotationSection<BuilderStringReference,
        BuilderTypeReference, BuilderAnnotation, BuilderAnnotationElement, BuilderEncodedValue> {

    private final ConcurrentMap<Annotation, BuilderAnnotation> internedItems =
            new ConcurrentHashMap<>();

    public BuilderAnnotationPool( DexBuilder dexBuilder) {
        super(dexBuilder);
    }


    public BuilderAnnotation internAnnotation( Annotation annotation) {
        BuilderAnnotation ret = internedItems.get(annotation);
        if (ret != null) {
            return ret;
        }

        BuilderAnnotation dexBuilderAnnotation = new BuilderAnnotation(
                annotation.getVisibility(),
                dexBuilder.typeSection.internType(annotation.getType()),
                dexBuilder.internAnnotationElements(annotation.getElements()));
        ret = internedItems.putIfAbsent(dexBuilderAnnotation, dexBuilderAnnotation);
        return ret==null?dexBuilderAnnotation:ret;
    }

    @Override
    public int getVisibility( BuilderAnnotation key) {
        return key.visibility;
    }


    @Override
    public BuilderTypeReference getType( BuilderAnnotation key) {
        return key.type;
    }


    @Override
    public Collection<? extends BuilderAnnotationElement> getElements( BuilderAnnotation key) {
        return key.elements;
    }


    @Override
    public BuilderStringReference getElementName( BuilderAnnotationElement element) {
        return element.name;
    }


    @Override
    public BuilderEncodedValue getElementValue( BuilderAnnotationElement element) {
        return element.value;
    }

    @Override
    public int getItemOffset( BuilderAnnotation key) {
        return key.offset;
    }


    @Override
    public Collection<? extends Entry<? extends BuilderAnnotation, Integer>> getItems() {
        return new BuilderMapEntryCollection<BuilderAnnotation>(internedItems.values()) {
            @Override protected int getValue( BuilderAnnotation key) {
                return key.offset;
            }

            @Override protected int setValue( BuilderAnnotation key, int value) {
                int prev = key.offset;
                key.offset = value;
                return prev;
            }
        };
    }
}
