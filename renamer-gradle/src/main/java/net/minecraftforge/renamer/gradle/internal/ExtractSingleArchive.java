package net.minecraftforge.renamer.gradle.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

@CacheableTask
abstract class ExtractSingleArchive extends DefaultTask implements RenamerTask {
    protected abstract @InputFile RegularFileProperty getInput();

    protected abstract @OutputDirectory DirectoryProperty getOutput();

    protected abstract @Inject FileSystemOperations getFileSystemOperations();

    protected abstract @Inject ArchiveOperations getArchiveOperations();

    @Inject
    public ExtractSingleArchive() {
        this.getOutput().convention(this.getDefaultOutputDirectory());
    }

    @TaskAction
    protected void exec() {
        getFileSystemOperations().copy(copy -> copy
            .from(getArchiveOperations().zipTree(getInput()))
            .into(this.getOutput())
        );
    }
}
