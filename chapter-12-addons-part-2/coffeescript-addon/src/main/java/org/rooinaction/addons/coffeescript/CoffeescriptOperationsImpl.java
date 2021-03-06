package org.rooinaction.addons.coffeescript;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementation of operations this add-on offers.
 * 
 * @since 1.1
 */
@Component
// Use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class CoffeescriptOperationsImpl implements
    CoffeescriptOperations {

  /**
   * MetadataService offers access to Roo's metadata model, use it to retrieve
   * any available metadata by its MID
   */
  @Reference
  private MetadataService metadataService;

  /**
   * Use the PhysicalTypeMetadataProvider to access information about a physical
   * type in the project
   */
  @Reference
  private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

  /**
   * Use ProjectOperations to install new dependencies, plugins, properties, etc
   * into the project configuration
   */
  @Reference
  private ProjectOperations projectOperations;

  /**
   * Use TypeLocationService to find types which are annotated with a given
   * annotation in the project
   */
  @Reference
  private TypeLocationService typeLocationService;

  @Reference
  private FileManager fileManager;

  /** {@inheritDoc} */
  public boolean isSetupCommandAvailable() {
    if (!projectOperations.isFocusedProjectAvailable()
        || !isProjectWar()) {
      return false;
    }

    // check to see whether the Maven dependency is installed already
    return !isCoffeeScriptPluginInstalled();
  }

  public boolean isRemoveCommandAvailable() {
    if (!projectOperations.isFocusedProjectAvailable()
        || !isProjectWar()) {
      return false;
    }

    return isCoffeeScriptPluginInstalled();

  }
	public void setup() {
	 Collection<Plugin> pluginsToAdd = getPluginsFromConfigurationXml();
	 projectOperations.addBuildPlugins(
	   projectOperations.getFocusedModuleName(), pluginsToAdd);
	}
	
  /**
   * provide access to the build plugins in the file, that way if additional
   * plugins are needed, you can just add them to the configuration file.
   * 
   * @return The set of plugins
   */
	private List<Plugin> getPluginsFromConfigurationXml() {
	  Element configuration = XmlUtils.getConfiguration(this.getClass());
	  Collection<Element> configPlugins = XmlUtils.findElements(
	     "/configuration/coffeescript/plugins/plugin",
	     configuration);

	  List<Plugin> plugins = new ArrayList<Plugin>();
	  for (Element pluginXml : configPlugins) {
	    plugins.add(new Plugin(pluginXml));
	  }
	  return plugins;
	}

  public void remove() {
    String moduleName = projectOperations.getFocusedModuleName();
    projectOperations.removeBuildPlugins(moduleName, 
		getPluginsFromConfigurationXml());
  }

  private boolean isCoffeeScriptPluginInstalled() {
    Set<Plugin> pluginsExcludingVersion = projectOperations
        .getFocusedProjectMetadata()
        .getPom()
        .getBuildPluginsExcludingVersion(

            new Plugin("com.theoryinpractise", "coffee-maven-plugin",
                "1.2.0")); // version specified is ignored (tested with 1.4.0 add-on installed)...
    return !pluginsExcludingVersion.isEmpty();
  }

  private boolean isProjectWar() {
    return projectOperations.getFocusedProjectMetadata().getPom()
        .getPackaging().equals("war");
  }
}