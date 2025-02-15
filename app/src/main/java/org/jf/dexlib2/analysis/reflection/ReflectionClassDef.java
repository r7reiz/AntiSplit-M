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

package org.jf.dexlib2.analysis.reflection;

import org.jf.dexlib2.analysis.reflection.util.ReflectionUtils;
import org.jf.dexlib2.base.reference.BaseTypeReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.util.collection.CombiningIterator;
import org.jf.util.collection.EmptySet;
import org.jf.util.collection.Iterables;
import org.jf.util.collection.ListUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Wraps a ClassDef around a class loaded in the current VM
 *
 * Only supports the basic information exposed by ClassProto
 */
public class ReflectionClassDef extends BaseTypeReference implements ClassDef {
    private final Class cls;

    public ReflectionClassDef(Class cls) {
        this.cls = cls;
    }

    @Override 
    public int getAccessFlags() {
        // the java modifiers appear to be the same as the dex access flags
        return cls.getModifiers();
    }

    
    @Override
    public String getSuperclass() {
        if (Modifier.isInterface(cls.getModifiers())) {
            return "Ljava/lang/Object;";
        }
        Class superClass = cls.getSuperclass();
        if (superClass == null) {
            return null;
        }
        return ReflectionUtils.javaToDexName(superClass.getName());
    }


    @Override
    public List<String> getInterfaces() {
        return ListUtil.copyOf(Iterables.transform(Iterables.forArray(cls.getInterfaces()), new Function<Class, String>() {
            
            @Override
            public String apply( Class input) {
                if (input == null) {
                    return null;
                }
                return ReflectionUtils.javaToDexName(input.getName());
            }
        }));
    }

    
    @Override
    public String getSourceFile() {
        return null;
    }


    @Override
    public Set<? extends Annotation> getAnnotations() {
        return EmptySet.of();
    }


    @Override
    public Iterable<? extends Field> getStaticFields() {
        return new Iterable<Field>() {
             @Override public Iterator<Field> iterator() {
                Iterator<java.lang.reflect.Field> staticFields = Iterables.filter(
                        Iterables.forArray(cls.getDeclaredFields()),
                        new Predicate<java.lang.reflect.Field>() {
                            @Override
                            public boolean test( java.lang.reflect.Field input) {
                                return input!=null && Modifier.isStatic(input.getModifiers());
                            }
                        });

                return Iterables.transform(staticFields,
                        new Function<java.lang.reflect.Field, Field>() {
                            
                            @Override
                            public Field apply( java.lang.reflect.Field input) {
                                return new ReflectionField(input);
                            }
                        }
                );
            }
        };
    }


    @Override
    public Iterable<? extends Field> getInstanceFields() {
        return new Iterable<Field>() {
             @Override public Iterator<Field> iterator() {
                Iterator<java.lang.reflect.Field> staticFields = Iterables.filter(
                        Iterables.forArray(cls.getDeclaredFields()),
                        new Predicate<java.lang.reflect.Field>() {
                            @Override
                            public
                            boolean test( java.lang.reflect.Field input) {
                                return input!=null && !Modifier.isStatic(input.getModifiers());
                            }
                        });

                return Iterables.transform(staticFields,
                        new Function<java.lang.reflect.Field, Field>() {
                             @Override public Field apply( java.lang.reflect.Field input) {
                                return new ReflectionField(input);
                            }
                        }
                );
            }
        };
    }


    @Override
    public Set<? extends Field> getFields() {
        return new AbstractSet<Field>() {
             @Override public Iterator<Field> iterator() {
                return Iterables.transform(Iterables.forArray(cls.getDeclaredFields()),
                        new Function<java.lang.reflect.Field, Field>() {
                             @Override public Field apply( java.lang.reflect.Field input) {
                                return new ReflectionField(input);
                            }
                        });
            }

            @Override public int size() {
                return cls.getDeclaredFields().length;
            }
        };
    }

    private static final int DIRECT_MODIFIERS = Modifier.PRIVATE | Modifier.STATIC;

    @Override
    public Iterable<? extends Method> getDirectMethods() {
        return new Iterable<Method>() {
             @Override public Iterator<Method> iterator() {
                Iterator<Method> constructorIterator =
                        Iterables.transform(Iterables.forArray(cls.getDeclaredConstructors()),
                                new Function<Constructor, Method>() {
                                     @Override public Method apply( Constructor input) {
                                        return new ReflectionConstructor(input);
                                    }
                                });

                Iterator<java.lang.reflect.Method> directMethods = Iterables.filter(
                        Iterables.forArray(cls.getDeclaredMethods()),
                        new Predicate<java.lang.reflect.Method>() {
                            @Override
                            public boolean test( java.lang.reflect.Method input) {
                                return input != null && (input.getModifiers() & DIRECT_MODIFIERS) != 0;
                            }
                        });

                Iterator<Method> methodIterator = Iterables.transform(directMethods,
                        new Function<java.lang.reflect.Method, Method>() {
                             @Override public Method apply( java.lang.reflect.Method input) {
                                return new ReflectionMethod(input);
                            }
                        });
                return new CombiningIterator<Method>(constructorIterator, methodIterator);
            }
        };
    }


    @Override
    public Iterable<? extends Method> getVirtualMethods() {
        return new Iterable<Method>() {
             @Override public Iterator<Method> iterator() {
                Iterator<java.lang.reflect.Method> directMethods = Iterables.filter(
                        Iterables.forArray(cls.getDeclaredMethods()),
                        new Predicate<java.lang.reflect.Method>() {
                            @Override
                            public boolean test( java.lang.reflect.Method input) {
                                return input != null && (input.getModifiers() & DIRECT_MODIFIERS) == 0;
                            }
                        });

                return Iterables.transform(directMethods,
                        new Function<java.lang.reflect.Method, Method>() {
                             @Override public Method apply( java.lang.reflect.Method input) {
                                return new ReflectionMethod(input);
                            }
                        });
            }
        };
    }


    @Override
    public Set<? extends Method> getMethods() {
        return new AbstractSet<Method>() {
             @Override public Iterator<Method> iterator() {
                Iterator<? extends Method> constructorIterator =
                        Iterables.transform(Iterables.forArray(cls.getDeclaredConstructors()),
                                new Function<Constructor, Method>() {
                                    
                                    @Override public Method apply( Constructor input) {
                                        return new ReflectionConstructor(input);
                                    }
                                });

                Iterator<Method> methodIterator =
                        Iterables.transform(Iterables.forArray(cls.getDeclaredMethods()),
                                new Function<java.lang.reflect.Method, Method>() {
                                    
                                    @Override
                                    public Method apply( java.lang.reflect.Method input) {
                                        return new ReflectionMethod(input);
                                    }
                                });
                return new CombiningIterator<Method>(constructorIterator, methodIterator);
            }

            @Override public int size() {
                return cls.getDeclaredMethods().length + cls.getDeclaredConstructors().length;
            }
        };
    }


    @Override
    public String getType() {
        return ReflectionUtils.javaToDexName(cls.getName());
    }
}
