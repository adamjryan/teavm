/*
 *  Copyright 2015 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.platform.plugin;

import java.lang.annotation.Annotation;
import org.teavm.dependency.DependencyAgent;
import org.teavm.dependency.DependencyConsumer;
import org.teavm.dependency.DependencyListener;
import org.teavm.dependency.DependencyNode;
import org.teavm.dependency.DependencyType;
import org.teavm.dependency.FieldDependency;
import org.teavm.dependency.MethodDependency;
import org.teavm.model.CallLocation;
import org.teavm.model.MethodReference;
import org.teavm.model.ValueType;
import org.teavm.platform.Platform;

/**
 *
 * @author Alexey Andreev
 */
public class AnnotationDependencySupport implements DependencyListener {
    private DependencyNode allClasses;

    @Override
    public void started(DependencyAgent agent) {
        allClasses = agent.createNode();
    }

    @Override
    public void classAchieved(DependencyAgent agent, String className, CallLocation location) {
        allClasses.propagate(agent.getType(className));
    }

    @Override
    public void methodAchieved(final DependencyAgent agent, final MethodDependency method,
            final CallLocation location) {
        if (method.getReference().getClassName().equals(Platform.class.getName()) &&
                method.getReference().getName().equals("getAnnotations")) {
            method.getResult().propagate(agent.getType("[" + Annotation.class.getName()));
            allClasses.addConsumer(new DependencyConsumer() {
                @Override public void consume(DependencyType type) {
                    MethodDependency readMethod = agent.linkMethod(new MethodReference(type.getName(),
                            "$$__readAnnotations__$$", ValueType.parse(Annotation[].class)), location);
                    readMethod.getResult().getArrayItem().connect(method.getResult().getArrayItem());
                    readMethod.use();
                }
            });
        }
    }

    @Override
    public void fieldAchieved(DependencyAgent agent, FieldDependency field, CallLocation location) {
    }
}
