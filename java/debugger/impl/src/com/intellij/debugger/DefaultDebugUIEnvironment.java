/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.intellij.debugger;

import com.intellij.diagnostic.logging.LogFilesManager;
import com.intellij.diagnostic.logging.OutputFileUtil;
import com.intellij.execution.Executor;
import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RestartAction;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.ide.actions.ContextHelpAction;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: michael.golubev
 */
public class DefaultDebugUIEnvironment implements DebugUIEnvironment {
  private final Project myProject;
  private final ExecutionEnvironment myExecutionEnvironment;
  private final DebugEnvironment myModelEnvironment;

  public DefaultDebugUIEnvironment(Project project,
                                   @NotNull ExecutionEnvironment environment,
                                   RunProfileState state,
                                   RemoteConnection remoteConnection,
                                   boolean pollConnection) {
    myProject = project;
    myExecutionEnvironment = environment;
    myModelEnvironment = new DefaultDebugEnvironment(project,
                                                     environment.getExecutor(),
                                                     RunnerRegistry.getInstance().findRunnerById(environment.getRunnerId()),
                                                     myExecutionEnvironment.getRunProfile(),
                                                     state,
                                                     remoteConnection,
                                                     pollConnection);
  }

  @Override
  public DebugEnvironment getEnvironment() {
    return myModelEnvironment;
  }

  @Nullable
  @Override
  public RunContentDescriptor getReuseContent() {
    return myExecutionEnvironment.getContentToReuse();
  }

  @Override
  public Icon getIcon() {
    return getRunProfile().getIcon();
  }

  @Override
  public void initLogs(RunContentDescriptor content, LogFilesManager logFilesManager) {
    ProcessHandler processHandler = content.getProcessHandler();
    if (getRunProfile() instanceof RunConfigurationBase) {
      RunConfigurationBase runConfiguration = (RunConfigurationBase)getRunProfile();

      logFilesManager.registerFileMatcher(runConfiguration);

      logFilesManager.initLogConsoles(runConfiguration, processHandler);
      OutputFileUtil.attachDumpListener(runConfiguration, processHandler, content.getExecutionConsole());
    }
  }

  @Override
  public void initActions(RunContentDescriptor content, DefaultActionGroup actionGroup) {
    Executor executor = myExecutionEnvironment.getExecutor();
    RestartAction restartAction = new RestartAction(executor, content, myExecutionEnvironment);
    actionGroup.add(restartAction, Constraints.FIRST);
    restartAction.registerShortcut(content.getComponent());

    actionGroup.add(new CloseAction(executor, content, myProject));
    actionGroup.add(new ContextHelpAction(executor.getHelpId()));
  }

  @Override
  @NotNull
  public RunProfile getRunProfile() {
    return myExecutionEnvironment.getRunProfile();
  }
}
