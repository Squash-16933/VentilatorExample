import com.pi4j.io.gpio.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TestSequences implements ActionListener
{
    private UserExperience ux;
    // Gpio pins used for the tester
    private final GpioController gpio = GpioFactory.getInstance();
    private final GpioPinDigitalInput pin38 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, "Raspi pin 38", PinPullResistance.PULL_UP);  // DataOut
    private final GpioPinDigitalInput pin40 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, "Raspi pin 40", PinPullResistance.PULL_UP);  // LpClkOut
    private final GpioPinDigitalInput pin32 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_26, "Raspi pin 32", PinPullResistance.PULL_UP);  // ModeOut
    private final GpioPinDigitalInput pin29 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_21, "Raspi pin 29", PinPullResistance.PULL_UP);  // ClkOut
    private final GpioPinDigitalInput pin15 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, "Raspi pin 15", PinPullResistance.PULL_UP);  // Eripple
    private final GpioPinDigitalInput pin16 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, "Raspi pin 16", PinPullResistance.PULL_UP);  // Rclk
    private final GpioPinDigitalInput pin08 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_15, "Raspi pin 08", PinPullResistance.PULL_UP);  // S/L
    private final GpioPinDigitalInput pin07 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07, "Raspi pin 07", PinPullResistance.PULL_UP);  // Emitter
    private final GpioPinDigitalOutput pin35 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "RasPi pin 35", PinState.LOW);  // ModeIn
    private final GpioPinDigitalOutput pin36 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "RasPi pin 36", PinState.LOW);  // ClkIn
    private final GpioPinDigitalOutput pin31 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, "RasPi pin 31", PinState.LOW);  // DataIn
    private final GpioPinDigitalOutput pin33 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "RasPi pin 33", PinState.HIGH); // LpClkIn
    private final GpioPinDigitalOutput pin10 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_16, "RasPi pin 10", PinState.LOW);  // Sin
    private final GpioPinDigitalOutput pin03 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_08, "RasPi pin 03", PinState.LOW);  // Esel0
    private final GpioPinDigitalOutput pin05 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_09, "RasPi pin 05", PinState.LOW);  // Esel1
    private final GpioPinDigitalOutput pin37 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "RasPi pin 37", PinState.LOW);  // LedClk
    private final GpioPinDigitalOutput pin13 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "RasPi pin 13", PinState.LOW);  // LedData
    private final GpioPinDigitalOutput pin11 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "RasPi pin 11", PinState.LOW);  // LedOn
    // Buttons visible on the display
    private JButton allButton = getAllButton();
    private JButton teeButton = getTeeButton();
    private JButton screenButton = getScreenButton();
    private JButton sensorButton = getSensorButton();
    private JButton commButton = getCommButton();
    private JButton resetButton = getResetButton();
    private JButton printButton = getPrintButton();
    private JButton runButton = getRunButton();
    // Flags used to set test mode
    private final boolean[] mode = new boolean[6];
    private final boolean[] action = new boolean[4];
    // Flags used for error reporting
    private boolean errDataOut = false; // flags for individual errors
    private boolean errLpClkOut = false;
    private boolean errModeOut = false;
    private boolean errClkOut = false;
    private boolean errEripple = false;
    private boolean errRclk = false;
    private boolean errShiftLoad = false;
    private boolean errSin = false;
    private boolean errFail = false; // one or more tests failed
    private final boolean[] errorList = new boolean[8];
    private int errTestByteHigh = 0; // byte used for reporting sensors errors, top 8 bits
    private int errTestByteLow = 0;  // byte used for reporting sensors errors, bottom 8 bits
    private int errEmitter = 0;      // byte used for reporting emitter errors
    // Variables used for testing
    private int testByte = 0;        // byte used for testing sensors, top and bottom 8 bits

    // Reset all errors and set all indicators to default state before running tests
    private void resetErrors() {
        errDataOut = false;
        errorList[0] = false; // errDataOut
        errLpClkOut = false;
        errorList[1] = false; // errLpClkOut
        errModeOut = false;
        errorList[2] = false; // errModeOut
        errClkOut = false;
        errorList[3] = false; // errClkOut
        errEripple = false;
        errorList[4] = false; // errEripple
        errRclk = false;
        errorList[5] = false; // errRclk
        errShiftLoad = false;
        errorList[6] = false; // errShiftLoad
        errSin = false;
        errorList[7] = false; // errSin
        errTestByteLow = 0;  // reset sensors errors, bottom 8 bits
        errTestByteHigh = 0; // reset sensors errors, top 8 bits
        errEmitter = 0;      // reset emitter errors
        System.out.println("Error bits were reset");
    }

    // Set all errors for reporting
    private void setErrorList() {
        if (errDataOut) errorList[0] = true;   // errDataOut
        if (errLpClkOut) errorList[1] = true;  // errLpClkOut
        if (errModeOut) errorList[2] = true;   // errModeOut
        if (errClkOut) errorList[3] = true;    // errClkOut
        if (errEripple) errorList[4] = true;   // errEripple
        if (errRclk) errorList[5] = true;      // errRclk
        if (errShiftLoad) errorList[6] = true; // errShiftLoad
        if (errSin) errorList[7] = true;       // errSin
        System.out.println("Error bits were set");
    }

    // Set CPLD state machine to the RESET state
    private void resetSequence() {
        pin35.low();  // ModeIn t1
        pin36.low();  // ClkIn t2
        pin31.low();  // DataIn
        pin33.high(); // LpClkIn
        pin10.low();  // Sin
        pin11.low();  // LedOn
    }

    // Set CPLD state machine to the tee frame state. Test signals
    private void teeSequence() {
        pin36.high(); // ClkIn t3
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut
        if ( pin15.isLow() ) errEripple = true;    // Eripple
        if ( pin16.isLow() ) errRclk = true;       // Rclk
        if ( pin08.isLow() ) errShiftLoad = true;  // ShiftLoad
        pin35.high(); // ModeIn t4
        pin36.low();  // ClkIn t5
        pin36.high(); // ClkIn t6
        pin36.high(); // ClkIn t7
        pin36.high(); // ClkIn t8
    }

    // Set CPLD state machine to the screen frame state. Test signals
    private void screenSequence() {
        pin36.high(); // ClkIn t3
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut
        if ( pin15.isLow() ) errEripple = true;    // Eripple
        if ( pin16.isLow() ) errRclk = true;       // Rclk
        if ( pin08.isLow() ) errShiftLoad = true;  // ShiftLoad
        pin35.high(); // ModeIn
        pin36.low();  // ClkIn
        pin36.high(); // ClkIn
        pin36.low();  // ClkIn
        pin36.high(); // ClkIn
    }

    // Set CPLD state machine to select on-board emitter. Test signals
    private void emitterSelSequence() {
        pin35.low();  // ModeIn t9
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        if ( pin16.isLow() ) errRclk = true;       // Rclk Error
        if ( pin08.isLow() ) errShiftLoad = true;  // ShiftLoad Error
        pin35.high(); // ModeIn t10
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        if ( pin16.isLow() ) errRclk = true;       // Rclk Error
        if ( pin08.isHigh() ) errShiftLoad = true; // ShiftLoad Error
        pin36.low();  // ClkIn t11
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        pin36.high(); // ClkIn t12
        pin36.high(); // ClkIn t13
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        pin36.high(); // ClkIn t14
    }

    // Set CPLD state machine to select next board emitter. Test signals
    private void emitterDeselSequence() {
        pin35.low();  // ModeIn t9
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        if ( pin16.isLow() ) errRclk = true;       // Rclk Error
        if ( pin08.isLow() ) errShiftLoad = true;  // ShiftLoad Error
        pin35.high(); // ModeIn t10
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        if ( pin16.isLow() ) errRclk = true;       // Rclk Error
        if ( pin08.isHigh() ) errShiftLoad = true; // ShiftLoad Error
        pin36.low();  // ClkIn t11
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        pin36.high(); // ClkIn t12
        pin36.low();  // ClkIn t13
        if ( pin15.isHigh() ) errEripple = true;   // Eripple Error
        pin36.high(); // ClkIn t14
    }

    // Set CPLD state machine to set emitter position, fire emitter. Test signals
    private void emitterFireSequence(int emitter) {
        int position = 1;                 // emitter position
        position = position << emitter-1; // current bit position of emitter
        selectEmitter(emitter);
        pin35.low();  // ModeIn t15
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        if ( pin16.isLow() ) errRclk = true;       // Rclk Error
        if ( pin08.isHigh() ) errShiftLoad = true; // ShiftLoad Error
        // test for correct emitter activation at current position
        pin35.high(); // ModeIn t16
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        if ( pin16.isLow() ) errRclk = true;       // Rclk Error
        if ( pin08.isHigh() ) errShiftLoad = true; // ShiftLoad Error
        if ( pin07.isHigh() ) errEmitter = errEmitter | position; // Emitter Error
        pin11.high(); // LedOn t17
        pin36.low();  // ClkIn
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        if ( pin16.isHigh() ) errRclk = true;      // Rclk Error
        if ( pin08.isHigh() ) errShiftLoad = true; // ShiftLoad Error
        if ( pin07.isLow() ) errEmitter = errEmitter | position;  // Emitter Error
        pin36.high(); // ClkIn t18
        pin11.low();  // LedOn
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        if ( pin16.isLow() ) errRclk = true;       // Rclk Error
        if ( pin08.isHigh() ) errShiftLoad = true; // ShiftLoad Error
        if ( pin07.isHigh() ) errEmitter = errEmitter | position; // Emitter Error
    }

    // Set CPLD state machine to shift out data from the tee frame, including Sin. Test signals
    private void teeShiftOutSequence(boolean sIn) {
        int data;      // Photo diode test pattern data masked for each LED position
        int sensor;    // Photo diode position
        boolean state; // Pin state
        if (!sIn)
            pin10.low();   // Sin
        else pin10.high(); // Sin
        pin35.low();       // ModeIn t19
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        if ( pin15.isLow() ) errEripple = true;    // Eripple Error
        if ( pin16.isLow() ) errRclk = true;       // Rclk Error
        if ( pin08.isHigh() ) errShiftLoad = true; // ShiftLoad Error
        pin35.high();      // ModeIn t20
        if ( pin07.isHigh() ) errEmitter = 0;      // Emitter Error (turned off)

        // shift out photo diode data from the sensor board CPLD shift register, low byte
        for (int i = 0; i < 8; i++) // t21-t36
        {
            pin36.low();  // ClkIn
            if ( pin40.isHigh() ) errLpClkOut = true;  // LpClkOut Error
            pin36.high(); // ClkIn
            if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error

            // test for correct IR detection by photo diodes
            sensor = 1;
            sensor = sensor << i;
            data = testByte & sensor; // current test pattern masked
            state = pin38.getState().isHigh();
            if (state == (data == 0)) {
                errDataOut = true;                        // DataOut Error
                errTestByteLow = errTestByteLow | sensor; // Error at current sensor position
            }
        }
        // shift out photo diode data from the sensor board CPLD shift register, high byte
        for (int i = 0; i < 8; i++) // t37-t52
        {
            pin36.low();  // ClkIn
            if ( pin40.isHigh() ) errLpClkOut = true;  // LpClkOut Error
            pin36.high(); // ClkIn
            if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error

            // test for correct IR detection by photo diodes
            sensor = 1;
            sensor = sensor << i;
            data = testByte & sensor; // current test pattern masked
            state = pin38.getState().isHigh();
            if (state == (data == 0)) {
                errDataOut = true;                          // DataOut Error
                errTestByteHigh = errTestByteHigh | sensor; // Error at current sensor position
            }
        }
        // shift out Sin data
        pin36.low();  // ClkIn t53
        if ( pin40.isHigh() ) errLpClkOut = true; // LpClkOut Error
        pin36.high(); // ClkIn t54
        if ( pin40.isLow() ) errLpClkOut = true;  // LpClkOut Error
        state = pin38.getState().isHigh();
        if ( state != sIn ) errSin = true;        // Sin Error
    }

    // Set CPLD state machine to shift out data from the screen frame. Test signals
    private void screenShiftOutSequence() {
        // test the screen frame connector
        pin35.low();  // ModeIn t19
        if ( pin32.isHigh() ) errModeOut = true;   // ModeOut Error
        pin35.high(); // ModeIn t20
        if ( pin32.isLow() ) errModeOut = true;    // ModeOut Error
        pin31.low();  // DataIn t21
        pin33.low();  // LpClkIn
        if ( pin38.isHigh() ) errLpClkOut = true;  // DataOut Error
        if ( pin40.isHigh() ) errLpClkOut = true;  // LpClkOut Error
        pin33.high(); // ClkIn t22
        if ( pin38.isHigh() ) errLpClkOut = true;  // DataOut Error
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        pin31.high(); // DataIn t23
        pin33.low();  // LpClkIn
        if ( pin38.isLow() ) errLpClkOut = true;   // DataOut Error
        if ( pin40.isHigh() ) errLpClkOut = true;  // LpClkOut Error
        pin33.high(); // ClkIn t25
        if ( pin38.isLow() ) errLpClkOut = true;   // DataOut Error
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
    }

    //  Selects one of four emitter positions for testing
    private void selectEmitter(int emitter) {
        switch (emitter) {
            case 1:
                pin03.low();  // Esel0
                pin05.low();  // Esel1
                break;
            case 2:
                pin03.low();  // Esel0
                pin05.high(); // Esel1
                break;
            case 3:
                pin03.high(); // Esel0
                pin05.low();  // Esel1
                break;
            case 4:
                pin03.high(); // Esel0
                pin05.high(); // Esel1
                break;
        }
    }

    // Load the LED shift register with the sensor test pattern
    private void loadTestWord(int testByte) {
        boolean state;
        int led; // LED position to be loaded into shift register
        for (int i = 0; i < 8; i++) // Load test pattern, MSB D8 ... LSB D1
        {
            led = 128;
            led = led >> i;
            led = testByte & led;  // current byte test pattern masked
            state = !(led == 0);
            pin37.low();           // LedClk
            pin13.setState(state); // LedData
            pin37.high();          // LedClk
        }
        pin13.low();  // LedData Leave low after done
        pin37.low();  // LedClk Leave low after done
    }

    // Set CPLD state machine to the tee frame and test all the emitters
    private void testTee() {
        System.out.println("tee test");
        errEmitter = 0;
        for (int i = 1; i < 5; i++) {
            resetSequence();        // t1-t2
            teeSequence();          // t3-t8
            emitterSelSequence();   // t9-t14
            emitterFireSequence(i); // t15-t18
            resetSequence();        // t1-t2
        }
    }

    // Set CPLD state machine to the screen frame and test the interconnection signals
    private void testScreen() {
        System.out.println("screen test");
        resetSequence();      // t1-t2
        screenSequence();     // t3-t8
        emitterSelSequence(); // t9-t14
        resetErrors(); // reset errors at end of t14, bit bang the emitter fire sequence
        if ( pin40.isLow() ) errLpClkOut = true;   // LpClkOut Error
        if ( pin32.isLow() ) errModeOut = true;    // ModeOut Error
        if ( pin29.isLow() ) errClkOut = true;     // ClkOut Error
        pin35.low();  // ModeIn t15
        if ( pin32.isHigh() ) errModeOut = true;   // ModeOut Error
        pin35.high(); // ModeIn t16
        if ( pin32.isLow() ) errModeOut = true;    // ModeOut Error
        pin36.low();  // ClkIn t17
        if ( pin29.isHigh() ) errClkOut = true;    // ClkOut Error
        pin36.high(); // ClkIn t18
        if ( pin29.isLow() ) errClkOut = true;     // ClkOut Error
        screenShiftOutSequence();
        resetSequence();
    }

    // Test each individual IR photodiode for correct operation
    private void testSensors() {
        System.out.println("sensors test");
        for (int i = 0; i < 8; i++) // walking 1 test pattern
        {
            resetSequence();      // t1-t2
            teeSequence();        // t3-t8
            emitterSelSequence(); // t9-t14
            testByte = 1;
            testByte = testByte << i;
            testByte = 0; // ### TEST ###
            loadTestWord(testByte);
            emitterFireSequence(0);          // t15-t18
            teeShiftOutSequence(true);  // t19-t54
            resetSequence();                // t55-t56
        }
        for (int i = 0; i < 8; i++) // walking 0 test pattern
        {
            resetSequence();      // t1-t2
            teeSequence();        // t3-t8
            emitterSelSequence(); // t9-t14
            testByte = 1;
            testByte = testByte << i;
            testByte = ~testByte;
            testByte = 0; // ### TEST ###
            loadTestWord(testByte);
            emitterFireSequence(0);          // t15-t18
            teeShiftOutSequence(false); // t19-t54
            resetSequence();                // t55-t56
        }
    }

    // Test the majority of the Comm Board functionality. Uses testByteHigh, testByteLow, emitter, Sin
    private void testBasic() {
        System.out.println("basic test");
        loadTestWord(testByte);
        // Test in tee frame mode with on-board emitter
        resetSequence();
        teeSequence();
        emitterSelSequence();
        emitterFireSequence(0);
        teeShiftOutSequence(false);
        // Test in tee frame mode with next board emitter
        resetSequence();
        teeSequence();
        emitterDeselSequence();
        emitterFireSequence(1);
        teeShiftOutSequence(true);
        // Test the screen frame connections
        resetSequence();
        screenSequence();
        emitterSelSequence();
        emitterFireSequence(2);
        screenShiftOutSequence();
        // End of testing
        resetSequence();
    }

    // Sets the mode array to the indicate the active test mode
    private void setMode(int i) {
        System.out.println("Set the active test mode");
        switch(i) {
            case 1:
                mode[0] = false; // reset mode
                mode[1] = true;  // all tests mode
                mode[2] = false; // tee test mode
                mode[3] = false; // screen test mode
                mode[4] = false; // sensors test mode
                mode[5] = false; // basic test mode
                allButton.setBackground(Color.yellow);
                teeButton.setBackground(Color.white);
                screenButton.setBackground(Color.white);
                sensorButton.setBackground(Color.white);
                commButton.setBackground(Color.white);
                break;
            case 2:
                mode[0] = false; // reset mode
                mode[1] = false; // all tests mode
                mode[2] = true;  // tee test mode
                mode[3] = false; // screen test mode
                mode[4] = false; // sensors test mode
                mode[5] = false; // basic test mode
                allButton.setBackground(Color.white);
                teeButton.setBackground(Color.yellow);
                screenButton.setBackground(Color.white);
                sensorButton.setBackground(Color.white);
                commButton.setBackground(Color.white);
                break;
            case 3:
                mode[0] = false; // reset mode
                mode[1] = false; // all tests mode
                mode[2] = false; // tee test mode
                mode[3] = true;  // screen test mode
                mode[4] = false; // sensors test mode
                mode[5] = false; // basic test mode
                allButton.setBackground(Color.white);
                teeButton.setBackground(Color.white);
                screenButton.setBackground(Color.yellow);
                sensorButton.setBackground(Color.white);
                commButton.setBackground(Color.white);
                break;
            case 4:
                mode[0] = false; // reset mode
                mode[1] = false; // all tests mode
                mode[2] = false; // tee test mode
                mode[3] = false; // screen test mode
                mode[4] = true;  // sensors test mode
                mode[5] = false; // basic test mode
                allButton.setBackground(Color.white);
                teeButton.setBackground(Color.white);
                screenButton.setBackground(Color.white);
                sensorButton.setBackground(Color.yellow);
                commButton.setBackground(Color.white);
                break;
            case 5:
                mode[0] = false; // reset mode
                mode[1] = false; // all tests mode
                mode[2] = false; // tee test mode
                mode[3] = false; // screen test mode
                mode[4] = false; // sensors test mode
                mode[5] = true;  // basic test mode
                allButton.setBackground(Color.white);
                teeButton.setBackground(Color.white);
                screenButton.setBackground(Color.white);
                sensorButton.setBackground(Color.white);
                commButton.setBackground(Color.yellow);
                break;
            default:
                mode[0] = true;  // reset mode (default, same as case 0)
                mode[1] = false; // all tests mode
                mode[2] = false; // tee test mode
                mode[3] = false; // screen test mode
                mode[4] = false; // sensors test mode
                mode[5] = false; // basic test mode
                allButton.setBackground(Color.white);
                teeButton.setBackground(Color.white);
                screenButton.setBackground(Color.white);
                sensorButton.setBackground(Color.white);
                commButton.setBackground(Color.white);
        }
    }

    // Sets the action array to indicate which operation is to be performed
    private void setAction(int i) {
        switch(i) {
            case 1:
                System.out.println("reset button pressed, setAction(1)");
                action[0] = false; // mode selected
                action[1] = true;  // perform reset
                action[2] = false; // perform print
                action[3] = false; // perform run
                break;
            case 2:
                System.out.println("print button pressed, setAction(2)");
                action[0] = false; // mode selected
                action[1] = false; // perform reset
                action[2] = true;  // perform print
                action[3] = false; // perform run
                break;
            case 3:
                System.out.println("run button pressed, setAction(3)");
                action[0] = false; // mode selected
                action[1] = false; // perform reset
                action[2] = false; // perform print
                action[3] = true;  // perform run
                break;
            default:
                System.out.println("mode button only, setAction(0), default case");
                action[0] = true;  // mode selected, same as mode 0
                action[1] = false; // perform reset
                action[2] = false; // perform print
                action[3] = false; // perform run
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        System.out.println("ActionEvent");
        if (e.getSource() == allButton)    { setMode(1); }
        if (e.getSource() == teeButton)    { setMode(2); }
        if (e.getSource() == screenButton) { setMode(3); }
        if (e.getSource() == sensorButton) { setMode(4); }
        if (e.getSource() == commButton)   { setMode(5); }
        ux.updateMode(mode); // passes mode to ux
        setAction(0); // set to indicate mode selection was made (default)
        if (e.getSource() == resetButton)  { setAction(1); }
        if (e.getSource() == printButton)  { setAction(2); }
        if (e.getSource() == runButton)    { setAction(3); }
        ux.updateAction(action); // passes action to ux

        if (action[0]) // do nothing if RESET, PRINT, or RUN buttons were not pressed
        {
            System.out.println("Mode button was pushed");
        }
        else if (action[1]) // reset button pushed
        {
            System.out.println("Resetting errors and clearing display...");
            resetErrors();
        }
        else if (action[2]) // print button pushed
        {
            System.out.println("Reading reset pin states:");
            System.out.print("38-" + pin38.getState() + " ");
            System.out.print("40-" + pin40.getState() + " ");
            System.out.print("32-" + pin32.getState() + " ");
            System.out.print("29-" + pin29.getState() + " ");
            System.out.print("15-" + pin15.getState() + " ");
            System.out.print("16-" + pin16.getState() + " ");
            System.out.print("08-" + pin08.getState() + " ");
            System.out.println("07-" + pin07.getState());
        }
        else if (action[3])
        {
            System.out.println("run button pushed");
            if (mode[1])
            {
                System.out.println("run all tests");
                resetErrors();
                testScreen(); // run first because to resetErrors() in test.
                testTee();
                testSensors();
                errTestByteLow = 1;  // byte used for testing sensors errors, bottom 8 bits   ### REMOVE ###
                errTestByteHigh = 4; // byte used for testing sensors errors, top 8 bits   ### REMOVE ###
                errEmitter = 2;      // byte used for testing emitter errors  ### REMOVE ###
                errFail = true;      // bit indicating FAIL   ### REMOVE ###
            }
            else if (mode[2])
            {
                resetErrors();
                testTee();
                errFail = false;
                errFail = errLpClkOut | errEripple | errRclk | errShiftLoad;
            }
            else if (mode[3])
            {
                resetErrors();
                testScreen();
                errFail = false;
                errFail = errLpClkOut | errModeOut | errClkOut;
            }
            else if (mode[4])
            {
                resetErrors();
                testSensors();
                errFail = false;
                errFail = errDataOut | errSin;
            }
            else if (mode[5])
            {
                testByte = (byte) 0b10101110; // byte used for testing sensors. Active low, LSB is D1
                for (int i = 0; i < 200; i++)
                {
                    resetErrors();
                    testBasic();
                    try { Thread.sleep(100); }   // 1000 milliseconds is one second.
                    catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
                }
            }
            System.out.println("tracer 1");
        }
        System.out.println("tracer 2");
        setErrorList();                         // build the list of errors
        ux.setErrorList(errorList);             // passes error codes to ux
        ux.setErrFail(errFail);                 // passes overall failure flag to ux
        ux.setErrTestByteHigh(errTestByteHigh); // passes high sensor failure bits to ux
        ux.setErrTestByteLow(errTestByteLow);   // passes low sensor failure bits to ux
        ux.setErrEmitter(errEmitter);           // passes emitter position failure bits to ux
        ux.repaint();
    }

    void setUx(UserExperience ux) { this.ux = ux; }

    private JButton getAllButton() { return allButton; }

    private JButton getTeeButton() { return teeButton; }

    private JButton getScreenButton() {return screenButton;}

    private JButton getSensorButton() { return sensorButton; }

    private JButton getCommButton() { return commButton; }

    private JButton getResetButton() { return resetButton; }

    private JButton getPrintButton() { return printButton; }

    private JButton getRunButton() { return runButton; }


    void setAllButton(JButton allButton) { this.allButton = allButton; }

    void setTeeButton(JButton teeButton) { this.teeButton = teeButton; }

    void setScreenButton(JButton screenButton) { this.screenButton = screenButton; }

    void setSensorsButton(JButton sensorButton) { this.sensorButton = sensorButton; }

    void setCommButton(JButton commButton) { this.commButton = commButton; }

    void setResetButton(JButton resetButton) { this.resetButton = resetButton; }

    void setPrintButton(JButton printButton) { this.printButton = printButton; }

    void setRunButton(JButton runButton) { this.runButton = runButton; }

}
