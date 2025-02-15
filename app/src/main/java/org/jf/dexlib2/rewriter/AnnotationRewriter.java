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

import org.jf.dexlib2.base.BaseAnnotation;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;

import java.util.Set;

public class AnnotationRewriter implements Rewriter<Annotation> {
    
    protected final Rewriters rewriters;

    public AnnotationRewriter( Rewriters rewriters) {
        this.rewriters = rewriters;
    }

    
    @Override
    public Annotation rewrite( Annotation value) {
        return new RewrittenAnnotation(value);
    }

    protected class RewrittenAnnotation extends BaseAnnotation {
         protected Annotation annotation;

        public RewrittenAnnotation( Annotation annotation) {
            this.annotation = annotation;
        }

        @Override public int getVisibility() {
            return annotation.getVisibility();
        }

        @Override  public String getType() {
            return rewriters.getTypeRewriter().rewrite(annotation.getType());
        }

        @Override  public Set<? extends AnnotationElement> getElements() {
            return RewriterUtils.rewriteSet(rewriters.getAnnotationElementRewriter(), annotation.getElements());
        }
    }
}
