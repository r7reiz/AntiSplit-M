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

package org.jf.dexlib2.immutable.instruction;

import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction21lh;
import org.jf.dexlib2.util.Preconditions;


public class ImmutableInstruction21lh extends ImmutableInstruction implements Instruction21lh {
    public static final Format FORMAT = Format.Format21lh;

    protected final int registerA;
    protected final long literal;

    public ImmutableInstruction21lh( Opcode opcode,
                                    int registerA,
                                    long literal) {
        super(opcode);
        this.registerA = Preconditions.checkByteRegister(registerA);
        this.literal = Preconditions.checkLongHatLiteral(literal);
    }

    public static ImmutableInstruction21lh of(Instruction21lh instruction) {
        if (instruction instanceof ImmutableInstruction21lh) {
            return (ImmutableInstruction21lh)instruction;
        }
        return new ImmutableInstruction21lh(
                instruction.getOpcode(),
                instruction.getRegisterA(),
                instruction.getWideLiteral());
    }

    @Override 
    public int getRegisterA() { return registerA; }
    @Override 
    public long getWideLiteral() { return literal; }
    @Override 
    public short getHatLiteral() { return (short)(literal >>> 48); }

    @Override 
    public Format getFormat() { return FORMAT; }
}
