import javax.swing.*;

public class Main
{
    /***************************************************************************************
     *      Ambu stepper motor controller
     *      copyright 2019 Vic Wintriss                                                    */
    private final String version = "    version 900.08";
    private final String date = " May 25, 2020 ";
    private final String description = " => Ambu stepper motor controller <= \n";
    /**************************************************************************************/
    private final TestSequences ts = new TestSequences();
    private final UserExperience ux = new UserExperience();

    private Main()
    {
        System.out.println(description + version + date);
        ts.setUx(ux);
        ux.setTs(ts);
        new Timer(100, ux).start();
        ux.createGUI(version);
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(Main::new); }
}

