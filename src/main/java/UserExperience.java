import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.Toolkit.getDefaultToolkit;

/***************************************************************************************
 *      Ambu stepper motor controller                                                  *
 *      copyright 2020 Vic Wintriss                                                    *
 /***************************************************************************************/
public class UserExperience extends JComponent implements ActionListener
{
    private TestSequences ts;
    private final int screenWidth = getDefaultToolkit().getScreenSize().width;
    private final JButton forwardButton = new JButton("FORWARD");
    private final JButton backwardButton = new JButton("BACKWARD");
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
    private final int leftMargin = 222;
    private final int middleMargin = 250;
    private final Font buttonFont = new Font("SansSerif", Font.PLAIN, 21);
    private final Font resultFont = new Font("SansSerif", Font.BOLD, 28);
    private final Font indicatorFont = new Font("Arial", Font.PLAIN, 17);
    private String direction;

    public void createGUI(String description, String version, String date)
    {
        display.setSize(getDefaultToolkit().getScreenSize().width, getDefaultToolkit().getScreenSize().height);
        display.add(this); //Adds Graphics
        display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        display.getContentPane().setBackground(new Color(255, 160, 160));
        display.setTitle(description + " " + version + " " + date);
        display.setVisible(true);

        forwardButton.setBounds(leftMargin, 108, 150, 34); // ALL Button
        forwardButton.setHorizontalAlignment(SwingConstants.CENTER);
        forwardButton.setFont(buttonFont);
        forwardButton.setBackground(Color.YELLOW);
        forwardButton.addActionListener(this);
        display.add(forwardButton);

        backwardButton.setBounds(leftMargin, 176, 150, 34); // ALL Button
        backwardButton.setHorizontalAlignment(SwingConstants.CENTER);
        backwardButton.setFont(buttonFont);
        backwardButton.setBackground(Color.YELLOW);
        backwardButton.addActionListener(this);
        display.add(backwardButton);

        passTextField.setBounds(500, 125, 120, 50); // PASS indicator
        passTextField.setHorizontalAlignment(SwingConstants.CENTER);
        passTextField.setFont(resultFont);
        passTextField.setText("Hello there");
        display.add(passTextField);
    }

    public void paint(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
    }

    public void actionPerformed(ActionEvent e)
    {
        repaint();
        if (e.getSource() == forwardButton)
        {
            direction = "FORWARD";
            System.out.println("fwd in paint Ticker");
        }
        if (e.getSource() == backwardButton)
        {
            direction = "BACKWARD";
            System.out.println("back in paint Ticker");
        }
    }

    public String getDirection()
    {
        return direction;
    }

    public void setDirection(String direction)
    {
        this.direction = direction;
    }
}
