package net.minecraftforge.renamer.gradle.internal;

import net.minecraftforge.gradleutils.shared.ToolExecBase;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import javax.inject.Inject;
import java.io.File;

@CacheableTask
abstract class RenameClasses extends ToolExecBase<RenamerProblems> implements RenamerTask {
    public abstract @InputFile RegularFileProperty getInput();

    public abstract @InputFiles ConfigurableFileCollection getMap();

    public abstract @OutputFile RegularFileProperty getOutput();

    public abstract @InputFiles @Optional @Classpath ConfigurableFileCollection getLibraries();

    @Inject
    public RenameClasses(Provider<? extends SourceSet> sourceSet, TaskProvider<? extends AbstractArchiveTask> parent) {
        super(Constants.FART);

        this.getOutput().convention(this.getDefaultOutputFile());
        this.getLibraries().convention(sourceSet.map(SourceSet::getCompileClasspath));
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
}
