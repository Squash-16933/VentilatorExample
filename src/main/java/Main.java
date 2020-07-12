import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/***************************************************************************************
 *      Ambu stepper motor controller                                                  *
 *      copyright 2020 Vic Wintriss                                                    *
 /***************************************************************************************/
public class Main implements ActionListener
{
    private final String version = " version 901.00 ";
    private final String date = " July 12, 2020 ";
    private final String description = " * Ambu stepper motor controller * ";
    private final TestSequences ts = new TestSequences();
    private final UserExperience ux = new UserExperience();
    private final Timer paintTicker = new Timer(100, ux);
    private final Timer runTicker = new Timer(100, this);
    private String direction = "BLANK";

    public static void main(String[] args)
    {
        new Main().getGoing();
    }

    private void getGoing()
    {
        System.out.println(description + version + date);
        ux.createGUI(description, version, date);
        //paintTicker.start();
        //runTicker.start();
        ts.blink("FORWARD");
        while (true)
        {
            ts.readLidar();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) //runTicker event
    {
        this.direction = ux.getDirection();
        if (direction.equals("FORWARD"))
        {
            ts.blink("FORWARD");
            System.out.println("fwd in run Ticker");
        }
        if (direction.equals("BACKWARD"))
        {
            ts.blink("BACKWORD");
            System.out.println("fwd in run Ticker");
        }
        ux.setDirection("BLANK");
    }
}

