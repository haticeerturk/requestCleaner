package burp;

import java.awt.event.*;
import java.util.*;
import javax.swing.JMenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burp.CustomMenuItem;

@SuppressWarnings("all")
public class BurpExtender implements IBurpExtender {
    static IBurpExtenderCallbacks callbacks;
    private static IExtensionHelpers helpers;

    public void registerExtenderCallbacks (IBurpExtenderCallbacks callbacks) {
        //Callback Objects
        BurpExtender.callbacks = callbacks;
        BurpExtender.helpers = callbacks.getHelpers();

        //Extension Name
        callbacks.setExtensionName("Request Cleaner");

        callbacks.registerContextMenuFactory(new CustomMenuItem(callbacks));

        callbacks.printOutput("Thank you for your installed!");
    }
}