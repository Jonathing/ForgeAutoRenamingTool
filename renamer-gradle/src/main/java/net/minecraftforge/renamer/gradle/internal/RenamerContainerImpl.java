package net.minecraftforge.renamer.gradle.internal;

import net.minecraftforge.renamer.gradle.RenamerConfiguration;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyFactory;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;

import javax.inject.Inject;

abstract class RenamerContainerImpl implements RenamerContainerInternal {
    private final Property<String> name = getObjects().property(String.class);

    // Renamer inputs
    private final ConfigurableFileCollection mappings = getObjects().fileCollection();

    // Renamer outputs
    private final AdhocComponentWithVariants softwareComponent;
    private final TaskProvider<RenameClasses> renamedClasses;
    private final TaskProvider<? extends AbstractArchiveTask> renamedClassesInput;
    private final FileCollection renamedClassesLibraries;
    private final TaskProvider<Zip> renamedJar;

    protected abstract @Inject Project getProject();

    protected abstract @Inject ObjectFactory getObjects();

    protected abstract @Inject ProviderFactory getProviders();

    protected abstract @Inject DependencyFactory getDependencies();

    protected abstract @Inject SoftwareComponentFactory getSoftwareComponents();

    @Inject
    public RenamerContainerImpl(SourceSet sourceSet) {
        var project = getProject();
        var tasks = project.getTasks();

        this.name.value(sourceSet.getTaskName("renamed", null)).finalizeValue();
        this.renamedClassesInput = tasks.named(sourceSet.getJarTaskName(), Jar.class);
        this.renamedClassesLibraries = sourceSet.getCompileClasspath();

        project.getComponents().add(this.softwareComponent = getSoftwareComponents().adhoc(this.name.get() + "java"));
        this.renamedClasses = tasks.register(this.name.get() + "Classes", RenameClasses.class, task -> {
            task.dependsOn(this.mappings.getBuildDependencies());
            task.getMap().setFrom(this.mappings);
        });
        var extractRenamedClasses = tasks.register("extract" + this.renamedClasses.getName(), ExtractSingleArchive.class, task -> {
            task.dependsOn(this.renamedClasses);
            task.getInput().set(this.renamedClasses.flatMap(RenameClasses::getOutput));
        });
        this.renamedJar = tasks.register(this.name.get() + "Jar", Zip.class, task -> {
            task.dependsOn(extractRenamedClasses);
            task.from(extractRenamedClasses);
        });

        getProject().afterEvaluate(this::finish);
    }

    private void finish(Project project) {
        this.renamedClasses.configure(task -> {
            var input = this.renamedClassesInput;
            var libraries = this.renamedClassesLibraries;

            task.dependsOn(input, libraries.getBuildDependencies());
            task.getInput().set(input.flatMap(AbstractArchiveTask::getArchiveFile));
            task.getLibraries().setFrom(libraries);
        });
    }

    @Override
    public void mappings(String artifact) {
        this.mappings(getDependencies().create(artifact));
    }

    @Override
    public void mappings(Dependency dependency) {
        var configuration = getProject().getConfigurations().detachedConfiguration(dependency);
        configuration.setTransitive(false);

        this.mappings.setFrom(configuration);
    }

    @Override
    public void mappings(Provider<? extends Dependency> dependency) {
        var configuration = getProject().getConfigurations().detachedConfiguration();
        configuration.getDependencies().addLater(dependency);
        configuration.setTransitive(false);

        this.mappings.setFrom(configuration);
    }

    public void classes(Action<? super RenamerConfiguration> action) {
        var configuration = getObjects().newInstance(RenamerConfigurationImpl.class);
        action.execute(configuration);

        this.renamedClasses.configure(task -> {
            if (configuration.getInput() != null) {
                task.getInput().set(configuration.getInput().flatMap(AbstractArchiveTask::getArchiveFile));
            }

            if (configuration.getClasspath() != null) {
                task.getClasspath().setFrom(configuration.getClasspath());
            }
        });

        if (configuration.getAction() != null)
            this.renamedJar.configure(configuration.getAction());
    }
}
