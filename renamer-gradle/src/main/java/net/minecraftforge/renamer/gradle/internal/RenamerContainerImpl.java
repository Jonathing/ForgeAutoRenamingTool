package net.minecraftforge.renamer.gradle.internal;

import groovy.lang.Closure;
import net.minecraftforge.gradleutils.shared.Closures;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.Task;
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
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Zip;

import javax.inject.Inject;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

abstract class RenamerContainerImpl implements RenamerContainerInternal {
    private final Property<String> name = getObjects().property(String.class);

    // Renamer inputs
    private final ConfigurableFileCollection mappings = getObjects().fileCollection();

    // Renamer outputs
    private final AdhocComponentWithVariants softwareComponent;
    private final TaskProvider<RenameClasses> renamedClasses;
    private final TaskProvider<Zip> renamedJar;
    private Closure<AbstractArchiveTask> renamedJarConfiguration = Closures.unaryOperator(UnaryOperator.identity());

    // Parent tasks
    private final Property<String> jarTaskName = getObjects().property(String.class);

    protected abstract @Inject Project getProject();
    protected abstract @Inject ObjectFactory getObjects();
    protected abstract @Inject ProviderFactory getProviders();
    protected abstract @Inject ArchiveOperations getArchiveOperations();
    protected abstract @Inject DependencyFactory getDependencies();
    protected abstract @Inject SoftwareComponentFactory getSoftwareComponents();

    @Inject
    public RenamerContainerImpl(NamedDomainObjectProvider<? extends SourceSet> sourceSet) {
        this.name.value(sourceSet.map(s -> s.getTaskName("renamed", null))).finalizeValue();

        getProject().getComponents().add(this.softwareComponent = getSoftwareComponents().adhoc(sourceSet.get().getTaskName(this.name.get(), "java")));
        this.renamedClasses = getProject().getTasks().register(this.name.get() + "Classes", RenameClasses.class);
        this.renamedJar = getProject().getTasks().register(this.name.get() + "jar", Zip.class, task -> {
            task.from(getArchiveOperations().zipTree(this.renamedClasses.flatMap(RenameClasses::getOutput)));
        });

        this.jarTaskName.set(sourceSet.map(SourceSet::getJarTaskName));

        getProject().afterEvaluate(this::finish);
    }

    private void finish(Project project) {

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

    public void classes(TaskProvider<? extends AbstractArchiveTask> input, FileCollection classpath, Action<? super AbstractArchiveTask> action) {
        this.renamedClasses.configure(task -> {
            task.getInput().set(input.flatMap(AbstractArchiveTask::getArchiveFile));
            task.getLibraries().setFrom(classpath);
        });

        this.renamedJarConfiguration.andThen(Closures.<AbstractArchiveTask>unaryOperator(it -> {
            action.execute(it);
            return it;
        }));
    }
}
