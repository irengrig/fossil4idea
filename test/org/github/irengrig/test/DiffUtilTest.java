package org.github.irengrig.test;

import org.github.irengrig.fossil4idea.local.DiffUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Irina.Chernushina on 5/31/2014.
 */
public class DiffUtilTest {
  @Test
  public void testSimpleRename() throws Exception {
    final String original = "package com.something;\n" +
            "\n" +
            "/**\n" +
            " * Created by Irina.Chernushina on 5/29/2014.\n" +
            " */\n" +
            "public class Test3 {//\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"***\");\n" +
            "    }\n" +
            "}\n";
    final String changed = "package com.something;\n" +
            "\n" +
            "/**\n" +
            " * Created by Irina.Chernushina on 5/29/2014.\n" +
            " */\n" +
            "public class Test4 {//\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"***\");\n" +
            "    }\n" +
            "}\n";
    final String patch = "Index: src/com/something/Test4.java\n" +
            "==================================================================\n" +
            "--- src/com/something/Test4.java\n" +
            "+++ src/com/something/Test4.java\n" +
            "@@ -1,11 +1,11 @@\n" +
            " package com.something;\n" +
            "\n" +
            " /**\n" +
            "  * Created by Irina.Chernushina on 5/29/2014.\n" +
            "  */\n" +
            "-public class Test3 {//\n" +
            "+public class Test4 {//\n" +
            "\n" +
            "     public static void main(String[] args) {\n" +
            "         System.out.println(\"***\");\n" +
            "     }\n" +
            " }";
    final String test = new DiffUtil().execute(changed, patch, "test");
    Assert.assertEquals(original, test);
  }
  @Test
  public void testSimpleModification() throws Exception {
    final String original = "/*\n" +
            " * Copyright 2000-2013 JetBrains s.r.o.\n" +
            " *\n" +
            " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
            " * you may not use this file except in compliance with the License.\n" +
            " * You may obtain a copy of the License at\n" +
            " *\n" +
            " * http://www.apache.org/licenses/LICENSE-2.0\n" +
            " *\n" +
            " * Unless required by applicable law or agreed to in writing, software\n" +
            " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
            " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
            " * See the License for the specific language governing permissions and\n" +
            " * limitations under the License.\n" +
            " */\n" +
            "package com.jetbrains.python.psi.impl;\n" +
            "\n" +
            "import com.intellij.openapi.application.Application;\n" +
            "import com.intellij.openapi.application.ApplicationManager;\n" +
            "import com.intellij.openapi.fileTypes.FileTypeManager;\n" +
            "import com.intellij.openapi.module.Module;\n" +
            "import com.intellij.openapi.module.ModuleManager;\n" +
            "import com.intellij.openapi.module.ModuleUtil;\n" +
            "import com.intellij.openapi.project.Project;\n" +
            "import com.intellij.openapi.projectRoots.Sdk;\n" +
            "import com.intellij.openapi.roots.*;\n" +
            "import com.intellij.openapi.roots.impl.FilePropertyPusher;\n" +
            "import com.intellij.openapi.roots.impl.PushedFilePropertiesUpdater;\n" +
            "import com.intellij.openapi.util.Key;\n" +
            "import com.intellij.openapi.vfs.VfsUtilCore;\n" +
            "import com.intellij.openapi.vfs.VirtualFile;\n" +
            "import com.intellij.openapi.vfs.VirtualFileVisitor;\n" +
            "import com.intellij.openapi.vfs.newvfs.FileAttribute;\n" +
            "import com.intellij.psi.SingleRootFileViewProvider;\n" +
            "import com.intellij.util.FileContentUtil;\n" +
            "import com.intellij.util.containers.WeakHashMap;\n" +
            "import com.intellij.util.io.DataInputOutputUtil;\n" +
            "import com.intellij.util.messages.MessageBus;\n" +
            "import com.jetbrains.python.PythonFileType;\n" +
            "import com.jetbrains.python.psi.LanguageLevel;\n" +
            "import com.jetbrains.python.sdk.PythonSdkType;\n" +
            "import org.jetbrains.annotations.NotNull;\n" +
            "import org.jetbrains.annotations.Nullable;\n" +
            "\n" +
            "import java.io.DataInputStream;\n" +
            "import java.io.DataOutputStream;\n" +
            "import java.io.IOException;\n" +
            "import java.util.*;\n" +
            "\n" +
            "/**\n" +
            " * @author yole\n" +
            " */\n" +
            "public class PythonLanguageLevelPusher implements FilePropertyPusher<LanguageLevel> {\n" +
            "  private final Map<Module, Sdk> myModuleSdks = new WeakHashMap<Module, Sdk>();\n" +
            "\n" +
            "  public static void pushLanguageLevel(final Project project) {\n" +
            "    PushedFilePropertiesUpdater.getInstance(project).pushAll(new PythonLanguageLevelPusher());\n" +
            "  }\n" +
            "\n" +
            "  public void initExtra(@NotNull Project project, @NotNull MessageBus bus, @NotNull Engine languageLevelUpdater) {\n" +
            "    final Module[] modules = ModuleManager.getInstance(project).getModules();\n" +
            "    Set<Sdk> usedSdks = new HashSet<Sdk>();\n" +
            "    for (Module module : modules) {\n" +
            "      final Sdk sdk = PythonSdkType.findPythonSdk(module);\n" +
            "      myModuleSdks.put(module, sdk);\n" +
            "      if (sdk != null && !usedSdks.contains(sdk)) {\n" +
            "        usedSdks.add(sdk);\n" +
            "        updateSdkLanguageLevel(project, sdk);\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  @NotNull\n" +
            "  public Key<LanguageLevel> getFileDataKey() {\n" +
            "    return LanguageLevel.KEY;\n" +
            "  }\n" +
            "\n" +
            "  public boolean pushDirectoriesOnly() {\n" +
            "    return true;\n" +
            "  }\n" +
            "\n" +
            "  @NotNull\n" +
            "  public LanguageLevel getDefaultValue() {\n" +
            "    return LanguageLevel.getDefault();\n" +
            "  }\n" +
            "\n" +
            "  public LanguageLevel getImmediateValue(@NotNull Project project, @Nullable VirtualFile file) {\n" +
            "    if (ApplicationManager.getApplication().isUnitTestMode() && LanguageLevel.FORCE_LANGUAGE_LEVEL != null) {\n" +
            "      return LanguageLevel.FORCE_LANGUAGE_LEVEL;\n" +
            "    }\n" +
            "    if (file == null) return null;\n" +
            "\n" +
            "    final Module module = ModuleUtil.findModuleForFile(file, project);\n" +
            "    if (module != null) {\n" +
            "      return getImmediateValue(module);\n" +
            "    }\n" +
            "    final Sdk sdk = findSdk(project, file);\n" +
            "    if (sdk != null) {\n" +
            "      return PythonSdkType.getLanguageLevelForSdk(sdk);\n" +
            "    }\n" +
            "    return null;\n" +
            "  }\n" +
            "\n" +
            "  @Nullable\n" +
            "  private static Sdk findSdk(Project project, VirtualFile file) {\n" +
            "    if (file != null) {\n" +
            "      final List<OrderEntry> orderEntries = ProjectRootManager.getInstance(project).getFileIndex().getOrderEntriesForFile(file);\n" +
            "      for (OrderEntry orderEntry : orderEntries) {\n" +
            "        if (orderEntry instanceof JdkOrderEntry) {\n" +
            "          return ((JdkOrderEntry)orderEntry).getJdk();\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "    return null;\n" +
            "  }\n" +
            "\n" +
            "  public LanguageLevel getImmediateValue(@NotNull Module module) {\n" +
            "    if (ApplicationManager.getApplication().isUnitTestMode() && LanguageLevel.FORCE_LANGUAGE_LEVEL != null) {\n" +
            "      return LanguageLevel.FORCE_LANGUAGE_LEVEL;\n" +
            "    }\n" +
            "\n" +
            "    final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();\n" +
            "    return PythonSdkType.getLanguageLevelForSdk(sdk);\n" +
            "  }\n" +
            "\n" +
            "  public boolean acceptsFile(@NotNull VirtualFile file) {\n" +
            "    return false;\n" +
            "  }\n" +
            "\n" +
            "  @Override\n" +
            "  public boolean acceptsDirectory(@NotNull VirtualFile file, @NotNull Project project) {\n" +
            "    return true;\n" +
            "  }\n" +
            "\n" +
            "  private static final FileAttribute PERSISTENCE = new FileAttribute(\"python_language_level_persistence\", 2, true);\n" +
            "\n" +
            "  public void persistAttribute(@NotNull VirtualFile fileOrDir, @NotNull LanguageLevel level) throws IOException {\n" +
            "    final DataInputStream iStream = PERSISTENCE.readAttribute(fileOrDir);\n" +
            "    if (iStream != null) {\n" +
            "      try {\n" +
            "        final int oldLevelOrdinal = DataInputOutputUtil.readINT(iStream);\n" +
            "        if (oldLevelOrdinal == level.ordinal()) return;\n" +
            "      }\n" +
            "      finally {\n" +
            "        iStream.close();\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    final DataOutputStream oStream = PERSISTENCE.writeAttribute(fileOrDir);\n" +
            "    DataInputOutputUtil.writeINT(oStream, level.ordinal());\n" +
            "    oStream.close();\n" +
            "\n" +
            "    for (VirtualFile child : fileOrDir.getChildren()) {\n" +
            "      if (!child.isDirectory() && PythonFileType.INSTANCE.equals(child.getFileType())) {\n" +
            "        PushedFilePropertiesUpdater.filePropertiesChanged(child);\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  public void afterRootsChanged(@NotNull final Project project) {\n" +
            "    Set<Sdk> updatedSdks = new HashSet<Sdk>();\n" +
            "    final Module[] modules = ModuleManager.getInstance(project).getModules();\n" +
            "    boolean needReparseOpenFiles = false;\n" +
            "    for (Module module : modules) {\n" +
            "      Sdk newSdk = PythonSdkType.findPythonSdk(module);\n" +
            "      if (myModuleSdks.containsKey(module)) {\n" +
            "        Sdk oldSdk = myModuleSdks.get(module);\n" +
            "        if ((newSdk != null || oldSdk != null) && newSdk != oldSdk) {\n" +
            "          needReparseOpenFiles = true;\n" +
            "        }\n" +
            "      }\n" +
            "      myModuleSdks.put(module, newSdk);\n" +
            "      if (newSdk != null && !updatedSdks.contains(newSdk)) {\n" +
            "        updatedSdks.add(newSdk);\n" +
            "        updateSdkLanguageLevel(project, newSdk);\n" +
            "      }\n" +
            "    }\n" +
            "    if (needReparseOpenFiles) {\n" +
            "      FileContentUtil.reparseFiles(project, Collections.<VirtualFile>emptyList(), true);\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  private void updateSdkLanguageLevel(final Project project, final Sdk sdk) {\n" +
            "    final LanguageLevel languageLevel = PythonSdkType.getLanguageLevelForSdk(sdk);\n" +
            "    final VirtualFile[] files = sdk.getRootProvider().getFiles(OrderRootType.CLASSES);\n" +
            "    final Application application = ApplicationManager.getApplication();\n" +
            "    application.executeOnPooledThread(new Runnable() {\n" +
            "      @Override\n" +
            "      public void run() {\n" +
            "        application.runReadAction(new Runnable() {\n" +
            "          @Override\n" +
            "          public void run() {\n" +
            "            if (project != null && project.isDisposed()) {\n" +
            "              return;\n" +
            "            }\n" +
            "            for (VirtualFile file : files) {\n" +
            "              if (file.isValid()) {\n" +
            "                VirtualFile parent = file.getParent();\n" +
            "                boolean suppressSizeLimit = false;\n" +
            "                if (parent != null && parent.getName().equals(PythonSdkType.SKELETON_DIR_NAME)) {\n" +
            "                  suppressSizeLimit = true;\n" +
            "                }\n" +
            "                markRecursively(project, file, languageLevel, suppressSizeLimit);\n" +
            "              }\n" +
            "            }\n" +
            "          }\n" +
            "        });\n" +
            "      }\n" +
            "    });\n" +
            "  }\n" +
            "\n" +
            "  private void markRecursively(final Project project,\n" +
            "                               @NotNull final VirtualFile file,\n" +
            "                               final LanguageLevel languageLevel,\n" +
            "                               final boolean suppressSizeLimit) {\n" +
            "    final FileTypeManager fileTypeManager = FileTypeManager.getInstance();\n" +
            "    VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor() {\n" +
            "      @Override\n" +
            "      public boolean visitFile(@NotNull VirtualFile file) {\n" +
            "        if (fileTypeManager.isFileIgnored(file)) {\n" +
            "          return false;\n" +
            "        }\n" +
            "        if (file.isDirectory()) {\n" +
            "          PushedFilePropertiesUpdater.findAndUpdateValue(project, file, PythonLanguageLevelPusher.this, languageLevel);\n" +
            "        }\n" +
            "        if (suppressSizeLimit) {\n" +
            "          SingleRootFileViewProvider.doNotCheckFileSizeLimit(file);\n" +
            "        }\n" +
            "        return true;\n" +
            "      }\n" +
            "    });\n" +
            "  }\n" +
            "\n" +
            "  public static void setForcedLanguageLevel(final Project project, @Nullable LanguageLevel languageLevel) {\n" +
            "    LanguageLevel.FORCE_LANGUAGE_LEVEL = languageLevel;\n" +
            "    pushLanguageLevel(project);\n" +
            "  }\n" +
            "}\n";
    final String changed = "/*\n" +
            " * Copyright 2000-2013 JetBrains s.r.o.\n" +
            " *\n" +
            " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
            " * you may not use this file except in compliance with the License.\n" +
            " * You may obtain a copy of the License at\n" +
            " *\n" +
            " * http://www.apache.org/licenses/LICENSE-2.0\n" +
            " *\n" +
            " * Unless required by applicable law or agreed to in writing, software\n" +
            " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
            " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
            " * See the License for the specific language governing permissions and\n" +
            " * limitations under the License.\n" +
            " */\n" +
            "package com.jetbrains.python.psi.impl;\n" +
            "\n" +
            "import com.intellij.openapi.application.Application;\n" +
            "import com.intellij.openapi.application.ApplicationManager;\n" +
            "import com.intellij.openapi.fileTypes.FileTypeManager;\n" +
            "import com.intellij.openapi.module.Module;\n" +
            "import com.intellij.openapi.module.ModuleManager;\n" +
            "import com.intellij.openapi.module.ModuleUtilCore;\n" +
            "import com.intellij.openapi.project.Project;\n" +
            "import com.intellij.openapi.projectRoots.Sdk;\n" +
            "import com.intellij.openapi.roots.*;\n" +
            "import com.intellij.openapi.roots.impl.FilePropertyPusher;\n" +
            "import com.intellij.openapi.roots.impl.PushedFilePropertiesUpdater;\n" +
            "import com.intellij.openapi.util.Key;\n" +
            "import com.intellij.openapi.vfs.VfsUtilCore;\n" +
            "import com.intellij.openapi.vfs.VirtualFile;\n" +
            "import com.intellij.openapi.vfs.VirtualFileVisitor;\n" +
            "import com.intellij.openapi.vfs.newvfs.FileAttribute;\n" +
            "import com.intellij.psi.SingleRootFileViewProvider;\n" +
            "import com.intellij.util.FileContentUtil;\n" +
            "import com.intellij.util.containers.WeakHashMap;\n" +
            "import com.intellij.util.io.DataInputOutputUtil;\n" +
            "import com.intellij.util.messages.MessageBus;\n" +
            "import com.jetbrains.python.PythonFileType;\n" +
            "import com.jetbrains.python.psi.LanguageLevel;\n" +
            "import com.jetbrains.python.sdk.PythonSdkType;\n" +
            "import org.jetbrains.annotations.NotNull;\n" +
            "import org.jetbrains.annotations.Nullable;\n" +
            "\n" +
            "import java.io.DataInputStream;\n" +
            "import java.io.DataOutputStream;\n" +
            "import java.io.IOException;\n" +
            "import java.util.*;\n" +
            "\n" +
            "/**\n" +
            " * @author yole\n" +
            " */\n" +
            "public class PythonLanguageLevelPusher implements FilePropertyPusher<LanguageLevel> {\n" +
            "  private final Map<Module, Sdk> myModuleSdks = new WeakHashMap<Module, Sdk>();\n" +
            "\n" +
            "  public static void pushLanguageLevel(final Project project) {\n" +
            "    PushedFilePropertiesUpdater.getInstance(project).pushAll(new PythonLanguageLevelPusher());\n" +
            "  }\n" +
            "\n" +
            "  public void initExtra(@NotNull Project project, @NotNull MessageBus bus, @NotNull Engine languageLevelUpdater) {\n" +
            "    final Module[] modules = ModuleManager.getInstance(project).getModules();\n" +
            "    Set<Sdk> usedSdks = new HashSet<Sdk>();\n" +
            "    for (Module module : modules) {\n" +
            "      final Sdk sdk = PythonSdkType.findPythonSdk(module);\n" +
            "      myModuleSdks.put(module, sdk);\n" +
            "      if (sdk != null && !usedSdks.contains(sdk)) {\n" +
            "        usedSdks.add(sdk);\n" +
            "        updateSdkLanguageLevel(project, sdk);\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  @NotNull\n" +
            "  public Key<LanguageLevel> getFileDataKey() {\n" +
            "    return LanguageLevel.KEY;\n" +
            "  }\n" +
            "\n" +
            "  public boolean pushDirectoriesOnly() {\n" +
            "    return true;\n" +
            "  }\n" +
            "\n" +
            "  @NotNull\n" +
            "  public LanguageLevel getDefaultValue() {\n" +
            "    return LanguageLevel.getDefault();\n" +
            "  }\n" +
            "\n" +
            "  public LanguageLevel getImmediateValue(@NotNull Project project, @Nullable VirtualFile file) {\n" +
            "    return getFileLanguageLevel(project, file);\n" +
            "  }\n" +
            "\n" +
            "  public static LanguageLevel getFileLanguageLevel(Project project, VirtualFile file) {\n" +
            "    if (ApplicationManager.getApplication().isUnitTestMode() && LanguageLevel.FORCE_LANGUAGE_LEVEL != null) {\n" +
            "      return LanguageLevel.FORCE_LANGUAGE_LEVEL;\n" +
            "    }\n" +
            "    if (file == null) return null;\n" +
            "\n" +
            "    final Module module = ModuleUtilCore.findModuleForFile(file, project);\n" +
            "    if (module != null) {\n" +
            "      final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();\n" +
            "      return PythonSdkType.getLanguageLevelForSdk(sdk);\n" +
            "    }\n" +
            "    final Sdk sdk = findSdk(project, file);\n" +
            "    if (sdk != null) {\n" +
            "      return PythonSdkType.getLanguageLevelForSdk(sdk);\n" +
            "    }\n" +
            "    return null;\n" +
            "  }\n" +
            "\n" +
            "  @Nullable\n" +
            "  private static Sdk findSdk(Project project, VirtualFile file) {\n" +
            "    if (file != null) {\n" +
            "      final List<OrderEntry> orderEntries = ProjectRootManager.getInstance(project).getFileIndex().getOrderEntriesForFile(file);\n" +
            "      for (OrderEntry orderEntry : orderEntries) {\n" +
            "        if (orderEntry instanceof JdkOrderEntry) {\n" +
            "          return ((JdkOrderEntry)orderEntry).getJdk();\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "    return null;\n" +
            "  }\n" +
            "\n" +
            "  public LanguageLevel getImmediateValue(@NotNull Module module) {\n" +
            "    if (ApplicationManager.getApplication().isUnitTestMode() && LanguageLevel.FORCE_LANGUAGE_LEVEL != null) {\n" +
            "      return LanguageLevel.FORCE_LANGUAGE_LEVEL;\n" +
            "    }\n" +
            "\n" +
            "    final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();\n" +
            "    return PythonSdkType.getLanguageLevelForSdk(sdk);\n" +
            "  }\n" +
            "\n" +
            "  public boolean acceptsFile(@NotNull VirtualFile file) {\n" +
            "    return false;\n" +
            "  }\n" +
            "\n" +
            "  @Override\n" +
            "  public boolean acceptsDirectory(@NotNull VirtualFile file, @NotNull Project project) {\n" +
            "    return true;\n" +
            "  }\n" +
            "\n" +
            "  private static final FileAttribute PERSISTENCE = new FileAttribute(\"python_language_level_persistence\", 2, true);\n" +
            "\n" +
            "  public void persistAttribute(@NotNull VirtualFile fileOrDir, @NotNull LanguageLevel level) throws IOException {\n" +
            "    final DataInputStream iStream = PERSISTENCE.readAttribute(fileOrDir);\n" +
            "    if (iStream != null) {\n" +
            "      try {\n" +
            "        final int oldLevelOrdinal = DataInputOutputUtil.readINT(iStream);\n" +
            "        if (oldLevelOrdinal == level.ordinal()) return;\n" +
            "      }\n" +
            "      finally {\n" +
            "        iStream.close();\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    final DataOutputStream oStream = PERSISTENCE.writeAttribute(fileOrDir);\n" +
            "    DataInputOutputUtil.writeINT(oStream, level.ordinal());\n" +
            "    oStream.close();\n" +
            "\n" +
            "    for (VirtualFile child : fileOrDir.getChildren()) {\n" +
            "      if (!child.isDirectory() && PythonFileType.INSTANCE.equals(child.getFileType())) {\n" +
            "        PushedFilePropertiesUpdater.filePropertiesChanged(child);\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  public void afterRootsChanged(@NotNull final Project project) {\n" +
            "    Set<Sdk> updatedSdks = new HashSet<Sdk>();\n" +
            "    final Module[] modules = ModuleManager.getInstance(project).getModules();\n" +
            "    boolean needReparseOpenFiles = false;\n" +
            "    for (Module module : modules) {\n" +
            "      Sdk newSdk = PythonSdkType.findPythonSdk(module);\n" +
            "      if (myModuleSdks.containsKey(module)) {\n" +
            "        Sdk oldSdk = myModuleSdks.get(module);\n" +
            "        if ((newSdk != null || oldSdk != null) && newSdk != oldSdk) {\n" +
            "          needReparseOpenFiles = true;\n" +
            "        }\n" +
            "      }\n" +
            "      myModuleSdks.put(module, newSdk);\n" +
            "      if (newSdk != null && !updatedSdks.contains(newSdk)) {\n" +
            "        updatedSdks.add(newSdk);\n" +
            "        updateSdkLanguageLevel(project, newSdk);\n" +
            "      }\n" +
            "    }\n" +
            "    if (needReparseOpenFiles) {\n" +
            "      FileContentUtil.reparseFiles(project, Collections.<VirtualFile>emptyList(), true);\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  private void updateSdkLanguageLevel(final Project project, final Sdk sdk) {\n" +
            "    final LanguageLevel languageLevel = PythonSdkType.getLanguageLevelForSdk(sdk);\n" +
            "    final VirtualFile[] files = sdk.getRootProvider().getFiles(OrderRootType.CLASSES);\n" +
            "    final Application application = ApplicationManager.getApplication();\n" +
            "    application.executeOnPooledThread(new Runnable() {\n" +
            "      @Override\n" +
            "      public void run() {\n" +
            "        application.runReadAction(new Runnable() {\n" +
            "          @Override\n" +
            "          public void run() {\n" +
            "            if (project != null && project.isDisposed()) {\n" +
            "              return;\n" +
            "            }\n" +
            "            for (VirtualFile file : files) {\n" +
            "              if (file.isValid()) {\n" +
            "                VirtualFile parent = file.getParent();\n" +
            "                boolean suppressSizeLimit = false;\n" +
            "                if (parent != null && parent.getName().equals(PythonSdkType.SKELETON_DIR_NAME)) {\n" +
            "                  suppressSizeLimit = true;\n" +
            "                }\n" +
            "                markRecursively(project, file, languageLevel, suppressSizeLimit);\n" +
            "              }\n" +
            "            }\n" +
            "          }\n" +
            "        });\n" +
            "      }\n" +
            "    });\n" +
            "  }\n" +
            "\n" +
            "  private void markRecursively(final Project project,\n" +
            "                               @NotNull final VirtualFile file,\n" +
            "                               final LanguageLevel languageLevel,\n" +
            "                               final boolean suppressSizeLimit) {\n" +
            "    final FileTypeManager fileTypeManager = FileTypeManager.getInstance();\n" +
            "    VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor() {\n" +
            "      @Override\n" +
            "      public boolean visitFile(@NotNull VirtualFile file) {\n" +
            "        if (fileTypeManager.isFileIgnored(file)) {\n" +
            "          return false;\n" +
            "        }\n" +
            "        if (file.isDirectory()) {\n" +
            "          PushedFilePropertiesUpdater.findAndUpdateValue(project, file, PythonLanguageLevelPusher.this, languageLevel);\n" +
            "        }\n" +
            "        if (suppressSizeLimit) {\n" +
            "          SingleRootFileViewProvider.doNotCheckFileSizeLimit(file);\n" +
            "        }\n" +
            "        return true;\n" +
            "      }\n" +
            "    });\n" +
            "  }\n" +
            "\n" +
            "  public static void setForcedLanguageLevel(final Project project, @Nullable LanguageLevel languageLevel) {\n" +
            "    LanguageLevel.FORCE_LANGUAGE_LEVEL = languageLevel;\n" +
            "    pushLanguageLevel(project);\n" +
            "  }\n" +
            "}\n";
    final String patch = "Index: src/com/something/zzz.txt\n" +
            "==================================================================\n" +
            "--- src/com/something/zzz.txt\n" +
            "+++ src/com/something/zzz.txt\n" +
            "@@ -18,11 +18,11 @@\n" +
            " import com.intellij.openapi.application.Application;\n" +
            " import com.intellij.openapi.application.ApplicationManager;\n" +
            " import com.intellij.openapi.fileTypes.FileTypeManager;\n" +
            " import com.intellij.openapi.module.Module;\n" +
            " import com.intellij.openapi.module.ModuleManager;\n" +
            "-import com.intellij.openapi.module.ModuleUtil;\n" +
            "+import com.intellij.openapi.module.ModuleUtilCore;\n" +
            " import com.intellij.openapi.project.Project;\n" +
            " import com.intellij.openapi.projectRoots.Sdk;\n" +
            " import com.intellij.openapi.roots.*;\n" +
            " import com.intellij.openapi.roots.impl.FilePropertyPusher;\n" +
            " import com.intellij.openapi.roots.impl.PushedFilePropertiesUpdater;\n" +
            "@@ -83,18 +83,23 @@\n" +
            "   public LanguageLevel getDefaultValue() {\n" +
            "     return LanguageLevel.getDefault();\n" +
            "   }\n" +
            "\n" +
            "   public LanguageLevel getImmediateValue(@NotNull Project project, @Nullable VirtualFile file) {\n" +
            "+    return getFileLanguageLevel(project, file);\n" +
            "+  }\n" +
            "+\n" +
            "+  public static LanguageLevel getFileLanguageLevel(Project project, VirtualFile file) {\n" +
            "     if (ApplicationManager.getApplication().isUnitTestMode() && LanguageLevel.FORCE_LANGUAGE_LEVEL != null) {\n" +
            "       return LanguageLevel.FORCE_LANGUAGE_LEVEL;\n" +
            "     }\n" +
            "     if (file == null) return null;\n" +
            "\n" +
            "-    final Module module = ModuleUtil.findModuleForFile(file, project);\n" +
            "+    final Module module = ModuleUtilCore.findModuleForFile(file, project);\n" +
            "     if (module != null) {\n" +
            "-      return getImmediateValue(module);\n" +
            "+      final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();\n" +
            "+      return PythonSdkType.getLanguageLevelForSdk(sdk);\n" +
            "     }\n" +
            "     final Sdk sdk = findSdk(project, file);\n" +
            "     if (sdk != null) {\n" +
            "       return PythonSdkType.getLanguageLevelForSdk(sdk);\n" +
            "     }";
    final String test = new DiffUtil().execute(changed, patch, "test");
    Assert.assertEquals(original, test);
  }

  @Test
  public void test3() throws Exception {
    final String original = "Skype Translator results from decades of work by the industry, years of work by our researchers,\n" +
            "and now is being developed jointly by the Skype and Microsoft Translator teams.\n" +
            "The demo showed near real-time audio translation from English to German and vice versa,\n" +
            "combining Skype voice and IM technologies with Microsoft Translator, and neural network-based speech recognition.\n" +
            "Skype Translator is a great example of why Microsoft invests in basic research.\n" +
            "    We've invested in speech recognition, automatic translation and machine learning technologies\n" +
            "for more than a decade, and now they're emerging as important components in this more personal computing era.\n" +
            "You can learn more about the research behind this initiative here.\n"; // \n was added to seems bug in patch applier with line endings
    final String changed = "Skype Translator results from decades of work by the industry, years of work by our researchers,\n" +
            "For more than a decade, Skype has brought people together to make progress on what matters to them.\n" +
            "The demo showed near real-time audio and video translation from English to German and vice versa,\n" +
            "combining Skype voice and IM technologies with Microsoft Translator, and neural network-based speech recognition.\n" +
            "Skype Translator is a great example of why Microsoft invests in basic research.\n" +
            "    We've invested in speech translation, automatic translation and machine learning technologies\n" +
            "for more than a decade, and now they're emerging as important components in this more personal computing era.\n" +
            "You can learn more about the research behind this initiative here.\n" +
            "test end";
    final String patch = "Index: src/com/something/art.txt\n" +
            "==================================================================\n" +
            "--- src/com/something/art.txt\n" +
            "+++ src/com/something/art.txt\n" +
            "@@ -1,8 +1,9 @@\n" +
            " Skype Translator results from decades of work by the industry, years of work by our researchers,\n" +
            "-and now is being developed jointly by the Skype and Microsoft Translator teams.\n" +
            "-The demo showed near real-time audio translation from English to German and vice versa,\n" +
            "+For more than a decade, Skype has brought people together to make progress on what matters to them.\n" +
            "+The demo showed near real-time audio and video translation from English to German and vice versa,\n" +
            " combining Skype voice and IM technologies with Microsoft Translator, and neural network-based speech recognition.\n" +
            " Skype Translator is a great example of why Microsoft invests in basic research.\n" +
            "-    We've invested in speech recognition, automatic translation and machine learning technologies\n" +
            "+    We've invested in speech translation, automatic translation and machine learning technologies\n" +
            " for more than a decade, and now they're emerging as important components in this more personal computing era.\n" +
            "-You can learn more about the research behind this initiative here.\n" +
            "+You can learn more about the research behind this initiative here.\n" +
            "+test end";
    final String test = new DiffUtil().execute(changed, patch, "test");
    Assert.assertEquals(original, test);
  }
}
