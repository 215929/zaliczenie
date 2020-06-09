package edu.iis.mto.testreactor.dishwasher;

import static edu.iis.mto.testreactor.dishwasher.Status.DOOR_OPEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import edu.iis.mto.testreactor.dishwasher.engine.Engine;
import edu.iis.mto.testreactor.dishwasher.engine.EngineException;
import edu.iis.mto.testreactor.dishwasher.pump.PumpException;
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

    private RunResult result;
    RunResult expectedResult;

    @BeforeEach
    void init () {
        dishWasher = new DishWasher(waterPump, engine, dirtFilter, door);
    }

    @Test
    void properProgramShouldResultInProperResult () {
        ProgramConfiguration program = properProgram();

        Mockito.when(door.closed()).thenReturn(true);
        Mockito.when(dirtFilter.capacity()).thenReturn(100.0d);

        result = dishWasher.start(program);
        expectedResult = getResult(Status.SUCCESS);

        assertEquals(expectedResult.getStatus(), result.getStatus());
        assertEquals(expectedResult.getRunMinutes(), result.getRunMinutes());
    }

    @Test
    void doorOpenShouldResultInDoorError () {
        ProgramConfiguration program = properProgram();

        Mockito.when(door.closed()).thenReturn(false);

        result = dishWasher.start(program);
        expectedResult = getResult(Status.DOOR_OPEN);

        assertEquals(expectedResult.getStatus(), result.getStatus());
    }

    @Test
    void ifFilterIsNotCleanShouldResultInFilterError () {
        ProgramConfiguration program = properProgram();

        Mockito.when(door.closed()).thenReturn(true);
        Mockito.when(dirtFilter.capacity()).thenReturn(0d);

        result = dishWasher.start(program);
        expectedResult = getResult(Status.ERROR_FILTER);

        assertEquals(expectedResult.getStatus(), result.getStatus());
    }

    @Test
    void ifEngineFailsShouldResultInProgramError () throws EngineException {
        ProgramConfiguration program = properProgram();

        Mockito.when(door.closed()).thenReturn(true);
        Mockito.when(dirtFilter.capacity()).thenReturn(100.0d);
        doThrow(EngineException.class).when(engine).runProgram(any());

        result = dishWasher.start(program);
        expectedResult = getResult(Status.ERROR_PROGRAM);

        assertEquals(expectedResult.getStatus(), result.getStatus());
    }

    @Test
    void ifWaterPumFailsShouldResultInPumpError () throws PumpException {
        ProgramConfiguration program = properProgram();

        Mockito.when(door.closed()).thenReturn(true);
        Mockito.when(dirtFilter.capacity()).thenReturn(100.0d);
        doThrow(PumpException.class).when(waterPump).drain();

        result = dishWasher.start(program);
        expectedResult = getResult(Status.ERROR_PUMP);

        assertEquals(expectedResult.getStatus(), result.getStatus());
    }



    private ProgramConfiguration properProgram() {
        return ProgramConfiguration.builder().withFillLevel(irrelevant).withProgram(notRinse).withTabletsUsed(true).build();
    }

    private RunResult getResult(Status status) {
        RunResult result;
        switch (status) {
            case SUCCESS:
                result = RunResult.builder().withStatus(Status.SUCCESS).withRunMinutes(notRinse.getTimeInMinutes()).build();
                break;
            case ERROR_PROGRAM:
                result = RunResult.builder().withStatus(Status.ERROR_PROGRAM).build();
                break;
            case DOOR_OPEN:
                result = RunResult.builder().withStatus(Status.DOOR_OPEN).build();
                break;
            case ERROR_FILTER:
                result = RunResult.builder().withStatus(Status.ERROR_FILTER).build();
                break;
            case ERROR_PUMP:
                result = RunResult.builder().withStatus(Status.ERROR_PUMP).build();
                break;
            default:
                result = null;
        }

        return result;
    }
}
