import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.awt.Toolkit.getDefaultToolkit;

public class UserExperience extends JComponent implements ActionListener
{
    private TestSequences ts;
    private final int screenWidth = getDefaultToolkit().getScreenSize().width;
    private final JButton allButton = new JButton("ALL");
    private final JButton teeButton = new JButton("TEE");
    private final JButton screenButton = new JButton("SCREEN");
    private final JButton sensorButton = new JButton("SENSORS");
    private final JButton commButton = new JButton("COMM");
    private final JButton resetButton = new JButton("RESET");
    private final JButton printButton = new JButton("PRINT");
    private final JButton runButton = new JButton("RUN");
    private final JTextField passTextField = new JTextField("PASS");
    private final JTextField failTextField = new JTextField("FAIL");
    private final JTextField errorCodeDisplayField = new JTextField();
    private final JFrame display = new JFrame();
    private final int leftMargin = 40;
    private final int middleMargin = 250;
    private final Font buttonFont = new Font("SansSerif", Font.PLAIN, 21);
    private final Font resultFont = new Font("SansSerif", Font.BOLD, 28);
    private final Font indicatorFont = new Font("Arial", Font.PLAIN, 17);
    private String codeCat = "";
    private int errTestByteHigh = 0;  // byte used for sensors errors, top 8 bits
    private int errTestByteLow = 0;   // byte used for sensors errors, bottom 8 bits
    private int errEmitter = 0;       // byte used for emitter errors
    private boolean errFail = false;  // one or more tests failed. Default is PASS
    private boolean[] mode = new boolean[6];   // Mode status used for display control
    private boolean[] action = new boolean[3]; // Action status used for display control
    private boolean[] errorList = new boolean[8];

    public UserExperience()
    {
        Timer paintTicker = new Timer(100, this);
        paintTicker.start();
    }

    public void createGUI(String version)
    {
        display.setSize(getDefaultToolkit().getScreenSize().width, getDefaultToolkit().getScreenSize().height);
        display.add(this); //Adds Graphics
        display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        display.getContentPane().setBackground(new Color(160, 160, 160));
        display.setTitle(version);
        display.setVisible(true);

        ts.setAllButton(allButton);
        ts.setTeeButton(teeButton);
        ts.setScreenButton(screenButton);
        ts.setSensorsButton(sensorButton);
        ts.setCommButton(commButton);
        ts.setResetButton(resetButton);
        ts.setPrintButton(printButton);
        ts.setRunButton(runButton);

        allButton.setBounds(leftMargin, 108, 150, 34); // ALL Button
        allButton.setHorizontalAlignment(SwingConstants.CENTER);
        allButton.setFont(buttonFont);
        allButton.addActionListener(ts);
        display.add(allButton);

        teeButton.setBounds(leftMargin, 153, 150, 34); // TEE button
        teeButton.setHorizontalAlignment(SwingConstants.CENTER);
        teeButton.setFont(buttonFont);
        teeButton.addActionListener(ts);
        display.add(teeButton);

        screenButton.setBounds(leftMargin, 198, 150, 34); // SCREEN button
        screenButton.setHorizontalAlignment(SwingConstants.CENTER);
        screenButton.setFont(buttonFont);
        screenButton.addActionListener(ts);
        display.add(screenButton);

        sensorButton.setBounds(leftMargin, 243, 150, 34); // SENSORS button
        sensorButton.setHorizontalAlignment(SwingConstants.CENTER);
        sensorButton.setFont(buttonFont);
        sensorButton.addActionListener(ts);
        display.add(sensorButton);

        commButton.setBounds(middleMargin, 108, 150, 34); // COMM button
        commButton.setHorizontalAlignment(SwingConstants.CENTER);
        commButton.setFont(buttonFont);
        commButton.addActionListener(ts);
        display.add(commButton);

        resetButton.setBounds(100, 345, 120, 58); // RESET button
        resetButton.setHorizontalAlignment(SwingConstants.CENTER);
        resetButton.setFont(buttonFont);
        resetButton.addActionListener(ts);
        display.add(resetButton);

        printButton.setBounds(300, 345, 120, 58); // PRINT button
        printButton.setHorizontalAlignment(SwingConstants.CENTER);
        printButton.setFont(buttonFont);
        printButton.addActionListener(ts);
        display.add(printButton);

        runButton.setBounds(500, 345, 120, 58); // RUN button
        runButton.setHorizontalAlignment(SwingConstants.CENTER);
        runButton.setFont(buttonFont);
        runButton.addActionListener(ts);
        display.add(runButton);

        passTextField.setBounds(500, 125, 120, 50); // PASS indicator
        passTextField.setHorizontalAlignment(SwingConstants.CENTER);
        passTextField.setFont(resultFont);
        display.add(passTextField);

        failTextField.setBounds(500, 210, 120, 50); // FAIL indicator
        failTextField.setHorizontalAlignment(SwingConstants.CENTER);
        failTextField.setFont(resultFont);
        display.add(failTextField);

        errorCodeDisplayField.setBounds(0, 289, screenWidth, 44);
        errorCodeDisplayField.setFont(buttonFont);
        errorCodeDisplayField.setText(codeCat);
        display.add(errorCodeDisplayField);

        display.setSize(getDefaultToolkit().getScreenSize().width, getDefaultToolkit().getScreenSize().height);
        display.add(this);
        display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        display.getContentPane().setBackground(new Color(160, 160, 160));
        display.setTitle("FSG StripTest ver " + version);
        display.setVisible(true);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(indicatorFont);
        int errBit; // Error bit position
        for (int i = 0; i < 8; i++) {
            // test for correct IR detection by photo diodes
            if (action[1] | mode[2] | mode[3]) {
                g2.setColor(new Color(255, 255, 153)); // Pale Yellow
            } else {
                errBit = 1;
                errBit = errBit << i;
                errBit = errTestByteLow & errBit; // current error masked
                if (errBit == 0) g2.setColor(new Color(0, 255, 0));  // Passed green
                else g2.setColor(new Color(255, 0, 0)); // Failed red
            }
            g2.fillOval((42 * i + 26), 10, 32, 32);
            g2.setColor(Color.BLACK);
            g2.drawString((i + 1) + "", (42 * i + 36), 32);
        }
        // Draw sensor indicators 9-16 with pass/fail colors
        for (int i = 0; i < 8; i++) {
            // test for correct IR detection by photo diodes
            if (action[1] | mode[2] | mode[3]) {
                g2.setColor(new Color(255, 255, 153)); // Pale Yellow
            } else {
                errBit = 1;
                errBit = errBit << i;
                errBit = errTestByteHigh & errBit; // current error masked
                if (errBit == 0) g2.setColor(new Color(0, 255, 0));  // Passed green
                else g2.setColor(new Color(255, 0, 0)); // Failed red
            }
            g2.fillOval((42 * i + 362), 10, 32, 32);
            g2.setColor(Color.BLACK);
            g2.drawString((i + 9) + "", (42 * i + 367), 32);
        }
        // Draw emitter indicators with pass/fail colors
        g2.setStroke(new BasicStroke(.1f));
        g2.setColor(Color.BLACK);
        g2.drawLine(0, 52, (screenWidth), 52);
        g2.setColor(new Color(153, 255, 255));
        g2.fillRect(0, 53, (screenWidth), 44);
        g2.setColor(Color.BLACK);
        g2.drawLine(0, 97, (screenWidth), 97);
        for (int i = 0; i < 4; i++) {
            if (action[1] | mode[3] | mode[4])
                g2.setColor(new Color(255, 255, 153)); // Pale Yellow
            else {
                errBit = 1;
                errBit = errBit << i;
                errBit = errEmitter & errBit; // current error masked
                if (errBit == 0)
                    g2.setColor(new Color(0, 255, 0));  // Passed green
                else g2.setColor(new Color(255, 0, 0)); // Failed red
            }
                g2.fillOval((120 * i + 180), 59, 30, 30);
                g2.setColor(Color.BLACK);
                g2.drawOval((120 * i + 180), 59, 30, 30);
                g2.drawString((i + 1) + "", (120 * i + 190), 80);
        }
        // Draw additional text and graphics
        g2.setFont(buttonFont);
        g2.setColor(Color.BLACK);
        g2.drawString("EMITTERS", leftMargin, 84);
        g2.drawLine(0, 289, (screenWidth), 289);
        g2.drawLine(0, 332, (screenWidth), 332);

        if (action[1]) {
            passTextField.setBackground(Color.white);
            failTextField.setBackground(Color.white);
            errorCodeDisplayField.setText(" Ready");
        } else if (errFail) {
            passTextField.setBackground(Color.white);
            failTextField.setBackground(Color.red);
            buildErrorListDisplay();
            errorCodeDisplayField.setText(codeCat);
        } else {
            passTextField.setBackground(Color.green);
            failTextField.setBackground(Color.white);
            errorCodeDisplayField.setText(" Tests Passed");
        }
    }
    public void actionPerformed(ActionEvent e) { repaint(); }

    void setErrTestByteHigh(int errTestByteHigh) {this.errTestByteHigh = errTestByteHigh;}

    void setErrTestByteLow(int errTestByteLow) {this.errTestByteLow = errTestByteLow;}

    void setErrEmitter(int errEmitter) {this.errEmitter = errEmitter;}

    void setErrFail(boolean errFail) {this.errFail = errFail;}

    void updateMode(boolean[] mode) {this.mode = mode; }

    void updateAction(boolean[] action) {this.action = action; }

    void setTs(TestSequences ts) { this.ts = ts; }

    void setErrorList(boolean[] errorList) { this.errorList = errorList; }

    private void buildErrorListDisplay()
    {
        codeCat = " Failure Error Codes: ";
        for (int i = 0; i < errorList.length; i++)
            if (errorList[i]) { codeCat += (i + ", "); }
    }
}
