package com.treblemaker.weighters.basslineweighter;

import com.treblemaker.Application;
import com.treblemaker.factory.ExecutorPoolFactory;
import com.treblemaker.model.bassline.BasslineWithRating;
import com.treblemaker.model.progressions.ProgressionUnitBar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class BasslineWeighter implements IBasslineWeighter {

    private final int THREAD_TIMEOUT = 60;

    @Override
    public List<BasslineWithRating> rateBasslines(boolean bypassBasslineVerticalRating, List<BasslineWithRating> basslineWithRatings, ProgressionUnitBar barOne, ProgressionUnitBar barTwo){

        Collection<BasslineWeightTask> taskList = new ArrayList<>();

        for (BasslineWithRating basslineRating: basslineWithRatings) {

            BasslineWeightTask basslineWeightTask = new BasslineWeightTask(bypassBasslineVerticalRating, barOne, barTwo, basslineRating);

            taskList.add(basslineWeightTask);
        }

        List<BasslineWithRating> responses = new ArrayList<>();

//        Application.logger.debug("LOG:","Bassline TaskList SIZE: " + taskList.size());
        ExecutorService executorPool = ExecutorPoolFactory.getPool();
//        Application.logger.debug("LOG:","EXECUTOR : CREATING");
//        Application.logger.debug("LOG:","TIME: " + DateTime.now());
        try {
            List<Future<BasslineWithRating>> invokedTasks = executorPool.invokeAll(taskList);


            for(Future<BasslineWithRating> task : invokedTasks){
                try {
                    responses.add(task.get());
                    task.get(THREAD_TIMEOUT, TimeUnit.SECONDS).getTotalWeight(); //TODO IS THIS USED!!!!!
//                    Application.logger.debug("LOG:","Bassline weight status : " + task.get(THREAD_TIMEOUT, TimeUnit.SECONDS).getTotalWeight());
                }catch (TimeoutException tException){
                    Application.logger.debug("LOG: ERROR!! : TIMEOUT EXCEPTION : BassL WEIGHTING!!");
                }catch(InterruptedException ie){
                    Application.logger.debug("LOG: ERROR!! : INTERRUPT EXCEPTION : BassL WEIGHTING!!");
                }
            }
        } catch (Exception e) {
            Application.logger.debug("LOG:", e);
        }
        executorPool.shutdownNow();

        try {
            while (!executorPool.awaitTermination(10, TimeUnit.SECONDS)) {
                Application.logger.debug("LOG: Awaiting completion of threads.");
            }

//            Application.logger.debug("LOG:","EXECUTOR : SHUTDOWN SUCCESS");
        } catch (InterruptedException e) {
            Application.logger.debug("LOG:", e);
        }

        return responses;
    }
}