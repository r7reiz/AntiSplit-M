/*
 * Copyright 2014, Google Inc.
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

package org.jf.dexlib2.rewriter;

import org.jf.dexlib2.base.reference.BaseMethodReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.util.collection.ListUtil;

import java.util.List;
import java.util.function.Function;

public class MethodReferenceRewriter implements Rewriter<MethodReference> {
    
    protected final Rewriters rewriters;

    public MethodReferenceRewriter( Rewriters rewriters) {
        this.rewriters = rewriters;
    }

    
    @Override
    public MethodReference rewrite( MethodReference methodReference) {
        return new RewrittenMethodReference(methodReference);
    }

    protected class RewrittenMethodReference extends BaseMethodReference {
         protected MethodReference methodReference;

        public RewrittenMethodReference( MethodReference methodReference) {
            this.methodReference = methodReference;
        }

        @Override  public String getDefiningClass() {
            return rewriters.getTypeRewriter().rewrite(methodReference.getDefiningClass());
        }

        @Override  public String getName() {
            return methodReference.getName();
        }

        @Override  public List<? extends CharSequence> getParameterTypes() {
            return RewriterUtils.rewriteList(rewriters.getTypeRewriter(),
                    ListUtil.transform(methodReference.getParameterTypes(),
                    new Function<CharSequence, String>() {
                        
                        @Override
                        public String apply(CharSequence input) {
                            return input.toString();
                        }
                    }));
        }

        @Override  public String getReturnType() {
            return rewriters.getTypeRewriter().rewrite(methodReference.getReturnType());
        }
    }
}
