package net.minecraftforge.renamer.gradle.internal;

import net.minecraftforge.gradleutils.shared.ToolExecBase;
import net.minecraftforge.renamer.gradle.RenameClassesJar;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import javax.inject.Inject;
import java.io.File;
import java.util.function.BiConsumer;

abstract class RenameClassesJarImpl extends ToolExecBase<RenamerProblems> implements RenameClassesJar, RenamerTask {
    public abstract @InputFile RegularFileProperty getInput();

    public abstract @InputFiles ConfigurableFileCollection getMap();

    public abstract @InputFiles @Optional @Classpath ConfigurableFileCollection getLibraries();

    //@formatter:off
    // [destinationDirectory]/[baseName]-[appendix]-[version]-[classifier].[extension]
    public abstract @Internal DirectoryProperty getArchiveDestinationDirectory();
    public abstract @OutputFile RegularFileProperty getArchiveFile();
    public @Internal Provider<String> getArchiveFileName() { return this.archiveName; }
    public abstract @Internal Property<String> getArchiveBaseName();
    public abstract @Internal Property<String> getArchiveAppendix();
    public abstract @Internal Property<String> getArchiveVersion();
    public abstract @Internal Property<String> getArchiveExtension();
    public abstract @Internal Property<String> getArchiveClassifier();
    //@formatter:on

    private final Property<String> archiveName = getObjects().property(String.class);

    @Inject
    public RenameClassesJarImpl(Provider<? extends SourceSet> sourceSet, TaskProvider<? extends AbstractArchiveTask> parent) {
        super(Constants.FART);

        // Set libraries from source set
        this.getLibraries().convention(sourceSet.map(SourceSet::getCompileClasspath));

        // Set convention properties from parent
        this.getArchiveDestinationDirectory().convention(parent.flatMap(AbstractArchiveTask::getDestinationDirectory));
        this.getArchiveBaseName().convention(parent.flatMap(AbstractArchiveTask::getArchiveBaseName));
        this.getArchiveAppendix().convention(parent.flatMap(AbstractArchiveTask::getArchiveAppendix));
        this.getArchiveVersion().convention(parent.flatMap(AbstractArchiveTask::getArchiveVersion));
        this.getArchiveExtension().convention(parent.flatMap(AbstractArchiveTask::getArchiveExtension));
        this.getArchiveClassifier().convention(parent.flatMap(AbstractArchiveTask::getArchiveClassifier));

        this.archiveName.value(getProviders().provider(() -> {
            var builder = new StringBuilder().append(this.getArchiveBaseName().filter(Util.STRING_IS_PRESENT).getOrElse(""));

            BiConsumer<String, Property<String>> append = (prefix, s) -> {
                if (s.filter(Util.STRING_IS_PRESENT).isPresent()) {
                    if (!builder.isEmpty())
                        builder.append(prefix);
                    builder.append(s.get());
                }
            };

            append.accept("-", getArchiveAppendix());
            append.accept("-", getArchiveVersion());
            append.accept("-", getArchiveClassifier());
            append.accept(".", getArchiveExtension());

            return builder.toString();
        }));

        this.getArchiveFile().value(this.getArchiveDestinationDirectory().file(this.archiveName));
    }

    @Override
    protected void addArguments() {
        this.args("--input", getInput());
        this.args("--map", getMapFile());
        this.args("--output", getArchiveFile());
        this.args("--lib", getLibraries());

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
