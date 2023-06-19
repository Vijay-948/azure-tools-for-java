package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.facet.Facet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.common.runconfig.IWebAppRunConfiguration;
import com.microsoft.azure.toolkit.intellij.connector.IConnectionAware;
import com.microsoft.azure.toolkit.intellij.facet.AzureProjectFacet;
import com.microsoft.azure.toolkit.intellij.facet.AzureProjectFacetConfiguration;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AzureModule {
    private static final Pattern PATTERN = Pattern.compile("(Gradle|Maven): (.+):(.+):(.+)");

    static final String DOT_AZURE = ".azure";
    static final String PROFILES_XML = "profiles.xml";
    static final String DOT_GITIGNORE = ".gitignore";
    static final String DEFAULT_PROFILE_NAME = "default";
    static final String DOT_ENV = ".env";
    static final String TARGETS_FILE = "deployment.targets.xml";
    static final String RESOURCES_FILE = "connections.resources.xml";
    static final String CONNECTIONS_FILE = "connections.xml";

    private static final Map<Module, AzureModule> modules = new ConcurrentHashMap<>();
    public static final String ATTR_DEFAULT_PROFILE = "defaultProfile";
    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    @Getter
    @Nonnull
    private final Module module;
    @Getter
    @Nullable
    private VirtualFile dotAzure;
    @Getter
    @Nullable
    private VirtualFile profilesXmlFile;
    @Nullable
    private Profile defaultProfile;

    public AzureModule(@Nonnull final Module module) {
        this(module, null);
    }

    public AzureModule(@Nonnull final Module module, @Nullable VirtualFile dotAzure) {
        this.module = module;
        Optional.ofNullable(dotAzure)
            .or(() -> Optional.ofNullable(AzureProjectFacet.getInstance(module)).map(Facet::getConfiguration).map(AzureProjectFacetConfiguration::getDotAzureDir))
            .or(() -> this.getModuleDir().map(d -> d.findChild(DOT_AZURE))).ifPresent(d -> {
                this.dotAzure = d;
                this.profilesXmlFile = this.dotAzure.findChild(PROFILES_XML);
                Optional.ofNullable(this.profilesXmlFile).ifPresent(this::loadProfiles);
            });
    }

    @SneakyThrows(value = {IOException.class, JDOMException.class})
    private void loadProfiles(@Nonnull final VirtualFile profilesXmlFile) {
        if (profilesXmlFile.contentsToByteArray().length < 1) {
            return;
        }
        final Element profilesEle = JDOMUtil.load(Objects.requireNonNull(profilesXmlFile).toNioPath());
        final VirtualFile dotAzure = Objects.requireNonNull(this.dotAzure);
        profilesEle.getChildren().stream().map(e -> e.getAttributeValue("name"))
            .forEach(name -> Optional.ofNullable(this.dotAzure).map(d -> d.findChild(name))
                .map(profileDir -> new Profile(name, profileDir, this))
                .ifPresent(profile -> this.profiles.put(profile.getName(), profile)));
        this.defaultProfile = Optional.ofNullable(profilesEle.getAttributeValue(ATTR_DEFAULT_PROFILE)).map(this.profiles::get).orElse(null);
    }

    @Nonnull
    public AzureModule initializeIfNot() {
        if (Objects.nonNull(this.profilesXmlFile)) {
            return this;
        }
        return this.getModuleDir().map(moduleDir -> {
            try {
                this.dotAzure = VfsUtil.createDirectoryIfMissing(moduleDir, DOT_AZURE);
                final VirtualFile dotGitIgnore = dotAzure.findOrCreateChildData(this, DOT_GITIGNORE);
                dotGitIgnore.setBinaryContent((DOT_ENV + "\n" + RESOURCES_FILE + "\n" + TARGETS_FILE).getBytes());
                this.profilesXmlFile = dotAzure.findOrCreateChildData(this, PROFILES_XML);
                Optional.of(this.profilesXmlFile).ifPresent(this::loadProfiles);
                this.dotAzure.refresh(true, false);
            } catch (final IOException e) {
                throw new AzureToolkitRuntimeException(e);
            }
            return this;
        }).orElse(this);
    }

    @Nonnull
    public Profile initializeWithDefaultProfileIfNot() {
        final AzureModule module = this.initializeIfNot();
        Profile profile = module.getDefaultProfile();
        if (Objects.isNull(profile)) {
            profile = module.getOrCreateProfile(DEFAULT_PROFILE_NAME);
            module.setDefaultProfile(profile);
        }
        return profile;
    }

    @Nullable
    public Profile getDefaultProfile() {
        if (Objects.isNull(this.defaultProfile)) {
            if (!this.isInitialized()) {
                return null;
            }
            final String envName = this.getDefaultProfileName();
            if (StringUtils.isBlank(envName)) {
                return null;
            }
            this.defaultProfile = this.getProfile(envName);
        }
        return this.defaultProfile;
    }

    @Nullable
    public Profile getProfile(String profileName) {
        return this.profiles.computeIfAbsent(profileName, name -> Optional
            .ofNullable(this.dotAzure)
            .map(dotAzure -> dotAzure.findChild(profileName))
            .map(profileDir -> new Profile(name, profileDir, this)).orElse(null));
    }

    @Nonnull
    public Profile getOrCreateProfile(String profileName) {
        this.validate();
        return this.profiles.computeIfAbsent(profileName, name -> {
            try {
                final VirtualFile profileDir = VfsUtil.createDirectoryIfMissing(this.dotAzure, profileName);
                final Profile profile = new Profile(name, profileDir, this);
                this.registerProfile(profile);
                return profile;
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AzureOperation(name = "boundary/connector.create_profile_for_module.module", params = {"this.module.getName()"})
    @SneakyThrows(value = {IOException.class, JDOMException.class})
    private void registerProfile(@Nonnull final Profile profile) {
        final VirtualFile profilesXmlFile = Objects.requireNonNull(this.profilesXmlFile);
        final Element profilesEle = profilesXmlFile.contentsToByteArray().length < 1 ?
            new Element("profiles").setAttribute("version", "1") :
            JDOMUtil.load(profilesXmlFile.toNioPath());
        profilesEle.addContent(new Element("profile").setAttribute("name", profile.getName()));
        JDOMUtil.write(profilesEle, this.profilesXmlFile.toNioPath());
    }

    @Nullable
    @SneakyThrows(value = {JDOMException.class, IOException.class})
    private String getDefaultProfileName() {
        if (Objects.nonNull(this.defaultProfile)) {
            return this.defaultProfile.getName();
        }
        if (Objects.isNull(this.profilesXmlFile) || this.profilesXmlFile.contentsToByteArray().length < 1) {
            return null;
        }
        final Element profilesEle = JDOMUtil.load(this.profilesXmlFile.toNioPath());
        return profilesEle.getAttributeValue(ATTR_DEFAULT_PROFILE, DEFAULT_PROFILE_NAME);
    }

    @SneakyThrows(value = {JDOMException.class, IOException.class})
    public void setDefaultProfile(@Nonnull Profile profile) {
        this.validate();
        final Element profilesEle = JDOMUtil.load(Objects.requireNonNull(this.profilesXmlFile).toNioPath());
        profilesEle.setAttribute(ATTR_DEFAULT_PROFILE, profile.getName());
        profilesEle.setAttribute("version", "1");
        JDOMUtil.write(profilesEle, this.profilesXmlFile.toNioPath());
        this.defaultProfile = profile;
    }

    private void validate() {
        final VirtualFile configFile = getProfilesXmlFile();
        if (Objects.isNull(configFile)) {
            throw new AzureToolkitRuntimeException("Azure module is not initialized.");
        }
    }

    public String getName() {
        return module.getName();
    }

    public Optional<VirtualFile> getModuleDir() {
        return Optional.of(module).map(ProjectUtil::guessModuleDir);
    }

    public Optional<VirtualFile> getDotAzureDir() {
        return Optional.ofNullable(this.dotAzure);
    }

    public boolean isInitialized() {
        return Objects.nonNull(this.profilesXmlFile);
    }

    public boolean hasAzureDependencies() {
        final List<String> libs = new ArrayList<>();
        OrderEnumerator.orderEntries(this.module).librariesOnly().forEachLibrary(l -> libs.add(l.getName()));
        return libs.stream().filter(StringUtils::isNotBlank)
            .map(PATTERN::matcher).filter(Matcher::matches)
            .anyMatch(m -> {
                final String artifactId = m.group(2).trim() + ":" + m.group(3).trim();
                return StringUtils.equals(artifactId, "com.azure:azure-core") ||
                    StringUtils.equals(artifactId, "com.microsoft.azure:azure-client-runtime") ||
                    StringUtils.equals(artifactId, "com.microsoft.azure.functions:azure-functions-java-library");
            });
    }

    @Nonnull
    public static AzureModule from(@Nonnull Module module) {
        return modules.computeIfAbsent(module, t -> new AzureModule(module));
    }

    @Nullable
    public static AzureModule from(@Nonnull VirtualFile file, @Nonnull Project project) {
        final Module module = ModuleUtil.findModuleForFile(file, project);
        return Objects.isNull(module) ? null : AzureModule.from(module);
    }

    public static List<AzureModule> list(@Nonnull Project project) {
        return Arrays.stream(ModuleManager.getInstance(project).getModules()).map(AzureModule::from).toList();
    }

    /**
     * check if the given {@param configuration} meet the requirements to convert into an {@link AzureModule}
     *
     * @return true if {@param configuration} is a
     * {@link ModuleBasedConfiguration}, {@link IWebAppRunConfiguration} or {@link IConnectionAware}.
     */
    public static boolean isSupported(RunConfiguration configuration) {
        return Optional.ofNullable(configuration)
            .map(AzureModule::getTargetModule)
            .isPresent();
    }

    /**
     * create {@code AzureModule} if the given {@param configuration} meet the requirements.
     * see {@link AzureModule#isSupported(RunConfiguration)}
     */
    public static Optional<AzureModule> createIfSupport(RunConfiguration configuration) {
        return Optional.ofNullable(configuration)
            .map(AzureModule::getTargetModule)
            .map(AzureModule::from);
    }

    @Nullable
    public static Module getTargetModule(@Nonnull RunConfiguration configuration) {
        if (configuration instanceof ModuleBasedConfiguration) {
            return ((ModuleBasedConfiguration<?, ?>) configuration).getConfigurationModule().getModule();
        } else if (configuration instanceof IWebAppRunConfiguration) {
            return ((IWebAppRunConfiguration) configuration).getModule();
        } else if (configuration instanceof IConnectionAware) {
            return ((IConnectionAware) configuration).getModule();
        }
        return null;
    }

    public Project getProject() {
        return this.module.getProject();
    }
}
