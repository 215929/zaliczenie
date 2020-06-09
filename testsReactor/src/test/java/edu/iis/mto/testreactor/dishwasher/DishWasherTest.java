package edu.iis.mto.testreactor.dishwasher;

import static edu.iis.mto.testreactor.dishwasher.Status.DOOR_OPEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import edu.iis.mto.testreactor.dishwasher.engine.Engine;
import edu.iis.mto.testreactor.dishwasher.pump.WaterPump;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DishWasherTest {

    DishWasher dishWasher;
    @Mock
    WaterPump waterPump;
    @Mock
    Engine engine;
    @Mock
    DirtFilter dirtFilter;
    @Mock
    Door door;

    WashingProgram notRinse = WashingProgram.ECO;
    FillLevel irrelevant = FillLevel.HALF;

    @BeforeEach
    void init () {
        dishWasher = new DishWasher(waterPump, engine, dirtFilter, door);
    }

       @Test
    void properProgramShouldResultInProperResult () {
        ProgramConfiguration program = properProgram();

        Mockito.when(door.closed()).thenReturn(true);
        Mockito.when(dirtFilter.capacity()).thenReturn(100.0d);

        RunResult result = dishWasher.start(program);
        RunResult expectedResult = RunResult.builder().withStatus(Status.SUCCESS).withRunMinutes(notRinse.getTimeInMinutes()).build();

        assertEquals(expectedResult.getStatus(), result.getStatus());
        assertEquals(expectedResult.getRunMinutes(), result.getRunMinutes());
    }

    @Test
    void doorOpenShouldResultInDoorError () {
        ProgramConfiguration program = properProgram();

        Mockito.when(door.closed()).thenReturn(false);

        RunResult result = dishWasher.start(program);
        RunResult expectedResult =  RunResult.builder().withStatus(Status.DOOR_OPEN).build();

        assertEquals(expectedResult.getStatus(), result.getStatus());
    }

    @Test
    void ifFilterIsNotCleanShouldResultInFilterError () {
        ProgramConfiguration program = properProgram();

        Mockito.when(door.closed()).thenReturn(true);
        Mockito.when(dirtFilter.capacity()).thenReturn(0d);

        RunResult result = dishWasher.start(program);
        RunResult expectedResult =  RunResult.builder().withStatus(Status.ERROR_FILTER).build();

        assertEquals(expectedResult.getStatus(), result.getStatus());
    }

    private ProgramConfiguration properProgram() {
        return ProgramConfiguration.builder().withFillLevel(irrelevant).withProgram(notRinse).withTabletsUsed(true).build();
    }

}
