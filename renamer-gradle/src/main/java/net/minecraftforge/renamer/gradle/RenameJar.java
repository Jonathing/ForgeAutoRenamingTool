package net.minecraftforge.renamer.gradle;

import net.minecraftforge.gradleutils.shared.ToolExecBase;
import org.gradle.api.Task;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.tasks.DefaultTaskDependency;
import org.gradle.api.internal.tasks.TaskDependencyFactory;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jspecify.annotations.Nullable;

import javax.inject.Inject;
import java.io.File;
import java.util.Date;
import java.util.Set;

@CacheableTask
public abstract class RenameJar extends ToolExecBase<RenamerProblems> implements RenamerTask, PublishArtifact {
    public abstract @InputFile @PathSensitive(PathSensitivity.RELATIVE) RegularFileProperty getInput();

    public abstract @InputFiles @Classpath ConfigurableFileCollection getMap();

    public abstract @OutputFile RegularFileProperty getOutput();

    public abstract @InputFiles @Optional @CompileClasspath ConfigurableFileCollection getLibraries();

    public abstract @Input @Optional Property<String> getArchiveClassifier();

    public abstract @Input Property<String> getArchiveExtension();

    protected abstract @Internal Property<Date> getArchiveDate();

    protected abstract @Inject TaskDependencyFactory getTaskDependencyFactory();

    @Inject
    public RenameJar() {
        super(Tools.RENAMER);

        this.getOutput().convention(this.getDefaultOutputFile());
        this.getLibraries().convention(getProject().getExtensions().getByType(JavaPluginExtension.class).getSourceSets().named(SourceSet.MAIN_SOURCE_SET_NAME).map(SourceSet::getCompileClasspath));

        this.getArchiveExtension().convention("jar");
        this.getArchiveDate().convention(this.getInput().map(input -> new Date(input.getAsFile().lastModified())));
    }

    public void from(AbstractArchiveTask task) {
        this.from(getProject().getTasks().named(task.getName(), task.getClass()));
    }

    public void from(TaskProvider<? extends AbstractArchiveTask> task) {
        this.getInput().set(task.flatMap(AbstractArchiveTask::getArchiveFile));
        this.getArchiveClassifier().set(task.flatMap(AbstractArchiveTask::getArchiveClassifier).filter(Util.STRING_IS_PRESENT).map(s -> s + "-renamed").orElse("renamed"));
        this.getArchiveExtension().set(task.flatMap(AbstractArchiveTask::getArchiveExtension).orElse("jar"));
    }

    @Override
    protected void addArguments() {
        this.args("--input", this.getInput());
        this.args("--map", this.getMapFile());
        this.args("--output", this.getOutput());
        this.args("--lib", this.getLibraries());

        super.addArguments();
    }

    private File getMapFile() {
        try {
            return this.getMap().getSingleFile();
        } catch (IllegalStateException exception) {
            throw getProblems().reportMultipleMapFiles(exception, this);
        }
    }

    @Override
    public @Internal @Nullable String getClassifier() {
        return this.getArchiveClassifier().getOrNull();
    }

    @Override
    public @Internal String getExtension() {
        return this.getArchiveExtension().get();
    }

    @Override
    public @Internal String getType() {
        return this.getExtension();
    }

    @Override
    public @Internal File getFile() {
        return this.getOutput().getAsFile().get();
    }

    @Override
    public @Internal Date getDate() {
        return this.getArchiveDate().get();
    }

    @Override
    public @Internal TaskDependency getBuildDependencies() {
        return task -> Set.of(RenameJar.this);
    }
}

