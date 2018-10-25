/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 28-Jul-2009
 * Time: 15:41:18
 */
package com.compomics.util.gui.utils;
import org.apache.log4j.Logger;


import com.compomics.util.enumeration.CompomicsTools;
import com.compomics.util.gui.JLabelAndComponentPanel;
import com.compomics.util.interfaces.Connectable;
import com.compomics.util.io.PropertiesManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ArrayList;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2009/07/30 10:20:39 $
 */

/**
 * This class implements a dialog to gather all information concerning a DB connection.
 *
 * @author Lennart Martens
 */
public class ConnectionDialog extends JDialog {

    /**
     * Empty default constructor
     */
    public ConnectionDialog() {
    }

    // Class specific log4j logger for ConnectionDialog instances.
    Logger logger = Logger.getLogger(ConnectionDialog.class);

    /***
     * ArrayList that holds all the preconfigured connections.
     * Note that when this list holds only 0 or 1 elements,
     * it changes the behaviour of the GUI.
     */
    private ArrayList iConnections = new ArrayList();

    private JComboBox cmbConfigurations = null;
    private JTextField txtDriver = null;
    private JTextField txtUrl = null;
    private JTextField txtUser = null;
    private JPasswordField txtPassword = null;

    private JButton btnOK = null;
    private JButton btnCancel = null;

    private Connectable iTarget = null;

    private String iPropsFile = null;
    private String iLastInitiatedConfiguration;

    /**
     * This constructor takes as arguments the parent JFrame and
     * a title for the dialog.
     * It also constructs the GUI.
     *
     * @param   aParent JFrame that is the parent of this JDialog.
     * @param   aTarget Connectable to which the connection will be passed
     *                  once it is successfully established.
     * @param   aTitle  String with the title for this dialog
     * @param   aPropsFile  String with the filename for a propertiesfile with some connection
     *                      parameters already filled out, notably 'url' and 'driver'. Can be 'null'
     *                      for no defaults. If something goes wrong, no defaults are filled out.
     */
    public ConnectionDialog(JFrame aParent, Connectable aTarget, String aTitle, String aPropsFile) {
        super(aParent, aTitle, true);
        this.iPropsFile = aPropsFile;
        this.iTarget = aTarget;
        this.showConnectionDialog();
    }

    /**
     * This constructor takes as arguments the parent JFrame and
     * a title for the dialog.
     * It also constructs the GUI.
     *
     * @param   aParent JFrame that is the parent of this JDialog.
     * @param   aTarget Connectable to which the connection will be passed
     *                  once it is successfully established.
     * @param   aTitle  String with the title for this dialog
     * @param   aConnectionProperties Properties instance with three variables (name, driver, url)
     */
    public ConnectionDialog(JFrame aParent, Connectable aTarget, String aTitle, Properties aConnectionProperties) {
        super(aParent, aTitle, true);
        this.iTarget = aTarget;

        parseConnectionProperties(aConnectionProperties);
        this.showConnectionDialog();
    }

    /**
     * This method actually shows the ConnectionDialog.
     * It takes care of the GUI related stuff.
     */
    private void showConnectionDialog() {
        this.addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             */
            public void windowClosing(WindowEvent e) {
                cancelTriggered();
            }
        });
        // Load predefined configuration connection parameters, if any.
        this.tryToLoadParams();
        // Create GUI.
        this.constructScreen();
        if(getParent().getLocation().getX() <= 0 || getParent().getLocation().getY() <= 0) {
            this.setLocation(100, 100);
        } else {
            this.setLocation((int)getParent().getLocation().getX()+100, (int)getParent().getLocation().getY()+100);
        }
        this.pack();
        this.setResizable(false);
    }

    /**
     * This method will initialize and lay-out all components.
     */
    private void constructScreen() {
        txtDriver = new JTextField(25);
        txtDriver.addKeyListener(new KeyAdapter() {
            /**
             * Invoked when a key has been typed.
             * This event occurs when a key press is followed by a key release.
             */
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER) {
                    txtUrl.requestFocus();
                } else {
                    super.keyTyped(e);
                }
            }
        });
        txtUrl = new JTextField(25);
        txtUrl.addKeyListener(new KeyAdapter() {
            /**
             * Invoked when a key has been typed.
             * This event occurs when a key press is followed by a key release.
             */
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER) {
                    txtUser.requestFocus();
                } else {
                    super.keyTyped(e);
                }
            }
        });
        txtUser = new JTextField(25);
        txtUser.addKeyListener(new KeyAdapter() {
            /**
             * Invoked when a key has been typed.
             * This event occurs when a key press is followed by a key release.
             */
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocus();
                } else {
                    super.keyTyped(e);
                }
            }
        });
        txtPassword = new JPasswordField(25);
        txtPassword.addKeyListener(new KeyAdapter() {
            /**
             * Invoked when a key has been typed.
             * This event occurs when a key press is followed by a key release.
             */
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER) {
                    connectTriggered();
                } else {
                    super.keyTyped(e);
                }
            }
        });


        // If there are less than 2 predefined connection parameters,
        // there is no reason to show the selection combobox.
        JLabelAndComponentPanel jpanTop = null;
        if(iConnections.size() < 2) {
            jpanTop = new JLabelAndComponentPanel( new JLabel[] { new JLabel("Database driver"), new JLabel("Database URL"), new JLabel("Username"), new JLabel("Password")},
                                                             new JTextField[]{txtDriver, txtUrl, txtUser, txtPassword});
            // Here we have to set the defaults.
            if(iConnections.size() > 0) {
                InnerConfigParams params = (InnerConfigParams) iConnections.get(0);
                initConfiguration(params);
            }
        } else {
            cmbConfigurations = new JComboBox(iConnections.toArray());
            cmbConfigurations.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Object temp = cmbConfigurations.getSelectedItem();
                    if(temp != null) {
                        InnerConfigParams params = (InnerConfigParams) temp;
                        initConfiguration(params);
                    }
                }
            });
            initConfiguration((InnerConfigParams) iConnections.get(0));

            jpanTop = new JLabelAndComponentPanel( new JLabel[] { new JLabel("Predefined connections"), new JLabel("Database driver"), new JLabel("Database URL"), new JLabel("Username"), new JLabel("Password")},
                                                             new JComponent[]{cmbConfigurations, txtDriver, txtUrl, txtUser, txtPassword});
        }

        jpanTop.setBorder(BorderFactory.createTitledBorder("Connection settings"));

        btnOK = new JButton("Connect");
        btnOK.setMnemonic(KeyEvent.VK_O);
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connectTriggered();
            }
        });
        btnOK.addKeyListener(new KeyAdapter() {
            /**
             * Invoked when a key has been typed.
             * This event occurs when a key press is followed by a key release.
             */
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER) {
                    connectTriggered();
                }
            }
        });
        btnCancel = new JButton("Cancel");
        btnCancel.setMnemonic(KeyEvent.VK_C);
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelTriggered();
            }
        });
        btnCancel.addKeyListener(new KeyAdapter() {
            /**
             * Invoked when a key has been typed.
             * This event occurs when a key press is followed by a key release.
             */
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER) {
                    cancelTriggered();
                }
            }
        });

        JPanel jpanButtons = new JPanel();
        jpanButtons.setLayout(new BoxLayout(jpanButtons, BoxLayout.X_AXIS));

        jpanButtons.add(Box.createHorizontalGlue());
        jpanButtons.add(btnOK);
        jpanButtons.add(Box.createRigidArea(new Dimension(15, btnOK.getHeight())));
        jpanButtons.add(btnCancel);
        jpanButtons.add(Box.createRigidArea(new Dimension(10, btnOK.getHeight())));

        JPanel jpanTotal = new JPanel();
        jpanTotal.setLayout(new BoxLayout(jpanTotal, BoxLayout.Y_AXIS));
        jpanTotal.add(jpanTop);
        jpanTotal.add(Box.createRigidArea(new Dimension(jpanTop.getWidth(), 10)));
        jpanTotal.add(jpanButtons);

        this.getContentPane().add(jpanTotal, BorderLayout.CENTER);
    }

    /**
     * This method initializes the predefined connection parameters 'driver'
     * and 'url', if they are found in the specified InnerConfigParams.
     *
     * @param aParams InnerConfigParams with the predefined configuration
     *                parameters to initialize.
     */
    private void initConfiguration(InnerConfigParams aParams) {
        iLastInitiatedConfiguration = aParams.getName();
        if(aParams.getDriver() != null) {
            txtDriver.setText(aParams.getDriver().trim());
        }
        if(aParams.getUrl() != null) {
            txtUrl.setText(aParams.getUrl().trim());
        }
        if(aParams.getUser() != null) {
            txtUser.setText(aParams.getUser().trim());
        }
    }

    /**
     * This method is called when the user attempts to connect.
     */
    private void connectTriggered() {
        String driverClass = txtDriver.getText().trim();
        String url = txtUrl.getText().trim();
        String user = txtUser.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if(driverClass.equals("")) {
            JOptionPane.showMessageDialog(this, "Driver class needs to be specified!", "No driver specified!", JOptionPane.ERROR_MESSAGE);
            txtDriver.requestFocus();
            return;
        }

        if(url.equals("")) {
            JOptionPane.showMessageDialog(this, "Database URL needs to be specified!", "No URL specified!", JOptionPane.ERROR_MESSAGE);
            txtUrl.requestFocus();
            return;
        }

        String errorString = null;
        Connection lConn = null;
        try {
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            Driver d = (Driver)Class.forName(driverClass).newInstance();
            Properties lProps = new Properties();
            if(user != null) {
                lProps.put("user", user);
            }
            if(password != null) {
                lProps.put("password", password);
            }
            lConn = d.connect(url, lProps);
            if(lConn == null) {
                errorString = "Could not connect to the database. Either your driver is incorrect for this database, or your URL is malformed.";
            }
        } catch(ClassNotFoundException cnfe) {
            errorString = "Driver class was not found! (" + cnfe.getMessage() + ")";
        } catch(IllegalAccessException iae) {
            errorString = "Could not access default constructor on driver class! (" + iae.getMessage() + ")";
        } catch(InstantiationException ie) {
            errorString = "Could not create instance of driver class! (" + ie.getMessage() + ")";
        } catch(SQLException sqle) {
            errorString = "Database refused connection! (" + sqle.getMessage() + ")";
        }
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        if(errorString != null) {
            JOptionPane.showMessageDialog(this, new String[]{"Unable to make the connection to '" + url + "' using '" + driverClass + "'!", errorString, "\n"}, "Unable to connect!", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            // Success!
            // First, fut the last used values into a properties file and store for next time.
            Properties lLastUsedProperties = new Properties();
            if(iLastInitiatedConfiguration.toLowerCase().equals("default")){
                lLastUsedProperties.put("user", user);
                lLastUsedProperties.put("driver", driverClass);
                lLastUsedProperties.put("url", url);
            }else{
                lLastUsedProperties.put("user_" + iLastInitiatedConfiguration, user);
                lLastUsedProperties.put("driver_" + iLastInitiatedConfiguration, driverClass);
                lLastUsedProperties.put("url_" + iLastInitiatedConfiguration, url);
            }
            
            PropertiesManager.getInstance().updateProperties(CompomicsTools.MSLIMS, "ms-lims.properties", lLastUsedProperties);

            // Now continue into ms_lims.
            JOptionPane.showMessageDialog(this, new String[]{"Connection to '" + url + "' established!", "\n"}, "Connection established!", JOptionPane.INFORMATION_MESSAGE);
            iTarget.passConnection(lConn, url.substring(url.lastIndexOf(":")+1));
            this.setVisible(false);
            this.dispose();
        }
    }

    /**
     * This method is called when the user presses cancel.
     */
    private void cancelTriggered() {
        this.setVisible(false);
        this.dispose();
        iTarget.passConnection(null, "");
    }

    /**
     * This method attempts to load connection parameters from
     * a properties file in the classpath.
     * If this file is not found, nothing happens.
     * If it is found, the parameters found will be filled out.
     */
    private void tryToLoadParams() {
        if(iPropsFile != null) {
            try {
                Properties p = new Properties();
                InputStream is = ClassLoader.getSystemResourceAsStream(iPropsFile);
                if(is == null) {
                    is = this.getClass().getClassLoader().getResourceAsStream(iPropsFile);
                    if(is == null) {
                        // Leave it at that.
                        return;
                    }
                    logger.info("local classloader.");
                }
                p.load(is);
                parseConnectionProperties(p);

                is.close();
            } catch(Exception e) {
                // Do nothing.
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * This method reads the predefined connection properties from a Properties instance.
     *
     * @param aConnectionProperties Properties instance with the information for the connection(s)
     */
    private void parseConnectionProperties(final Properties aConnectionProperties) {
        // Two options here - old-fashioned, single predefined configuration,
        // or hot new multiple predefined configurations.
        // Distinction is the presence of the 'CONFIGURATION' key in the
        // latter case.
        if( (aConnectionProperties.getProperty("CONFIGURATION") == null || aConnectionProperties.getProperty("CONFIGURATION").trim().length() == 0)
                &&
            (aConnectionProperties.getProperty("configuration") == null || aConnectionProperties.getProperty("configuration").trim().length() == 0)
           ) {
            // In this case, we expect only a sinlge predefined configuration,
            // which will be stored under the 'DEFAULT' key in the hashmap.
            String dbDriver = aConnectionProperties.getProperty("driver");
            if(dbDriver == null) {
                dbDriver = aConnectionProperties.getProperty("DRIVER");
            }
            String dbUrl = aConnectionProperties.getProperty("url");
            if(dbUrl == null) {
                dbUrl = aConnectionProperties.getProperty("URL");
            }
            String dbUser = aConnectionProperties.getProperty("USER");
            if(dbUser == null) {
                dbUser = aConnectionProperties.getProperty("user");
            }

            iConnections.add(new InnerConfigParams(dbUser, "DEFAULT", dbDriver, dbUrl));
        } else {
            String configurationString = aConnectionProperties.getProperty("CONFIGURATION");
            if(configurationString == null) {
                configurationString = aConnectionProperties.getProperty("configuration");
            }
            String[] configurations = configurationString.split(",");
            for (int i = 0; i < configurations.length; i++) {
                String lConfiguration = configurations[i].trim();
                String dbDriver = aConnectionProperties.getProperty("DRIVER_" + lConfiguration);
                if(dbDriver == null) {
                    dbDriver = aConnectionProperties.getProperty("driver_" + lConfiguration);
                }
                String dbUrl = aConnectionProperties.getProperty("url_" + lConfiguration);
                if(dbUrl == null) {
                    dbUrl = aConnectionProperties.getProperty("URL_" + lConfiguration);
                }
                String dbUser = aConnectionProperties.getProperty("USER_" + lConfiguration);
                if (dbUser == null) {
                    dbUser = aConnectionProperties.getProperty("user_" + lConfiguration);
                }
                iConnections.add(new InnerConfigParams(dbUser, lConfiguration, dbDriver, dbUrl));
            }
        }
    }

    /**
     * This class represents a wrapper object for connection properties
     */
    private class InnerConfigParams {

        private String iUser = null;
        private String iName = null;
        private String iDriver = null;
        private String iUrl = null;

        /**
         * Constructor to create an InnerConfigParams object.
         *
         * @param aName String with the connection name
         * @param aDriver String with the driver classname
         * @param aUrl String with the DB URL
         */
        private InnerConfigParams(String aName, String aDriver, String aUrl) {
            iName = aName;
            iDriver = aDriver;
            iUrl = aUrl;
        }

        /**
         * Constructor to create an InnerConfigParams object.
         *
         * @param aUser String with the username
         * @param aName String with the connection name
         * @param aDriver String with the driver classname
         * @param aUrl String with the DB URL
         */
        private InnerConfigParams(String aUser, String aName, String aDriver, String aUrl) {
            this(aName, aDriver, aUrl);
            iUser = aUser;
        }

        public String getUser() {
            return iUser;
        }

        public void setUser(String aUser) {
            iUser = aUser;
        }

        public String getDriver() {
            return iDriver;
        }

        public void setDriver(String aDriver) {
            iDriver = aDriver;
        }

        public String getUrl() {
            return iUrl;
        }

        public void setUrl(String aUrl) {
            iUrl = aUrl;
        }

        public String toString() {
            return iName;
        }

        public String getName() {
            return iName;
        }

        public void setName(final String aName) {
            iName = aName;
        }
    }
}
