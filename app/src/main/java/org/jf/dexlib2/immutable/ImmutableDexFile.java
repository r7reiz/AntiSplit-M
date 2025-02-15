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

package org.jf.dexlib2.immutable;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.extra.DexMarker;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.util.ImmutableUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ImmutableDexFile implements DexFile {

    protected final Set<? extends ImmutableClassDef> classes;

    private final Opcodes opcodes;

    private List<DexMarker> markerList;

    public ImmutableDexFile( Opcodes opcodes,  Collection<? extends ClassDef> classes) {
        this.classes = ImmutableClassDef.immutableSetOf(classes);
        this.opcodes = opcodes;
    }

    public ImmutableDexFile( Opcodes opcodes,  Set<? extends ImmutableClassDef> classes) {
        this.classes = ImmutableUtils.nullToEmptySet(classes);
        this.opcodes = opcodes;
    }

    public static ImmutableDexFile of(DexFile dexFile) {
        if (dexFile instanceof ImmutableDexFile) {
            return (ImmutableDexFile)dexFile;
        }
        ImmutableDexFile immutableDexFile = new ImmutableDexFile(dexFile.getOpcodes(), dexFile.getClasses());
        immutableDexFile.setMarkerList(dexFile.getMarkers());
        return immutableDexFile;
    }

    public void setMarkerList(List<DexMarker> markerList) {
        this.markerList = markerList;
    }

    @Override
    public List<DexMarker> getMarkers() {
        if(this.markerList == null){
            this.markerList = new ArrayList<>();
        }
        return markerList;
    }

    @Override
    public Set<? extends ImmutableClassDef> getClasses() { return classes; }

    @Override
    public Opcodes getOpcodes() { return opcodes; }
}
