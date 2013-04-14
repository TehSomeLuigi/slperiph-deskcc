package com.someluigi.slperiph.ccdesk;


import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.deskcc.api.EmulatorPlugin;
import net.deskcc.core.Emulator;

import com.someluigi.slperiph.server.SLPHTTPServer;
import com.someluigi.slperiph.tileentity.TileEntityHTTPD;



/*
public class SLPPlug implements EmulatorPlugin {
		
	public static Boolean httpdEnabled = false;
	
	
	
	
	@Override
	public String getName() {
		return "SomeLuigisPeripherals-PORT-v1.2";
	}

	@Override
	public boolean start() {
		
		/*
		JFrame frame = new JFrame("SomeLuigis Peripherals");
		
		String out = JOptionPane.showInputDialog(frame, "Please enter a port number to start SomeLuigis Peripherals HTTP server on:", "ENTER HTTP PORT NUMBER", JOptionPane.QUESTION_MESSAGE);
		
		try {
			Integer port = Integer.valueOf(out);
			
			httpdEnabled = true;
			
			if (SLPHTTPServer.start(port)) {
				JOptionPane.showMessageDialog(frame, "Http Server start successful!", "SLP-HTTPD: OK", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(frame, "Failed to start Http Server.", "SLP-HTTPD: FAILED", JOptionPane.ERROR_MESSAGE);
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(frame, "You didn't enter a valid port number, the http server will not be started.", "NOT A NUMBER", JOptionPane.ERROR_MESSAGE);
		}

		frame.dispose();
		*
		
		try {
			Integer port = 8080;
			
			httpdEnabled = true;
			
			SLPHTTPServer.start(port);
		} catch (NumberFormatException e) {
			
		}
		
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}

	public static void main(String[] args) {
		
	}

}
*/

public class SLPPlug implements EmulatorPlugin {
	
	public static Boolean httpdEnabled = false;
	
	
	public String getName() {
		return "SomeLuigisPeripherals-PORT-v1.2";
	}

	public boolean start() {
		
		
		JFrame frame = new JFrame("SomeLuigis Peripherals");
		
		String out = JOptionPane.showInputDialog(frame, "Please enter a port number to start SomeLuigis Peripherals HTTP server on:", "ENTER HTTP PORT NUMBER", JOptionPane.QUESTION_MESSAGE);
		
		try {
			Integer port = Integer.valueOf(out);
			
			httpdEnabled = true;
			
			if (SLPHTTPServer.start(port)) {
				JOptionPane.showMessageDialog(frame, "Http Server start successful!", "SLP-HTTPD: OK", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(frame, "Failed to start Http Server.", "SLP-HTTPD: FAILED", JOptionPane.ERROR_MESSAGE);
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(frame, "You didn't enter a valid port number, the http server will not be started.", "NOT A NUMBER", JOptionPane.ERROR_MESSAGE);
		}

		frame.dispose();
		
		/*
		try {
			Integer port = 8080;
			
			httpdEnabled = true;
			
			SLPHTTPServer.start(port);
		} catch (NumberFormatException e) {
			
		}
		
		
		//System.out.println("abcdefg");
		*/
		
		Emulator.getPeripheralRegistry().registerPeripheral("http-server", TileEntityHTTPD.class);
		
		return true;
	}

	public boolean stop() {
		return true;
	}

}