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
package com.intellij.execution;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RefactoringListenerProvider;
import com.intellij.execution.testframework.sm.runner.SMRunnerConsolePropertiesProvider;
import org.jetbrains.annotations.NotNull;

public abstract class JavaTestConfigurationBase extends ModuleBasedConfiguration<JavaRunConfigurationModule>
  implements CommonJavaRunConfigurationParameters, RefactoringListenerProvider, SMRunnerConsolePropertiesProvider {
  public JavaTestConfigurationBase(String name,
                                   @NotNull JavaRunConfigurationModule configurationModule,
                                   @NotNull ConfigurationFactory factory) {
    super(name, configurationModule, factory);
  }

  public JavaTestConfigurationBase(JavaRunConfigurationModule configurationModule,
                                   ConfigurationFactory factory) {
    super(configurationModule, factory);
  }
}
