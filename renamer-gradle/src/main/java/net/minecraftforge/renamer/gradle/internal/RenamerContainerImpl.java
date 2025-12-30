package net.minecraftforge.renamer.gradle.internal;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyFactory;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jspecify.annotations.Nullable;

import javax.inject.Inject;

abstract class RenamerContainerImpl implements RenamerContainerInternal {
    private final ConfigurableFileCollection mappings = getObjets().fileCollection();

    private final NamedDomainObjectProvider<? extends SourceSet> sourceSet;
    private TaskProvider<? extends AbstractArchiveTask> jar;
    private @Nullable TaskProvider<? extends AbstractArchiveTask> sourcesJar;

    protected abstract @Inject Project getProject();
    protected abstract @Inject ObjectFactory getObjets();
    protected abstract @Inject DependencyFactory getDependencies();

    @Inject
    public RenamerContainerImpl(NamedDomainObjectProvider<? extends SourceSet> sourceSet) {
        this.sourceSet = sourceSet;
    }

    @Override
    public TaskProvider<? extends Task> getJarTask() {
        return this.renameJar;
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
}
