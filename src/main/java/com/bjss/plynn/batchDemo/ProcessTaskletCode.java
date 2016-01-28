package com.bjss.plynn.batchDemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;


/**
 * The Process tasklet represents processing a file and then moving it to
 * a directory of completed files for archiving.  It is the secong step of
 * the simple Spring Batch processing demo.
 */
public class ProcessTaskletCode implements Tasklet {

    Logger myLogger = LoggerFactory.getLogger(ProcessTaskletCode.class);

    /*
     * Two methods of passing parameters to the batch are illustrated.  The variable "fileNameInjected" is injected
     * by the batch definition demoOne.xml, from the run parameter outFileName.  The directory name is
     * taken from the parameters directly through the step context, from inFileName.
     *   Note also the use of the job execution context to get changed file names from earlier steps.
     */

    /* Injected value */
    private String fileNameInjected = "";
    public String getfileNameInjected() {
        return fileNameInjected;
    }
    public void setfileNameInjected(String fileNameInjected) {
        this.fileNameInjected = fileNameInjected;
    }

    public RepeatStatus execute( StepContribution arg0, ChunkContext arg1 ) throws Exception {

        PathFinder pathFinder;
        {
        /* Check the job execution context for "changedFileName" to see if a previous step had
        to change the file name to avoid a collision. */
            String fileName = arg1.getStepContext().getStepExecution().getJobExecution().getExecutionContext().
                    getString("changedFileName", fileNameInjected);
            String directoryName = (String) arg1.getStepContext().getJobParameters().get("inDirectoryName");
            pathFinder = PathFinder.getPathFinder(directoryName, fileName);
        }
        // Get Path object to the file to process
        Path workFilePath=pathFinder.getWorkFilePath();
        Path doneFilePath=pathFinder.getDoneFilePath();
        myLogger.info ("\n***************************************");
        myLogger.info("The work path object is {}, and the work path fileName is {}", workFilePath, workFilePath.getFileName());
        arg0.setExitStatus(ExitStatus.COMPLETED);

        try {
            // "Process" the file
            long howbig = Files.size(workFilePath);
            myLogger.info("The size of the file is {} bytes", howbig);
        } catch (Exception e) {
            // and fail if that doesn't work either.
            myLogger.error("Got an error in processing {}.", workFilePath);
            myLogger.error(e.toString());
            arg0.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        }
        try {
            // Move the file to the completed directory
            Files.move(workFilePath, doneFilePath);
            myLogger.info("Moved processed file {} to {}", workFilePath, doneFilePath);
        } catch (Exception e) {
            // and fail if that doesn't work either.
            myLogger.error("Got an error in moving {} to {}.", workFilePath, doneFilePath);
            myLogger.error(e.toString());
            arg0.setExitStatus(ExitStatus.FAILED);
        }

        return RepeatStatus.FINISHED;
    }
}
