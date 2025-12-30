package net.minecraftforge.renamer.gradle;

import org.gradle.api.Task;
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

public interface RenameClassesJar extends Task {
    @InputFile RegularFileProperty getInput();

    @InputFiles ConfigurableFileCollection getMap();

    @InputFiles @Optional @Classpath ConfigurableFileCollection getLibraries();

    @Internal DirectoryProperty getArchiveDestinationDirectory();

    @OutputFile RegularFileProperty getArchiveFile();

    @Internal Provider<String> getArchiveFileName();

    @Internal Property<String> getArchiveBaseName();

    @Internal Property<String> getArchiveAppendix();

    @Internal Property<String> getArchiveVersion();

    @Internal Property<String> getArchiveExtension();

    @Internal Property<String> getArchiveClassifier();
}
