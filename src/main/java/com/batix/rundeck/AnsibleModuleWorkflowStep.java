package com.batix.rundeck;

import com.dtolabs.rundeck.core.common.IRundeckProject;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import org.apache.tools.ant.Project;
import java.nio.file.*;

import java.util.Map;

@Plugin(name = AnsibleModuleWorkflowStep.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class AnsibleModuleWorkflowStep implements StepPlugin, Describable {
  public static final String SERVICE_PROVIDER_NAME = "com.batix.rundeck.AnsibleModuleWorkflowStep";

  @Override
  public void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
    String module = (String) configuration.get("module");
    String args = (String) configuration.get("args");
    String extraArgs = (String) configuration.get("extraArgs");
    String sshPass = (String) configuration.get("sshPassword");
    String tempDir = (String) configuration.get("tempDirectory");
    Path tempDirectory = Paths.get(tempDir);
    boolean retainTempDir = (boolean) configuration.get("retainTempDirectory");
    final PluginLogger logger = context.getLogger();

    AnsibleRunner runner = AnsibleRunner.adHoc(module, args).limit(context.getNodes()).extraArgs(extraArgs).sshPass(sshPass).tempDirectory(tempDirectory).retainTempDirectory(retainTempDir);
    
    if ("true".equals(System.getProperty("ansible.debug"))) {
      runner.debug();
    }

    int result;
    try {
        result = runner.run();
    } catch (Exception e) {
        throw new StepException("Error running Ansible.", e, AnsibleFailureReason.AnsibleError);
    }
  }

  @Override
  public Description getDescription() {
    return DescriptionBuilder.builder()
      .name(SERVICE_PROVIDER_NAME)
      .title("Ansible Module")
      .description("Runs an Ansible Module on selected nodes.")
      .property(PropertyUtil.string(
        "module",
        "Module",
        "Module name",
        true,
        null
      ))
      .property(PropertyUtil.string(
        "args",
        "Arguments",
        "Arguments to pass to the module (-a/--args flag)",
        false,
        null
      ))
      .property(PropertyUtil.string(
        "extraArgs",
        "Extra Arguments",
        "Extra Arguments for the Ansible process",
        false,
        null
      ))
      .property(PropertyUtil.string(
        "sshPassword",
        "SSH Password",
        "ssh password passed to ansible job using Private data context.",
        false,
        "option.sshpassword",
        null,
        PropertyScope.Unspecified,
        AnsibleCommon.getRenderParametersForSshPassword()
      ))
      .property(PropertyUtil.string(
        "tempDirectory",
        "Temporary Directory",
        "The directory to execute Ansible",
        false,
        null
      ))
      .property(PropertyUtil.bool(
        "retainTempDirectory",
        "Retain Temporary Directory",
        "Do not delete the temp dir",
        false,
        null
      ))
      .build();
  }
}
