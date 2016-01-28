package com.bjss.plynn.batchDemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;


/**
 * The InitialMove tasklet is used to write out a simple message to
 * standard out during the processing of your job.
 */
public class InitialMove implements Tasklet {

    Logger myLogger = LoggerFactory.getLogger(InitialMove.class);

    /* Injected value */
    private String fileNameInjected = "";
    public String getfileNameInjected() { return fileNameInjected; }
    public void setfileNameInjected(String fileNameInjected) { this.fileNameInjected = fileNameInjected; }

    /*
    Two methods of passing parameters to the batch are illustrated.  The variable "fileNameInjected" is injected
    by the batch definition demoOne.xml, from the run parameter outFileName.  The directory name is
    taken from the parameters directly through the step context, from inFileName.
      Note also the use of the job execution context to pass changed file names to later steps.
     */
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

         // Get Path objects to source file and working location
        Path inFilePath = pathFinder.getInputFilePath();
        Path workFilePath = pathFinder.getWorkFilePath();
        Path doneFilePath = pathFinder.getDoneFilePath();

        myLogger.info ("\n***************************************\n");
        myLogger.info("The path object is {}, and the path fileName is {}", inFilePath, inFilePath.getFileName());
        myLogger.info("The work path object is {}, and the work path fileName is {}", workFilePath, workFilePath.getFileName());
        arg0.setExitStatus(ExitStatus.COMPLETED);

        if (Files.exists(doneFilePath) || Files.exists(workFilePath))
        {
           long now = new Date().getTime();
            String append = "." + Long.toHexString(now);
            if (Files.exists(workFilePath)) {
                myLogger.info("{} already exists.  Add timestamp {}",
                        workFilePath, append);
            } else {
                myLogger.info("{} already exists.  Add timestamp {}",
                        doneFilePath, append);
            }
            pathFinder.setFileName(pathFinder.getFileName().concat(append));
            workFilePath = pathFinder.getWorkFilePath();
            doneFilePath = pathFinder.getDoneFilePath();
            arg1.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                    .put("changedFileName", pathFinder.getFileName());
        }
        try {
            // Move the file
            Files.move(inFilePath, workFilePath);
            myLogger.info("Moved file {} to {}", inFilePath, workFilePath);
         } catch (Exception e) {
            // and fail if that doesn't work either.
            myLogger.error("Got an error in moving {} to {}.", inFilePath, workFilePath);
            myLogger.error(e.toString());
            arg0.setExitStatus(ExitStatus.FAILED);
        }
        return RepeatStatus.FINISHED;
    }
}
