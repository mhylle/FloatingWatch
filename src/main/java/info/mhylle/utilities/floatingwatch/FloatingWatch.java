package info.mhylle.utilities.floatingwatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FloatingWatch
{

  private Timer timer;

  public static void main(String[] args)
  {
    FloatingWatch floatingWatch = new FloatingWatch();
    floatingWatch.initialize();
    floatingWatch.start();
  }

  private void start()
  {
  }

  private void initialize()
  {
    JFrame frame = new JFrame("Floating Watch");
    frame.setSize(480, 300);
    frame.setAlwaysOnTop(true);
    JPanel contentPanel = new JPanel(new BorderLayout());
    frame.add(contentPanel);

    createClockLabel(contentPanel);
    createTimerLabel(contentPanel);

    frame.addWindowListener(new WindowAdapter()
    {
      @Override public void windowClosing(WindowEvent e)
      {
        timer.runTimer = false;
        System.exit(-1);
      }
    });
    frame.setVisible(true);
  }

  private void createTimerLabel(JPanel contentPanel)
  {
    JPanel timerPanel = new JPanel(new BorderLayout());
    contentPanel.add(timerPanel, BorderLayout.SOUTH);

    JLabel timerLabel = new JLabel("0");
    timerPanel.add(timerLabel, BorderLayout.NORTH);
    JLabel formattedTimerLabel = new JLabel("0:00:00");
    timerPanel.add(formattedTimerLabel, BorderLayout.CENTER);
    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton startButton = new JButton("Start");
    JButton stopButton = new JButton("Stop");
    JButton resetButton = new JButton("Reset");
    timer = new Timer(timerLabel, formattedTimerLabel);
    startButton.addActionListener(e -> timer.start());
    stopButton.addActionListener(e -> timer.stop());
    resetButton.addActionListener(e -> timer.reset());
    buttonPanel.add(startButton);
    buttonPanel.add(stopButton);
    buttonPanel.add(resetButton);
    timerPanel.add(buttonPanel, BorderLayout.SOUTH);
  }

  private void setFontSize(JLabel label)
  {
    Font clockFont = label.getFont();
    String clockText = label.getText();

    int stringWidth = label.getFontMetrics(clockFont).stringWidth(clockText);
    int componentWidth = label.getWidth();

    double widthRatio = (double) componentWidth / (double) stringWidth;

    int newFontSize = (int) (clockFont.getSize() * widthRatio);
    int componentHeight = label.getHeight();

    int fontSizeToUse = Math.min(newFontSize, componentHeight);

    label.setFont(new Font(clockFont.getName(), Font.PLAIN, fontSizeToUse));
  }

  private class Timer
  {
    private final JLabel timerLabel;
    private final JLabel formattedTimerLabel;
    Instant startTime;
    boolean runTimer = true;
    boolean isRunning = false;
    private final Thread workerThread;

    Timer(JLabel timerLabel, JLabel formattedTimerLabel)
    {
      this.timerLabel = timerLabel;
      this.formattedTimerLabel = formattedTimerLabel;
      workerThread = new Thread(() -> {
        while (runTimer) {
          if (isRunning) {
            Instant now = Instant.now();
            Duration elapsed = Duration.between(startTime, now);
            long elapsedMillis = elapsed.toMillis();
            timerLabel.setText("" + elapsedMillis);

            int hours = (int) elapsedMillis / (60 * 60 * 1000);
            int mins = (int) (elapsedMillis / (60 * 1000)) % 60;
            int seconds = (int) (elapsedMillis / (1000)) % 60;
            int remainder = (hours * 60 * 60 + mins * 60 + seconds) * 1000;
            int millis = (int) (elapsedMillis - remainder);
            String hourLabel = "" + hours;
            String minsLabel = "" + mins;
            String secondsLabel = "" + seconds;
            String millisLabel = "" + millis;
            if (mins < 10) {
              minsLabel = "0" + minsLabel;
            }
            if (seconds < 10) {
              secondsLabel = "0" + secondsLabel;
            }
            if (millis < 10) {
              millisLabel = "00" + millisLabel;
            } else if (millis < 100) {
              millisLabel = "0" + millisLabel;
            }
            formattedTimerLabel.setText(hourLabel + ":" + minsLabel + ":" + secondsLabel + ":" + millisLabel);
          }
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            // do nothing
          }
        }
      });
    }

    void start()
    {
      if (!isRunning) {
        isRunning = true;
        if (!workerThread.isAlive()) {
          workerThread.start();
          startTime = Instant.now();
        }
      }
    }

    void stop()
    {
      isRunning = false;
    }

    void reset()
    {
      timerLabel.setText("0");
      formattedTimerLabel.setText("0:00:00:000");
      startTime = Instant.now();
    }
  }

  private void createClockLabel(JPanel contentPanel)
  {
    JPanel clockPanel = new JPanel();
    final JLabel clockLabel = new JLabel();
    clockPanel.add(clockLabel);
    contentPanel.add(clockLabel, BorderLayout.NORTH);
    Thread clockThread = new Thread(new Runnable()
    {
      private boolean clockRunning = true;

      public void run()
      {
        while (clockRunning) {
          DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
          clockLabel.setText(dtf.format(LocalDateTime.now()));
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            // do nothing
          }

          setFontSize(clockLabel);
        }
      }
    });

    clockThread.start();
  }
}
