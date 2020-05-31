import com.pi4j.io.gpio.*;
/***************************************************************************************
 *      Ambu stepper motor controller                                                  *
 *      copyright 2020 Vic Wintriss                                                    *
 /***************************************************************************************/
public class TestSequences {
    private final GpioController gpio = GpioFactory.getInstance();
    private final GpioPinDigitalInput pin07 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07, "Raspi pin 07", PinPullResistance.PULL_UP);//
    private final GpioPinDigitalOutput pin11 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "BCM 17", PinState.LOW);//Dir
    private final GpioPinDigitalOutput pin13 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "BCM 27", PinState.LOW);//Pul
    private final GpioPinDigitalOutput pin15 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "BCM 22", PinState.LOW);//En

    public void blink(String direction) {
        pin15.low();// EN invalid, motor under control
        {
            //if (direction.equals("FORWARD"))
            {
                pin11.high();// DIR forward
                for (int k = 0; k < 1500; k++) {
                    try {
                        pin13.high();//step
                        Thread.sleep(1);
                        pin13.low();
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("f ");
                }
            }
            //if (direction.equals("BACKWARD")) {
            pin11.low();// DIR backward
            for (int k = 0; k < 1500; k++) {
                try {
                    pin13.high();//step
                    Thread.sleep(1);
                    pin13.low();
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("b ");
            }
        }
        }
    }

