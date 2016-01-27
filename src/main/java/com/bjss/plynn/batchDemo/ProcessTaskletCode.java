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
 * The InitialMove tasklet is used to write out a simple message to
 * standard out during the processing of your job.
 */
public class ProcessTaskletCode implements Tasklet {

    Logger myLogger = LoggerFactory.getLogger(ProcessTaskletCode.class);

    /* Getter and setter for injected value */
    public String getfileNameInjected() {
        return fileNameInjected;
    }

    public void setfileNameInjected(String fileNameInjected) {
        this.fileNameInjected = fileNameInjected;
    }

    private String fileNameInjected = "";

    /*
    Two methods of passing parameters to the batch are illustrated.  The variable "fileNameInjected" is injected
    by the batch definition demoOne.xml, from the run parameter outFileName.  The directory name is
    taken from the parameters directly through the step context, from inFileName.
      Note also the use of the job execution context to pass changed file names to later steps.
     */
    public RepeatStatus execute( StepContribution arg0, ChunkContext arg1 ) throws Exception {

        String directoryName = (String) arg1.getStepContext().getJobParameters().get("inDirectoryName");
        /* Check the job execution context for "changedFileName" to see if a previous step had
        to change the file name to avoid a collision. */
        String fileName =  arg1.getStepContext().getStepExecution().getJobExecution().getExecutionContext().
                getString("changedFileName", fileNameInjected);
        myLogger.info ("\n***************************************\nThe processing directory is {} plus inFlight.  The processing file is {}\n***************************************",
                directoryName, fileName);
         // Get Path objects to source file and working location
        Path inFilePath = FileSystems.getDefault().getPath(directoryName, fileName);
        myLogger.info("The path object is {}, and the path fileName is {}", inFilePath, inFilePath.getFileName());
        Path workFilePath=FileSystems.getDefault().getPath(directoryName, "/inFlight", fileName);
        myLogger.info("The work path object is {}, and the work path fileName is {}", workFilePath, workFilePath.getFileName());
        arg0.setExitStatus(ExitStatus.COMPLETED);
//
//        try {
//            // Move the file
//            Files.move(inFilePath, workFilePath);
//            myLogger.info("Moved file {} to {}", inFilePath, workFilePath);
//        } catch (java.nio.file.FileAlreadyExistsException exists) {
//            // If a work file with the same directoryName exists, add an arbitrary sequence.
//            // Take the current date/time in hexadecimal.
//            long now = new Date().getTime();
//            String append = "." + Long.toHexString(now);
//            String newFileName = fileName.concat(append);
//            workFilePath = FileSystems.getDefault().getPath(directoryName, "/inFlight", newFileName);
//            myLogger.info("{} already exists.  Add timestamp and move file to {} instead.", inFilePath, workFilePath);
//            Files.move(inFilePath, workFilePath);
//            /* ... and put that modified file name into the job execution context */
//            arg1.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
//                    .put("changedFileName", newFileName);
//            myLogger.info("Moved file {} to {}", inFilePath, workFilePath);
//        } catch (Exception e) {
//            // and fail if that doesn't work either.
//            myLogger.error("Got an error in moving {} to {}.", inFilePath, workFilePath);
//            myLogger.error(e.toString());
//         arg0.setExitStatus(ExitStatus.FAILED);
//        }
        return RepeatStatus.FINISHED;
    }
}
