package org.github.irengrig.fossil4idea;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/12/13
 * Time: 12:04 PM
 */
@State(
    name = "FossilConfiguration",
    storages = {
        @Storage(file = StoragePathMacros.WORKSPACE_FILE)
    }
)
public class FossilConfiguration implements PersistentStateComponent<Element> {
  public String FOSSIL_PATH;

  @Nullable
  public Element getState() {
    Element element = new Element("state");
    element.setAttribute("FOSSIL_PATH", FOSSIL_PATH);
    return element;
  }

  public void loadState(Element element) {
    final Attribute fossilPath = element.getAttribute("FOSSIL_PATH");
    if (fossilPath != null) {
      FOSSIL_PATH = fossilPath.getValue();
    }
  }

  public static FossilConfiguration getInstance(final Project project) {
    return ServiceManager.getService(project, FossilConfiguration.class);
  }
}
