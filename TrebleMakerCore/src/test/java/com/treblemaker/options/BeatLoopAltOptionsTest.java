package com.treblemaker.options;

import com.treblemaker.SpringConfiguration;
import com.treblemaker.model.BeatLoop;
import com.treblemaker.model.SourceData;
import com.treblemaker.model.progressions.ProgressionUnit;
import com.treblemaker.model.progressions.ProgressionUnitBar;
import com.treblemaker.options.interfaces.IBeatLoopOptions;
import com.treblemaker.scheduledevents.interfaces.IQueueHelpers;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringConfiguration.class, properties ={"return_queue_early_for_tests=true","queue_scheduled_interval=8999999","queue_scheduled_start_delay=8999999","spring.datasource.url=jdbc:mysql://localhost:3306/hivecomposedb?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","spring.datasource.username=root","spring.datasource.password=redrobes79D"})
public class BeatLoopAltOptionsTest extends TestCase {

    @Autowired
    private IQueueHelpers queueHelpers;

    @Autowired
    private IBeatLoopOptions beatLoopAltOptions;

    @Test
    public void ShouldSetBeatLoopAltOptions() {

        SourceData sourceData = queueHelpers.getSourceData();

        BeatLoop beatLoopA = new BeatLoop();
        beatLoopA.setBarCount(2);
        BeatLoop beatLoopB = new BeatLoop();
        beatLoopB.setBarCount(1);

        ProgressionUnitBar pUnitBarA = new ProgressionUnitBar();
        pUnitBarA.setBeatLoop(beatLoopA);
        ProgressionUnitBar pUnitBarB = new ProgressionUnitBar();
        pUnitBarB.setBeatLoop(beatLoopA);
        ProgressionUnitBar pUnitBarC = new ProgressionUnitBar();
        pUnitBarC.setBeatLoop(beatLoopB);
        ProgressionUnitBar pUnitBarD = new ProgressionUnitBar();
        pUnitBarD.setBeatLoop(beatLoopB);

        List<ProgressionUnitBar> pBars = Arrays.asList(pUnitBarA, pUnitBarB, pUnitBarC,pUnitBarD);

        ProgressionUnit progressionUnit = new ProgressionUnit();
        progressionUnit.setProgressionUnitBars(pBars);

        progressionUnit.getProgressionUnitBars().forEach(pUnitBar -> {
            beatLoopAltOptions.setBeatLoopOptions(pUnitBar, sourceData.getBeatLoops(1));
        });

        progressionUnit.getProgressionUnitBars().forEach(pUnitBar -> {
            assertThat(pUnitBar.getBeatLoopAltOptions()).isNotNull();
            assertThat(progressionUnit.getProgressionUnitBars().size()).isGreaterThan(0);
        });

        List<ProgressionUnitBar> bars = progressionUnit.getProgressionUnitBars();

        bars.get(0).getBeatLoopAltOptions().forEach(option -> assertThat(option.getBarCount()).isEqualTo(2));
        bars.get(1).getBeatLoopAltOptions().forEach(option -> assertThat(option.getBarCount()).isEqualTo(2));
        bars.get(2).getBeatLoopAltOptions().forEach(option -> assertThat(option.getBarCount()).isEqualTo(1));
        bars.get(3).getBeatLoopAltOptions().forEach(option -> assertThat(option.getBarCount()).isEqualTo(1));
    }

}