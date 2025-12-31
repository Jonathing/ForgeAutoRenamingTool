package net.minecraftforge.renamer.gradle.internal;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jspecify.annotations.Nullable;

import javax.inject.Inject;

abstract class RenamerConfigurationImpl implements RenamerConfigurationInternal {
    private @Nullable TaskProvider<? extends AbstractArchiveTask> input;
    private @Nullable FileCollection classpath;
    private @Nullable Action<? super AbstractArchiveTask> action;

    protected abstract @Inject Project getProject();

    @Inject
    public RenamerConfigurationImpl() { }

    @Override
    public @Nullable TaskProvider<? extends AbstractArchiveTask> getInput() {
        return this.input;
    }

    @Override
    public @Nullable FileCollection getClasspath() {
        return this.classpath;
    }

    @Override
    public @Nullable Action<? super AbstractArchiveTask> getAction() {
        return this.action;
    }

    @Override
    public void setInput(TaskProvider<? extends AbstractArchiveTask> input) {
        this.input = input;
    }

    @Override
    public void setInput(AbstractArchiveTask task) {
        this.setInput(getProject().getTasks().named(task.getName(), AbstractArchiveTask.class));
    }

    @Override
    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    @Override
    public void archive(Action<? super AbstractArchiveTask> action) {
        this.action = action;
    }
}
